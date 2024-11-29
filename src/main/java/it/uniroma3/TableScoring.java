package it.uniroma3;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;


public class TableScoring {

    public static double calculateScore(String query, TableMetadata metadata, Table table) {
        
        Set<String> queryTerms = new HashSet<>(Arrays.asList(query.toLowerCase().split("\\s+")));
        Set<String> captionTerms = new HashSet<>(Arrays.asList(metadata.getCaption().toLowerCase().split("\\s+")));
        Set<String> referencesTerms = new HashSet<>();
        int footnotesCount = 0;
        if(!metadata.getReferences().isEmpty()) {
          for (String reference : metadata.getReferences()) {
              String[] terms = reference.toLowerCase().split("\\s+");
              Collections.addAll(referencesTerms, terms);
          }
        }
        int htmlLength = 0;
        if(metadata.getTableHtml()!=null)
          htmlLength = metadata.getTableHtml().length();
        if(!metadata.getReferences().isEmpty()) 
          footnotesCount = metadata.getFootnotes().size();

        // Integrazione della struttura Table
        int rowCount = table != null ? table.getRows().length : 0;
        int colCount = table != null ? table.getHeaders().length : 0;
        long headerOverlap = table != null
                ? queryTerms.stream().filter(term -> Arrays.asList(table.getHeaders()).contains(term)).count()
                : 0;
        long cellOverlap = table != null
                ? queryTerms.stream().filter(term -> Arrays.asList(table.getRows()).contains(term)).count()
                : 0;

        // Formula del punteggio
        return (2 * captionTerms.stream().filter(queryTerms::contains).count()) +
               (1.5 * referencesTerms.stream().filter(queryTerms::contains).count()) +
               Math.log(htmlLength + 1) +
               (0.2 * footnotesCount) +
               (1.0 * cellOverlap)+ // Bonus per celle corrispondenti
               (1.0 * headerOverlap); // Bonus per header corrispondenti
    }

}
