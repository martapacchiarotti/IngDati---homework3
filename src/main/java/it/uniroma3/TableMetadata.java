 package it.uniroma3;

 import java.util.List;
 
 public class TableMetadata {
        String caption;
        String tableHtml;
        List<String> footnotes;
        List<String> references;
        String name;
 
        public TableMetadata(String caption, String tableHtml, List<String> footnotes, List<String> references,String name) {
            this.caption = caption;
            this.tableHtml = tableHtml;
            this.footnotes = footnotes;
            this.references = references;
            this.name=name;
        }

        public String getCaption() {
            return caption;
        }
    
        public String getTableHtml() {
            return tableHtml;
        }
        public List<String> getFootnotes() {
            return footnotes;
        }
    
        public List<String> getReferences() {
            return references;
        }
        public String getName() {
            return name;
        }

 }