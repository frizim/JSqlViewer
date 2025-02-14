package com.frizim.utils;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class ActionEventContext {

    private Map<String, Component> components = new HashMap<>();
    private Map<String, ButtonGroup> groups = new HashMap<>();

    protected void add(String id, Component c) {
        components.put(id, c);
    }

    protected void addGroup(String id, ButtonGroup group) {
        groups.put(id, group);
    }

    public JTextField input(String id) {
        return (JTextField)components.get(id);
    }

    @SuppressWarnings("unchecked")
    public String selected(String id) {
        return (String) ((JComboBox<String>)components.get(id)).getSelectedItem();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String id) {
        return (T)components.get(id);
    }

    public String selectedOption(String id) {
        ButtonModel model = groups.get(id).getSelection();
        return model != null ? model.getActionCommand() : "";
    }

}
