import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class ProcessData {
    List<List<Integer>> pageTable;
    Set<Integer> assignedFrames;   // Marcos de pagina asignados
    LinkedHashMap<Integer, Integer> lruMap; // Key: # pagina, Value: # marco (orden LRU)
    List<Reference> references;    // Lista de referencias de memoria para este proceso
    int pageFaults = 0;
    int pageHits = 0;
    int writes = 0;
}
    
