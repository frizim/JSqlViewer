package com.frizim.jsqlviewer;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import java.util.Properties;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import com.frizim.utils.SimpleWindowBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class JSqlViewer {

    private Properties connectionParams;
    private HikariDataSource ds;
    private SchemaManager schema;

    public JSqlViewer() throws SQLException {
        connectionParams = new Properties();
        try(BufferedReader reader = new BufferedReader(new FileReader(new File("database.properties")))) {
            connectionParams.load(reader);
        } catch(IOException ex) {
            ex.printStackTrace();
            return;
        }

        ds = new HikariDataSource(new HikariConfig(connectionParams));
        schema = new SchemaManager("information_schema", ds);

        JTable tbl = new JTable();
        JScrollPane pane = new JScrollPane(tbl);

        new SimpleWindowBuilder()
            .title("SwingSqlViewer")
            .minimumSize(800, 400)
            .labeledSelection("database")
                .label("Datenbank")
                .options(schema.getDatabases())
                .onSelect((evt, ctx) -> {
                    JComboBox<String> cmb = ctx.get("table");
                    cmb.removeAllItems();
                    for(String table : schema.getTables(ctx.selected("database"))) {
                        cmb.addItem(table);
                    }
                })
                .create()
            .labeledSelection("table")
                .label("Tabelle")
                .options("-- Keine DB --")
                .onSelect((evt, ctx) -> {
                    if(ctx.selected("table") == null) {
                        return;
                    }

                    if(tbl.getModel() instanceof SqlTableModel tm) {
                        tm.close();
                    }

                    Dimension dim = pane.getSize();
                    try {
                        tbl.setModel(new SqlTableModel(ds, ctx.selected("database"), ctx.selected("table")));
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Fehler beim Laden der Tabelle", "Fehler", JOptionPane.ERROR_MESSAGE);
                    }
                    pane.setMinimumSize(dim);
                    pane.repaint();
                })
                .create()
            .fill(pane, "out")
        .build();
    }

    public static void main(String[] args) throws SQLException {
        new JSqlViewer();
    }
}