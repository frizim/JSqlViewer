package com.frizim.jsqlviewer;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.AbstractTableModel;
import com.zaxxer.hikari.HikariDataSource;

public class SqlTableModel extends AbstractTableModel {

    private final String database;
    private final String table;
    private final String[] columns;

    private HikariDataSource ds;
    private ExecutorService queryExecutor;

    private int rowCount = -1;
    private final String[] empty;

    private Map<Integer, String[]> cachedRows = new ConcurrentHashMap<>();

    private static final int QUERY_THREADS = 4;

    public SqlTableModel(HikariDataSource ds, String database, String table) throws SQLException {
        this.ds = ds;
        this.database = database;
        this.table = table;
        this.queryExecutor = Executors.newFixedThreadPool(QUERY_THREADS);

        try(Connection c = ds.getConnection(); Statement stmt = c.createStatement()) {
            ResultSet rs = stmt.executeQuery("DESCRIBE " + table());
            ArrayList<String> lst = new ArrayList<>();
            while(rs.next()) {
                lst.add(rs.getString(1));
            }
            this.columns = lst.toArray(new String[0]);
        }

        this.empty = new String[this.columns.length];
    }

    @Override
    public int getRowCount() {
        if(this.rowCount == -1) {
            try(Connection c = ds.getConnection(); PreparedStatement stmt = c.prepareStatement("SELECT COUNT(*) FROM " + table())) {
                ResultSet rs = stmt.executeQuery();
                if(rs.next()) {
                    this.rowCount = rs.getInt(1);
                }
                return 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }
        }

        return this.rowCount;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 0;
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        String[] cached = cachedRows.get(rowIndex);
        if(cached != null) {
            return cached[columnIndex];
        }

        cachedRows.put(rowIndex, empty);

        CompletableFuture.runAsync(() -> {
            try(Connection c = ds.getConnection(); PreparedStatement stmt = c.prepareStatement("SELECT * FROM " + table() + " LIMIT 10 OFFSET ?")) {
                stmt.setInt(1, rowIndex);
                ResultSet rs = stmt.executeQuery();
                int relIndex = 0;
                while(rs.next()) {
                    String[] row = new String[this.getColumnCount()];
                    for(int i = 0; i < row.length; i++) {
                        row[i] = rs.getString(i + 1);
                    }
                    cachedRows.put(rowIndex + relIndex, row);
                    relIndex++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, this.queryExecutor).thenRun(() -> this.fireTableRowsUpdated(rowIndex, rowIndex + 9));

        return "";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String currentKey = getValueAt(rowIndex, 0);
        try(Connection c = ds.getConnection(); PreparedStatement stmt = c.prepareStatement("UPDATE " + table() + " SET " + columns[columnIndex] + " = ? WHERE " + columns[0] + " = ?")) {
            stmt.setString(1, aValue.toString());
            stmt.setString(2, currentKey);
            stmt.executeUpdate();
            this.cachedRows.remove(rowIndex);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        this.queryExecutor.shutdownNow();
    }

    private String table() {
        return "`" + this.database + "`.`" + this.table + "`";
    }
    
}
