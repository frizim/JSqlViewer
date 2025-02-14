package com.frizim.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

public class SimpleWindowBuilder {
    
    private final GridBagConstraints constraints;
    private final GridBagLayout layout;
    private final JFrame frame;
    private final JPanel panel;
    private ActionEventContext ctx = new ActionEventContext();
    private boolean buttonsAdded = false;
    private int buttonY = 1;

    @SuppressWarnings("unchecked")
    public abstract class ControlBuilder<T extends ControlBuilder<T, C>, C extends Component> {

        private final SimpleWindowBuilder builder;
        private final String id;
        private String label = "";
        private JButton button = null;
        private ContextAwareActionListener buttonLst = null;
        private Color textColor = Color.BLACK;
        private boolean stacked = false;

        protected ControlBuilder(String id, SimpleWindowBuilder builder) {
            this.id = id;
            this.builder = builder;
        }

        public T label(String label) {
            this.label = label;
            return (T) this;
        }

        public T fontColor(Color color) {
            this.textColor = color;
            return (T) this;
        }

        public T stacked(boolean stacked) {
            this.stacked = stacked;
            return (T) this;
        }

        public T button(String buttonLabel, ContextAwareActionListener listener) {
            button = new JButton(buttonLabel);
            buttonLst = listener;
            return (T) this;
        }

        protected abstract C createInput();

        public SimpleWindowBuilder create() {
            JLabel jl = new JLabel(this.label, stacked ? SwingConstants.LEFT : SwingConstants.RIGHT);
            C textfield = createInput();
            textfield.setForeground(textColor);

            builder.getConstraints().weightx = 0;
            panel.add(jl, constraints);
            builder.constraints.weightx = 1;
            builder.getConstraints().gridx = 1;
            panel.add(textfield, constraints);
            builder.getConstraints().gridx = 0;

            builder.registerComponent(id, textfield);

            if(button != null) {
                constraints.gridx = 2;
                panel.add(button, constraints);
                button.addActionListener(evt -> buttonLst.actionPerformed(evt, builder.ctx));
                constraints.gridx = 0;
            }

            return builder;
        }
    }

    public class InputBuilder extends ControlBuilder<InputBuilder, JTextField> {

        public InputBuilder(String id, SimpleWindowBuilder builder) {
            super(id, builder);
        }

        private String defaultText = "";

        public InputBuilder defaultText(String defaultText) {
            this.defaultText = defaultText;
            return this;
        }

        @Override
        protected JTextField createInput() {
            return new JTextField(defaultText);
        }
        
    }

    public class ComboBuilder extends ControlBuilder<ComboBuilder, JComboBox<String>> {

        private String[] options;
        private ContextAwareItemListener lst;

        public ComboBuilder(String id, SimpleWindowBuilder builder) {
            super(id, builder);
        }

        public ComboBuilder options(String... options) {
            this.options = options;
            return this;
        }

        public ComboBuilder onSelect(ContextAwareItemListener lst) {
            this.lst = lst;
            return this;
        }

        @Override
        protected JComboBox<String> createInput() {
            JComboBox<String> cmb = new JComboBox<>(options);
            if(lst != null) {
                cmb.addItemListener(evt -> {
                    if(evt.getStateChange() == ItemEvent.SELECTED) {
                        lst.actionPerformed(evt, ctx);
                    }
                });
            }
            return cmb;
        }
        
    }

    public SimpleWindowBuilder() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            //Ignored
        }

        this.frame = new JFrame();
        this.panel = new JPanel();
        this.layout = new GridBagLayout();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        frame.setContentPane(panel);
        panel.setLayout(this.layout);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.weightx = 1.0;
        constraints.anchor = GridBagConstraints.BASELINE;
        constraints.insets = new Insets(2, 10, 2, 10);
    }

    public SimpleWindowBuilder title(String title) {
        this.frame.setTitle(title);
        return this;
    }

    public SimpleWindowBuilder minimumSize(int minWidth, int minHeight) {
        this.frame.setMinimumSize(new Dimension(minWidth, minHeight));
        return this;
    }

    public InputBuilder labeledInput(String id) {
        buttonY++;
        return new InputBuilder(id, this);
    }

    public ComboBuilder labeledSelection(String id) {
        buttonY++;
        return new ComboBuilder(id, this);
    }

    public SimpleWindowBuilder button(String text, ContextAwareActionListener listener) {
        if(!buttonsAdded) {
            this.constraints.insets.top = 25;
            this.constraints.gridx = 0;
            this.constraints.gridy = buttonY + 1;
            this.buttonsAdded = true;
        }
        JButton button = new JButton(text);
        this.panel.add(button, this.constraints);
        button.addActionListener(evt -> listener.actionPerformed(evt, this.ctx));
        this.constraints.gridx++;
        return this;
    }

    public SimpleWindowBuilder radio(String id, String... options) {
        this.constraints.gridx = 3;
        ButtonGroup group = new ButtonGroup();
        for(String option : options) {
            JRadioButton radio = new JRadioButton(option);
            radio.setActionCommand(option);
            this.panel.add(radio, this.constraints);
            group.add(radio);
        }
        this.ctx.addGroup(id, group);
        return this;
    }

    public SimpleWindowBuilder fill(Component c, String id) {
        buttonY++;
        ctx.add(id, c);
        constraints.gridx = 0;
        constraints.gridy = buttonY + 1;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.BOTH;
        frame.add(c, constraints);
        return this;
    }

    public JFrame build() {
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return this.frame;
    }

    protected GridBagConstraints getConstraints() {
        return constraints;
    }

    protected void registerComponent(String id, Component c) {
        this.ctx.add(id, c);
    }

}
