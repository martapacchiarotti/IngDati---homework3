package it.uniroma3;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;


public class Validate_Application {

    /* funzione che legge tutte le tabelle dall'indice */
    private static Map<TableMetadata, Table> loadAllTables(IndexReader reader) throws Exception{
        Map<TableMetadata,Table> allTables = new HashMap<>();
        List<String> footnotes;
        List<String> references;
         for (int i = 0; i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                String caption = doc.get("caption");
                String tableHtml = doc.get("html");
                String name = doc.get("name");
                if(doc.get("footnotes")!=null)
                    footnotes = Arrays.asList(doc.get("footnotes").split(","));
                else
                    footnotes = new ArrayList<>();
                if(doc.get("references")!=null)
                    references = Arrays.asList(doc.get("references").split(","));
                else
                    references = new ArrayList<>();
                TableMetadata metadata = new TableMetadata(caption, tableHtml, footnotes, references,name);
                Table table = HtmlTable_Parser.parseTable(tableHtml);
                if(!table.isEmpty()) 
                    allTables.put(metadata,table);

          }
          return allTables;
    }

    public static void main(String[] args) throws Exception {
        // Configurazione percorsi e preparazione Lucene
        Path indexPath = Paths.get("E:/homework3/index");
        FSDirectory directory = FSDirectory.open(indexPath);
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        String[] fields = {"caption", "references", "footprints", "tableContent"};
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, new StandardAnalyzer());

        // Lista di query predefinite
        List<String> queries = Arrays.asList(
            "Reinforcement Learning",
            "human interactive",
            "CPN network as the 2D pose",
            "record linkage f1 sul dataset wdc",
            "Performance of the prompt ensemble algorithm",
            "Complexities of Secure Federated Learning",
            "action recognition accuracy",
            "video clip retrieval accuracies",
            "Conv2D model for FedNet2Net",
            "precision fault diagnosis "
        );
        //Carica tutte le tabelle 
        Map<TableMetadata,Table> allTables = loadAllTables(reader);
        // Calcola la ground truth per tutte le query
        // restituisce una mappa di elementi (query,lista di tabelle pertinenti)
        Map<String, List<String>> groundTruth = GroundTruth_Generator.generateGroundTruth(queries,allTables,100);

        // Risultati delle query Lucene
        Map<String, List<String>> luceneResults = new HashMap<>();

        // Elaborazione di ogni query con Lucene
        for (String inputQuery : queries) {
            System.out.println("Esecuzione della query: " + inputQuery);

            // 1. Esegui la ricerca con Lucene
            Query query = queryParser.parse(inputQuery);
            TopDocs topDocs = searcher.search(query, 10); // Restituisci le prime 10 tabelle
            List<String> results = new ArrayList<>();

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                String tableHtml = doc.get("html"); // Usa l'html come identificatore
                results.add(tableHtml);
            }

            // 2. Aggiungi i risultati alla mappa
            luceneResults.put(inputQuery, results);
        }

        // Calcola e stampa le metriche
        double mrr = Metriche.calculateMRR(luceneResults, groundTruth);
        System.out.println("MRR complessivo: " + mrr);
        System.out.println("Risultati: " + luceneResults);
        System.out.println("Rilevanti: " + groundTruth);
    }

}
