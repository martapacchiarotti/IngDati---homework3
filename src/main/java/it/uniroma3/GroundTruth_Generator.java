package it.uniroma3;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;


public class GroundTruth_Generator {

    /**
     * Genera una ground truth basata su una funzione di scoring e un insieme di query.
     *
     * @param queries   L'elenco delle query.
     * @param allTables L'elenco di tutte le tabelle indicizzate (Map con metadati e tabelle).
     * @param topN      Il numero di tabelle da selezionare come rilevanti per ogni query.
     * @return Una mappa dove ogni query è associata a una lista delle tabelle più rilevanti.
     */
    public static Map<String, List<String>> generateGroundTruth(List<String> queries, 
                                                                Map<TableMetadata, Table> allTables, 
                                                                int topN) throws IOException {
        Map<String, List<String>> groundTruth = new HashMap<>();

        for (String query : queries) {
            // Mappa per memorizzare i punteggi di tutte le tabelle per la query corrente
            Map<String, Double> scores = new HashMap<>();

            // Calcola il punteggio per ogni tabella
            for (Map.Entry<TableMetadata, Table> entry : allTables.entrySet()) {
                TableMetadata metadata = entry.getKey();
                Table table = entry.getValue();

                // Calcola il punteggio utilizzando il nostro ScoringProcessor
                double score = TableScoring.calculateScore(query, metadata, table);
                scores.put(metadata.getTableHtml(), score);
            }

            // Ordina le tabelle per punteggio e seleziona le topN
            List<String> topTables = scores.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())) // Ordina in ordine decrescente
                    .limit(topN) // Prendi solo le topN
                    .map(Map.Entry::getKey) // Prendi le tabelle (il codice html)
                    .collect(Collectors.toList());

            // Aggiungi la lista delle topN tabelle alla ground truth per questa query
            groundTruth.put(query, topTables);
        }

        return groundTruth;
    }
}
