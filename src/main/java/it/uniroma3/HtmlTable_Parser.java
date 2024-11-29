package it.uniroma3;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlTable_Parser {

    public static Table parseTable(String tableHtml) {
        
            Document doc = Jsoup.parse(tableHtml);
            Element table = doc.select("table").first();
            
            if(table!=null) {
                // Parsing delle intestazioni
                Elements headers = table.select("th");
                String[] headerArray = headers.stream().map(Element::text).toArray(String[]::new);
        
                // Parsing delle righe
                Elements rows = table.select("tr");
                String[][] rowArray = new String[rows.size()][];
                int rowIndex = 0;
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    rowArray[rowIndex++] = cells.stream().map(Element::text).toArray(String[]::new);
                }

                return new Table(headerArray, rowArray);
            }
        else 
            return new Table(new String[0], new String[0][0]);  // Ritorna una tabella vuota
    }
}
