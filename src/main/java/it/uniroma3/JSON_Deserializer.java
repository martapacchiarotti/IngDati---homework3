package it.uniroma3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSON_Deserializer {

    public static List<TableMetadata> deserialize(File jsonInput) {
        List<TableMetadata> tableList = new ArrayList<>(); // Lista per tutte le tabelle

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonInput); // Parsing del JSON

            Iterator<String> fieldNames = rootNode.fieldNames(); // Itera sulle chiavi del JSON
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode tableObj = rootNode.get(key);

                // Estrai i valori
                String caption = tableObj.has("caption") ? tableObj.get("caption").asText() : "";
                String tableHtml = tableObj.has("table") && !tableObj.get("table").isNull()
                        ? tableObj.get("table").asText()
                        : "";

                // Estrai l'array footnotes
                List<String> footnotes = new ArrayList<>();
                if (tableObj.has("footnotes")) {
                    for (JsonNode footnote : tableObj.get("footnotes")) {
                        footnotes.add(footnote.asText());
                    }
                }

                // Estrai l'array references
                List<String> references = new ArrayList<>();
                if (tableObj.has("references")) {
                    for (JsonNode reference : tableObj.get("references")) {
                        references.add(reference.asText());
                    }
                }

                String name=jsonInput.getName();
                // Crea un'istanza di TableMetadata e aggiungila alla lista
                TableMetadata tableData = new TableMetadata(caption, tableHtml, footnotes, references,name);
                tableList.add(tableData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tableList; 
    }
}
