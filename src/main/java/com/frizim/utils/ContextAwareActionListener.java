package com.frizim.utils;

import java.awt.event.ActionEvent;

@FunctionalInterface
public interface ContextAwareActionListener {
    
    void actionPerformed(ActionEvent evt, ActionEventContext ctx);

}
