import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SimulatorOption2 {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java SimulatorOption2 <#pageFrames>, <#process>");
            return;
        }

        int pageFrames = Integer.parseInt(args[0]);
        int NPROC = Integer.parseInt(args[1]);
        int framesPerProcess = pageFrames / NPROC;
        int TP = 0;

        // Datos por proceso
        List<ProcessData> processes = new ArrayList<>();

        System.out.println("Inicio");
        for (int i = 0; i < NPROC; i++) {
            String filename = "proc" + i + ".txt";
            System.out.println("PROC " + i + " == Leyendo archivo de configuración ==");
            File inputFile = new File(filename);

            int NF = 0;
            int NC = 0;
            int NR = 0;
            int NP = 0;

            List<List<Integer>> pageTable = new ArrayList<>();
            Set<Integer> assignedFrames = new HashSet<>();
            List<Reference> references = new ArrayList<>();

            try(Scanner sc = new Scanner(inputFile)) {
                // Config
                for (int l = 0; l < 5 && sc.hasNextLine(); l++) {
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
                    if (line.startsWith("NR=")) {
                        NR = Integer.parseInt(line.split("=")[1]);
                        System.out.println("PROC " + i + " leyendo NR. Num Referencias: "+NR);
                    }
                    if (line.startsWith("NP=")) {
                        NP = Integer.parseInt(line.split("=")[1]);
                        System.out.println("PROC " + i + " leyendo NP. Num Páginas: "+NP);
                    }
                }
                // Tabla de páginas
                for (int j = 0; j < NP; j++) {
                    List<Integer> fila = new ArrayList<>();
                    fila.add(-1); // asigna marco, -1 si no asignado
                    fila.add(0); // r bit
                    fila.add(0); // w bit
                    pageTable.add(fila);
                }
                // Referencias
                while (sc.hasNextLine()) {
                    String line = sc.nextLine().trim();
                    if (line.isEmpty()) continue;
                    // Formato: M1: [j-k], pagina, offset, r/w
                    String[] parts = line.split(",");
                    if (parts.length < 4) continue;
                    int page = Integer.parseInt(parts[1].trim());
                    int offset = Integer.parseInt(parts[2].trim());
                    char type = parts[3].trim().charAt(0);
                    references.add(new Reference(page, offset, type));
                }
            } catch (FileNotFoundException e) {
                System.out.println("Ocurrio un error al intentar leer el archivo: " + filename);
                System.out.println("Verifique que el archivo exista y esté en el mismo directorio.");
                throw new RuntimeException(e);
            }

            // Asignar marcos a este proceso
            for (int j = 0; j < framesPerProcess; j++) {
                int assignedFrame = j + i * framesPerProcess;
                assignedFrames.add(assignedFrame);
            }

            // LRU: pagina -> marco, accessOrder=true
            LinkedHashMap<Integer, Integer> lruMap = new LinkedHashMap<>(framesPerProcess, 0.75f, true);

            // Guardar datos del proceso
            ProcessData pdata = new ProcessData();
            pdata.pageTable = pageTable;
            pdata.assignedFrames = assignedFrames;
            pdata.lruMap = lruMap;
            pdata.references = references;
            processes.add(pdata);

            System.out.println("PROC " + i + " == Terminó de leer archivo de configuración y referencias ==");
        }

        // --- Loop de simulación (round-robin) ---
        int[] currentRefIndex = new int[NPROC];
        boolean[] retrying = new boolean[NPROC]; // true si reintentando después de una falla de página
        Queue<Integer> processQueue = new LinkedList<>();
        for (int i = 0; i < NPROC; i++) processQueue.add(i);

        // Para estadísticas de SWAP
        int[] swapAccesses = new int[NPROC];

        while (!processQueue.isEmpty()) {
            int pid = processQueue.poll();
            ProcessData pdata = processes.get(pid);
            int idx = currentRefIndex[pid];
            if (idx >= pdata.references.size()) {
                // Proceso terminado, manejar reasignación de marcos si es necesario
                // Encontrar proceso con más fallas de página (si quedan)
                int framesToReassign = pdata.assignedFrames.size();
                pdata.assignedFrames.clear();
                if (!processQueue.isEmpty() && framesToReassign > 0) {
                    int maxFaults = -1, maxPid = -1;
                    for (int qpid : processQueue) {
                        if (processes.get(qpid).pageFaults > maxFaults) {
                            maxFaults = processes.get(qpid).pageFaults;
                            maxPid = qpid;
                        }
                    }
                    if (maxPid != -1) {
                        // Asignar marcos al proceso con más fallas
                        processes.get(maxPid).assignedFrames.addAll(pdata.lruMap.values());
                    }
                }
                continue;
            }

            Reference ref = pdata.references.get(idx);
            int page = ref.page;
            char type = ref.type;

            boolean pageFault = false;
            boolean replacement = false;

            // Verificar si la página está cargada (page hit)
            if (pdata.lruMap.containsKey(page)) {
                if (!retrying[pid]) {
                    pdata.pageHits++;
                }
                pdata.lruMap.get(page); // Actualizar orden LRU
                pdata.pageTable.get(page).set(1, 1); // ref bit
                if (type == 'w') pdata.pageTable.get(page).set(2, 1);
            } else {
                pageFault = true;
                pdata.pageFaults++;
                // Si hay espacio libre, asignar; si no, reemplazar LRU
                if (pdata.lruMap.size() < pdata.assignedFrames.size()) {
                    // Buscar un marco libre
                    for (int frame : pdata.assignedFrames) {
                        if (!pdata.lruMap.containsValue(frame)) {
                            pdata.lruMap.put(page, frame);
                            pdata.pageTable.get(page).set(0, frame);
                            break;
                        }
                    }
                    swapAccesses[pid] += 1; // Page fault sin reemplazo
                } else {
                    // Reemplazo LRU
                    replacement = true;
                    int lruPage = pdata.lruMap.keySet().iterator().next();
                    int victimFrame = pdata.lruMap.remove(lruPage);
                    if (pdata.pageTable.get(lruPage).get(2) == 1) pdata.writes++;
                    pdata.pageTable.get(lruPage).set(0, -1);
                    pdata.pageTable.get(lruPage).set(1, 0);
                    pdata.pageTable.get(lruPage).set(2, 0);
                    pdata.lruMap.put(page, victimFrame);
                    pdata.pageTable.get(page).set(0, victimFrame);
                    swapAccesses[pid] += 2; // Page fault con reemplazo
                }
                pdata.pageTable.get(page).set(1, 1);
                if (type == 'w') pdata.pageTable.get(page).set(2, 1);
            }

            if (pageFault && !retrying[pid]) {
                retrying[pid] = true;
                processQueue.add(pid); // Re-queue para el siguiente turno, misma referencia
            } else {
                retrying[pid] = false;
                currentRefIndex[pid]++;
                if (currentRefIndex[pid] < pdata.references.size()) {
                    processQueue.add(pid); // Más referencias para procesar
                }
            }
        }

        // Mostrar estadísticas
        for (int pid = 0; pid < NPROC; pid++) {
            ProcessData pdata = processes.get(pid);
            int totalRefs = pdata.references.size();
            int faults = pdata.pageFaults;
            int hits = pdata.pageHits;
            int swaps = swapAccesses[pid];
            double faultRate = totalRefs > 0 ? (double)faults / totalRefs : 0.0;
            double hitRate = totalRefs > 0 ? (double)hits / totalRefs : 0.0;
            System.out.println("Proceso: " + pid);
            System.out.println("- Num referencias: " + totalRefs);
            System.out.println("- Fallas: " + faults);
            System.out.println("- Hits: " + hits);
            System.out.println("- SWAP: " + swaps);
            System.out.println("- Tasa fallas: " + String.format("%.4f", faultRate));
            System.out.println("- Tasa éxito: " + String.format("%.4f", hitRate));
            }
        }

    }
