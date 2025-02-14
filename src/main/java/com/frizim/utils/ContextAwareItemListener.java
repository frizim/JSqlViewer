package com.frizim.utils;

import java.awt.event.ItemEvent;

@FunctionalInterface
public interface ContextAwareItemListener {
    
    void actionPerformed(ItemEvent evt, ActionEventContext ctx);

}
