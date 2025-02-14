package com.frizim.jsqlviewer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.zaxxer.hikari.HikariDataSource;

public class SchemaManager {
    
    private final String schemaDatabase;
    private final HikariDataSource ds;

    public SchemaManager(String schemaDatabase, HikariDataSource ds) {
        this.schemaDatabase = schemaDatabase;
        this.ds = ds;
    }

    public String[] getDatabases() {
        try(Connection c = ds.getConnection()) {
            c.setCatalog(schemaDatabase);
            try(PreparedStatement stmt = c.prepareStatement("SELECT SCHEMA_NAME FROM SCHEMATA")) {
                List<String> res = new ArrayList<>();
                res.add("Bitte auswählen");
                ResultSet rs = stmt.executeQuery();
                while(rs.next()) {
                    res.add(rs.getString(1));
                }
                return res.toArray(new String[0]);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return new String[0];
    }

    public String[] getTables(String database) {
        try(Connection c = ds.getConnection()) {
            c.setCatalog(schemaDatabase);
            try(PreparedStatement stmt = c.prepareStatement("SELECT TABLE_NAME FROM TABLES WHERE TABLE_SCHEMA = ?")) {
                stmt.setString(1, database);
                List<String> res = new ArrayList<>();
                ResultSet rs = stmt.executeQuery();
                while(rs.next()) {
                    if(!rs.getString(1).toLowerCase().endsWith("_schema")) {
                        res.add(rs.getString(1));
                    }
                }
                return res.toArray(new String[0]);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return new String[0];
    }

}
