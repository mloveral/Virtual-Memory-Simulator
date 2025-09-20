import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SimulatorOption2 {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java SimulatorOption2 <#pageFrames>, <#process>");
        }

        int pageFrames = Integer.parseInt(args[0]);
        int NPROC = Integer.parseInt(args[1]);
        int framesPerProcess = pageFrames / NPROC; //marcos a asignar a cada proceso
        int TP; //tamaño de página (es el mismo para cada proceso)

        //Lista con los marcos asignados a cada proceso (el valor de la posición i representa la lista de marcos asignados al proceso i)
        List<Set<Integer>> assignedFrames = new ArrayList<>();

        //Las tablas de traducción por cada proceso
        List<List<List<Integer>>> traductionTables = new ArrayList<>();

        System.out.println("Inicio");
        for (int i = 0; i < NPROC; i++) {
            String filename = "proc" + i + ".txt";
            System.out.println("PROC " + i + " == Leyendo archivo de configuración ==");
            File inputFile = new File(filename);

            int NF;
            int NC;
            int NR;
            int NP = 0;

            //Crear una tabla de traducción para el proceso i
            traductionTables.add(new ArrayList<>());

            try(Scanner sc = new Scanner(inputFile)) {
                String line = sc.nextLine();
                if (line.startsWith("TP=")) {
                    TP = Integer.parseInt(line.split("=")[1]);
                    System.out.println("PROC " + i + " leyendo TP. Tam Páginas: "+TP);
                }
                if (line.startsWith("NF=")) {
                    NF = Integer.parseInt(line.split("=")[1]);
                    System.out.println("PROC " + i + " leyendo NF. Num Filas: "+NF);
                }
                if (line.startsWith("NC=")) {
                    NC = Integer.parseInt(line.split("=")[1]);
                    System.out.println("PROC " + i + " leyendo NC. Num Cols: "+NC);
                }
                if (line.startsWith("NR=")) {NR = Integer.parseInt(line.split("=")[1]);
                    System.out.println("PROC " + i + " leyendo NR. Num Referencias: "+NR);
                }
                if (line.startsWith("NP=")) {
                    NP = Integer.parseInt(line.split("=")[1]);
                    System.out.println("PROC " + i + " leyendo NP. Num Páginas: "+NP);
                }
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred while trying to read the file: " + filename);
                System.out.println("Verify the file exists and it's on the same directory.");
                throw new RuntimeException(e);
            }

            //Se crea una fila por cada página en la tabla de traducción
            for (int j = 0; j < NP; j++) {
                List<Integer> fila = new ArrayList<>();
                fila.add(-1); //pagina cargada
                fila.add(0); //bit de consulta
                fila.add(0); //bit de escritura
                traductionTables.get(i).add(fila);
            }


            System.out.println("PROC " + i + " == Terminó de leer archivo de configuración ==");

            //Asignación de marcos a cada proceso
            Set<Integer> listFrames = new HashSet<>();
            for (int j = 0; j < framesPerProcess; j++) {
                int assignedFrame = j+i*framesPerProcess;
                System.out.println("Proceso "+i+": recibe marco "+(assignedFrame));
                listFrames.add(assignedFrame);
            }
            assignedFrames.add(listFrames);
            System.out.println(assignedFrames.get(i).toString());
        }

    }
}