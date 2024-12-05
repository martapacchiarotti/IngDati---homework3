package it.uniroma3;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

import org.apache.lucene.analysis.standard.StandardAnalyzer; 
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.TokenStream; 
import java.io.StringReader; 
import java.io.IOException;
import org.apache.lucene.analysis.CharArraySet;



public class TableScoring {

    // Creazione di un set statico di stop word
    private static final CharArraySet CUSTOM_STOP_WORDS;

    static {
       // Creazione di un array di stop word
        String[] stopWordsArray = {"of", "the", "on", "sul", "in","as","for"};

        // Creazione di un CharArraySet con queste parole
        CUSTOM_STOP_WORDS = new CharArraySet(stopWordsArray.length, true);
        Collections.addAll(CUSTOM_STOP_WORDS, stopWordsArray);
    }
    private static Set<String> parseQuery(String query) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptySet(); // Restituisce un Set vuoto se la query è nulla o vuota
        }
        Set<String> terms = new HashSet<>();
        try (Analyzer analyzer = new StandardAnalyzer(CUSTOM_STOP_WORDS)) { 
            TokenStream tokenStream = analyzer.tokenStream("query", new StringReader(query));
            tokenStream.reset(); // Inizializza il token stream
            while (tokenStream.incrementToken()) {
                CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
                terms.add(term.toString());
            }
            tokenStream.end(); 
        }
        catch (Exception e) {
          // Stampa l'intero stack trace
          e.printStackTrace();
          throw new IOException("Errore durante l'analisi della query: " + query, e);

        }
        return terms;
}
    public static double calculateScore(String query, TableMetadata metadata, Table table) throws IOException {
        
        Set<String> queryTerms = query != null ? parseQuery(query) : Collections.emptySet(); 
        Set<String> captionTerms = metadata.getCaption() != null ? parseQuery(metadata.getCaption()) : Collections.emptySet();        
        Set<String> referencesTerms = new HashSet<>();

        if(metadata.getReferences() != null && !metadata.getReferences().isEmpty()) {
          for (String reference  : metadata.getReferences()) {              
              referencesTerms.addAll(parseQuery(reference));
          }
        }

        int footnotesCount = 0;
        if (metadata.getFootnotes() != null && !metadata.getFootnotes().isEmpty()) {
            footnotesCount = metadata.getFootnotes().size();
        }

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
               (2 * referencesTerms.stream().filter(queryTerms::contains).count()) +
               (0.2 * footnotesCount) +
               (1.0 * cellOverlap)+ 
               (1.5 * headerOverlap); 
    }

}
