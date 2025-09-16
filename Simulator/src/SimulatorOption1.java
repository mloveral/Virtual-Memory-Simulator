import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SimulatorOption1 {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SimulatorOption1 file");
            return;
        }

        String fileName = args[0];
        System.out.println(fileName);

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
                        String[] mxm = tam.split("x");
                        mTuple.add(Integer.parseInt(mxm[0]));
                        mTuple.add(Integer.parseInt(mxm[1]));
                        TAMS.add(mTuple);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.println(TP);
        System.out.println(NPROC);
        System.out.println(TAMS);


        //Verificar que la cantidad de tamaños de matrices sea igual a la cantidad de procesos
        if (TAMS.size() != NPROC) {
            System.out.println("Error: TAMS is not the same as the NPROC!");
            return;
        }


        for (int i = 0; i <NPROC; i++) {
            int NF = TAMS.get(i).get(0); //número de filas
            int NC = TAMS.get(i).get(1); //número de columnas
            int NR = NF*NC*3; //número de referencias (#enteros por matriz x 3 matrices)
            int NP = (int) Math.ceil(NR*4.0/TP); //número de páginas virtuales

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
                System.out.println("An error occurred.");
                throw new RuntimeException(e);
            }

            // Escribir la información de la memoria virtual asignada para el proceso
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write("TP=" + TP +"\n");
                writer.write("NF=" + NF +"\n");
                writer.write("NC=" + NC +"\n");
                writer.write("NR=" + NR +"\n");
                writer.write("NP=" + NP +"\n");
                System.out.println("Successfully wrote to the file.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
