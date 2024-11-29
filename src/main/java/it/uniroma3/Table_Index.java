package it.uniroma3;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Query;
import java.util.Arrays; 
import java.io.File;
import java.nio.file.Path; 

import java.util.Map;


import java.io.IOException;

public class Table_Index {

    public static void createIndex(Map<TableMetadata,Table> tables, Path indexPath) {
        try  {
            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            int batchSize = 100;
            long startTimeBatch,endTimeBatch,indexingTimeBatch; 
            int tabCount = 0;
            Directory directory = FSDirectory.open(Path.of(indexPath.toString()));
            IndexWriter writer = new IndexWriter(directory, config);

            /* cancella tutti i file indicizzati in precedenza
             * necessario quando bisogna fare un aggiornamento */
            writer.deleteAll();
            
            startTimeBatch = System.currentTimeMillis(); /*inizializza conteggio tempo batch */

            /* data contiene: caption, references e footprints di una tabella */
            for ( TableMetadata data : tables.keySet()) {
                // Creazione del documento Lucene
                Document doc = new Document();
                if (data.caption != null) {
                    doc.add(new TextField("caption", data.getCaption(), TextField.Store.YES));
                }
                if (data.references != null) {
                    for (String reference : data.getReferences()) 
                        doc.add(new TextField("references", reference, TextField.Store.YES)); 
                }   
                if (data.footnotes != null) {
                    for (String footnote : data.getFootnotes()) {
                        doc.add(new TextField("footnotes", footnote, TextField.Store.YES)); 
                    }   
                }
                /* il campo html viene memorizzato nel documento Lucene 
                 * per essere utilizzato durante la visualizzazione dei risultati */
                if (data.tableHtml != null) {
                    doc.add(new StoredField("html", data.getTableHtml()));
                }
                if (data.name != null) {
                    doc.add(new StoredField("name", data.getName()));
                }
                
                /* indicizzazione della tabella 
                 * avremo un campo indice per l'header 
                 * e un campo per ogni riga della tabella */
                Table table = tables.get(data); // tabella con intestazione e celle 
                if (table != null) {
                    /* Converte le intestazioni in una singola stringa */
                    String header = String.join(",", table.getHeaders());
                    doc.add(new TextField("header", header, TextField.Store.YES));

                    for (String[] row : table.getRows()) {
                        String rowContent = String.join(" ", row); // Combina le celle della riga con uno spazio
                        doc.add(new TextField("tableContent", rowContent, TextField.Store.YES)); // Aggiunge il contenuto come campo
                    }
                
                    writer.addDocument(doc);
                    tabCount ++;
                }
                if (tabCount % batchSize == 0) {
                    writer.commit(); // Esegui commit ogni batch di 100 file
                    /*calcola tempo di esecuzione del batch */
                    endTimeBatch = System.currentTimeMillis();
                    indexingTimeBatch = endTimeBatch - startTimeBatch;
                    System.out.println("Batch " + (tabCount / batchSize) + " completato in " + indexingTimeBatch + " ms\n");
                    startTimeBatch = System.currentTimeMillis();  //azzera contatore per il nuovo batch 
                    
                }
            }
            writer.commit();
            writer.close();

            System.out.println("Indice creato con successo!");
            System.out.println("Tabelle indicizzate: "+tabCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runQuery(IndexSearcher searcher, Query query) throws IOException {
        /* restituisce i primi 10 documenti che fanno match */
        TopDocs hits = searcher.search(query,10);
        System.out.println("Sono state trovate " + hits.scoreDocs.length + " " + "tabelle\n");

        /* per ogni documento trovato nella ricerca mostra 
         * la didascalia della tabella 
         * la tabella
         * lo score 
         * references e footprints vengono usati per l'indicizzazione
         * e la ricerca, ma non vengono visualizzati nei risultati
         * per rendere la lettura delle tabelle più imemdiata */
        for (int i = 0; i < hits.scoreDocs.length; i++) {
 
            ScoreDoc scoreDoc = hits.scoreDocs[i];
 
            Document doc = searcher.doc(scoreDoc.doc);
            String tableHtml = doc.get("html"); // Recupera l'HTML archiviato
            if(doc.get("caption")!=null)
                System.out.println(doc.get("caption"));
            if(tableHtml!=null)
                System.out.println('\n'+tableHtml+'\n');
            System.out.println("File "+doc.get("name") + " : (Score: "+ scoreDoc.score +")");

        }
        System.out.print("\n");
    }
    // nota: scoreDoc è un oggetto che ha un campo doc (per recuperare il documento)
    // e un campo score per il punteggio
}
