import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SimulatorOption1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int TP = 0;
        int NPROC = 0;
        List<List<Integer>> TAMS = new ArrayList<>();

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
        sc.close();

        //Verificar que la cantidad de tamaños de matrices sea igual a la cantidad de procesos
        if (TAMS.size() != NPROC) {
            System.out.println("Error: TAMS is not the same as the NPROC!");
            return;
        }

        //Generar un archivo por cada proceso de acuerdo a su información respectiva
        for (int i = 0; i <NPROC; i++) {
            int NF = TAMS.get(i).get(0); //número de filas
            int NC = TAMS.get(i).get(1); //número de columnas
            int NR = NF*NC*3; //número de referencias (#enteros por matriz x 3 matrices)
            int NP = (int) Math.ceil(NR*4.0/TP); //número de páginas virtuales
            String fileName = "proc"+i+".txt";
            try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
                pw.println("TP=" + TP);
                pw.println("NF=" + NF);
                pw.println("NC=" + NC);
                pw.println("NR=" + NR);
                pw.println("NP=" + NP);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
