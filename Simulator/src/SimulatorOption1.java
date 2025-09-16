import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SimulatorOption1 {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SimulatorOption1 nameFile");
            return;
        }

        //Abrir el archivo de entrada
        String fileName = args[0];
        File caseFile = new File(fileName);

        int TP = 0;
        int NPROC = 0;
        List<List<Integer>> TAMS = new ArrayList<>();

        try(Scanner sc = new Scanner(caseFile)) {
            //Leer todas las líneas del archivo
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith("TP=")) {
                    TP = Integer.parseInt(line.split("=")[1]);
                } else if (line.startsWith("NPROC=")) {
                    NPROC = Integer.parseInt(line.split("=")[1]);
                } else if (line.startsWith("TAMS=")) {
                    String[] tams = line.split("=")[1].split(",");
                    for (String tam : tams) {
                        List<Integer> mTuple = new ArrayList<>();
                        String[] mxm = tam.split("x"); //sacar los tamaños de fila y columna en una tupla
                        mTuple.add(Integer.parseInt(mxm[0]));
                        mTuple.add(Integer.parseInt(mxm[1]));
                        TAMS.add(mTuple);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while trying to read the file: " + fileName);
            throw new RuntimeException(e);
        }


        //Verificar que la cantidad de tamaños de matrices sea igual a la cantidad de procesos
        if (TAMS.size() != NPROC) {
            System.out.println("Error: TAMS is not the same as the NPROC!");
            return;
        }


        for (int i = 0; i <NPROC; i++) {
            int NF = TAMS.get(i).get(0); //número de filas
            int NC = TAMS.get(i).get(1); //número de columnas
            int NR = NF*NC*3; //número de referencias (#enteros por matriz x 3 matrices)
            int totalSpace = NR*4;
            int NP = (int) Math.ceil(totalSpace*1.0/TP); //número de páginas virtuales

            //Generar un archivo por cada proceso de acuerdo a su información respectiva
            fileName = "proc"+i+".txt";
            try {
                File newFile = new File(fileName);
                if (newFile.createNewFile()) {
                    System.out.println("File created: " + newFile.getName());
                } else {
                    System.out.println("File already exists.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred creating the file of "+fileName);
                throw new RuntimeException(e);
            }

            int[] pr; //estructura para obtener la página actual y la referencia
            int refM1 = 0;
            int refM2 = NC*NF*4;
            int refM3 = NC*NF*4*2;

            // Escribir la información de la memoria virtual asignada para el proceso
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write("TP=" + TP +"\n");
                writer.write("NF=" + NF +"\n");
                writer.write("NC=" + NC +"\n");
                writer.write("NR=" + NR +"\n");
                writer.write("NP=" + NP +"\n");
                for (int j = 0; j < NF; j++) {
                    for (int k = 0; k < NC; k++) {
                        pr = calculatePageAndReference(refM1, TP);
                        writer.write("M1: ["+j+"-"+k+"], "+pr[0]+", "+pr[1]+", r\n");
                        pr = calculatePageAndReference(refM2, TP);
                        writer.write("M2: ["+j+"-"+k+"], "+pr[0]+", "+pr[1]+", r\n");
                        pr = calculatePageAndReference(refM3, TP);
                        writer.write("M3: ["+j+"-"+k+"], "+pr[0]+", "+pr[1]+", w\n");
                        refM1+=4;
                        refM2+=4;
                        refM3+=4;
                    }
                }
            } catch (IOException e) {
                System.out.println("An error occurred while writing in the file of "+fileName);
                throw new RuntimeException(e);
            }
        }
    }

    public static int[] calculatePageAndReference(int currentReference, int pageSize) {
        int page = currentReference/pageSize;
        int ref = currentReference%pageSize;
        return new int[]{page, ref};
    }
}
