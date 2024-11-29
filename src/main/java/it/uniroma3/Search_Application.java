package it.uniroma3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;


public  class Search_Application {

    /* Metodo che legge i file json dalla cartella passata come 
     * parametro e restituisce la lista dei file */
    
    public static ArrayList<File> readJsonFiles(Path directoryPath) throws IOException {
        
        File directory = directoryPath.toFile();
        ArrayList<File> jsonFiles =  new ArrayList<>(); // lista dei file JSON  //lista dei file json
        
        if (directory.exists() && directory.isDirectory()) {
            // Ottieni tutti i file nella directory
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".json")) 
                        jsonFiles.add(file);
                
                }
            } 
        }

        else {
            System.out.println("Il percorso specificato non è una directory valida.");
            jsonFiles = null; // Ritorna null se la directory non è valida

        }
        return jsonFiles; 
    }

    // Metodo per verificare se l'indice esiste
    private static boolean isIndexExists(Path indexPath) {
         File indexDirectory = indexPath.toFile();
        // Verifica che la directory esista e sia effettivamente una directory
        if (!indexDirectory.exists() || !indexDirectory.isDirectory()) {
            return false;
        }
        // Controlla se la directory contiene effettivamente dei file visibili o se ci sono file di lock
        String[] files = indexDirectory.list();
        if (files != null && files.length > 0) {
            // Aggiungi qui un controllo sui file di lock di Lucene, se necessario
            for (String file : files) {
                if (file.endsWith("write.lock")) {
                    return true; // Indica che l'indice esiste a causa del file di lock
                }
            }
        }
        return false; // Nessun file di lock o altri file, quindi l'indice non esiste
    }

    
    public static void main(String args[]) throws Exception {
        Path path = Paths.get("E:/homework3/all_tables");
        Path indexPath = Paths.get("E:/homework3/index");
        int htmlError = 0;
        long start_searchTime = 0, end_searchTime = 0, searchTime = 0;
        FSDirectory directory = null;
        Scanner scanner = new Scanner(System.in);
        JSON_Deserializer deserializer = new JSON_Deserializer();
        ArrayList<File> jsonFiles = new ArrayList<>();
        Map<TableMetadata,Table> tables = new HashMap<>(); 

       
        // Verifica se l'indice esiste già
        if (isIndexExists(indexPath)) 
            System.out.println("Indice trovato, caricamento...");
        else {
             /* leggi i file json */ 
            jsonFiles = readJsonFiles(path); 
            List<TableMetadata> tablesData;  // contiene caption, references, footprints, html
            for(File file : jsonFiles) {
                /* desierializzazione delle tabelle json */
                tablesData= JSON_Deserializer.deserialize(file);
                for (TableMetadata tableMetadata : tablesData) {
                    if (tableMetadata.tableHtml !=null) {
                        /* parsing dell'html */
                        Table tableHtml=HtmlTable_Parser.parseTable(tableMetadata.tableHtml);
                        /* se la tabella è vuota non viene indicizzata*/
                        if (!tableHtml.isEmpty()) 
                            tables.put(tableMetadata,tableHtml);
                        else {
                            System.out.println(" File "+file +": errore nel parsing html: tabella vuota");
                            htmlError++;    
                        }

                    }
                    else {
                        System.out.println("Si è verificato un errore con il codice HTML della tabella.");
                        htmlError++; 
                    }
                }
            }
            System.out.println("\nTabelle non indicizzate: " + htmlError);
            System.out.println("Creazione dell'indice in corso...");
            // creazione dell'indice per le tabelle 
            Table_Index.createIndex(tables,indexPath);
        }
            directory = FSDirectory.open(indexPath);
            IndexReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);
            Boolean exit = false; // per uscire dal ciclo di ricerca
            String[] fields = {"caption", "references", "footprints","tableContent"};
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
        // qui va il codice che permette all'utente di scrivere una 
        // query + valutazione delle metriche 
        while (!exit) {
                
                System.out.print("Inserisci una query: ");
                String input_query = scanner.nextLine(); 
                System.out.println("Ricerca dei risultati in corso...");
                Query query;
                 
                start_searchTime = System.currentTimeMillis();
                query = queryParser.parse(input_query);
                Table_Index.runQuery(searcher,query);
                end_searchTime = System.currentTimeMillis();
                searchTime = end_searchTime - start_searchTime; 

                System.out.print("\nTempo di ricerca: "+ searchTime); 
                System.out.print("\nVuoi uscire [y/n]? ");
                String exit_status = scanner.nextLine().trim(); 
                if(exit_status.equalsIgnoreCase("y"))
                    exit = true; 
         } 

    }
}
  