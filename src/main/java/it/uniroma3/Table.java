
package it.uniroma3;

// Classe per rappresentare la struttura dell tabella JSON in Java
public class Table {
    private String[] headers;
    private String[][] rows;

    public Table(String[] headers, String[][] rows) {
        this.headers = headers;
        this.rows = rows;
    }

    public boolean isEmpty() {
        // La tabella è vuota se sia le intestazioni che le righe sono null o vuote
        return (headers == null || headers.length == 0) && (rows == null || rows.length == 0);
     }

    public int getRowCount() {
        return rows != null ? rows.length : 0;
    }

    public int getColumnCount() {
        return headers != null ? headers.length : (rows != null && rows.length > 0 ? rows[0].length : 0);
    }

    public String[] getHeaders() {
        return headers;
    }

    public String[][] getRows() {
        return rows;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public void setRows(String[][] rows) {
        this.rows = rows;
    }
}
