package it.uniroma3;
import java.util.*;

public class Metriche {    
     // Metodo per calcolare il MRR
     public static double calculateMRR(Map<String, List<String>> results, Map<String, List<String>> groundTruth) {
        double mrr = 0.0;
        int queryCount = 0;

        for (String query : results.keySet()) {
            List<String> retrievedDocs = results.get(query);
            List<String> relevantDocs = groundTruth.get(query);

            for (int i = 0; i < retrievedDocs.size(); i++) {
                if (relevantDocs.contains(retrievedDocs.get(i))) {  // Se il documento è rilevante
                    mrr += 1.0 / (i + 1); // Reciprocal rank
                    break;               // Considera solo il primo rilevante
                }
            }
            queryCount++;
        }

        return mrr / queryCount; // Media
    }

    // Metodo per calcolare il NDCG
    public static double calculateNDCG(List<String> retrievedDocs, List<String> relevantDocs, int k) {
        double dcg = 0.0;
        for (int i = 0; i < Math.min(retrievedDocs.size(), k); i++) {
            if (relevantDocs.contains(retrievedDocs.get(i))) {
                int relevance = 1; // Se rilevante, assegna rilevanza 1 (puoi cambiarlo)
                dcg += (Math.pow(2, relevance) - 1) / (Math.log(i + 2) / Math.log(2));
            }
        }

        // Calcola l'IDCG (ordine ideale)
        double idcg = 0.0;
        for (int i = 0; i < Math.min(relevantDocs.size(), k); i++) {
            int relevance = 1; // Assegna rilevanza 1
            idcg += (Math.pow(2, relevance) - 1) / (Math.log(i + 2) / Math.log(2));
        }

        return idcg == 0.0 ? 0.0 : dcg / idcg; // Normalizza
    }
}
