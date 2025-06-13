package org.mochaboy.gui.swing;

import org.mochaboy.FrameBuffer;

import javax.swing.*;
import java.awt.*;

public class GuiSwingEmulator {
    static final public String title = "MochaBoy";
    private final FrameBuffer frameBuffer;
    private final JFrame frame;
    private final GuiSwingDisplay display;

    public GuiSwingEmulator(FrameBuffer frameBuffer){
        this.frameBuffer = frameBuffer;
        frame = new JFrame();
        display = new GuiSwingDisplay(frameBuffer);
        init();
    }

    private void init(){
        frame.setTitle(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(display, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void run(){
        SwingUtilities.invokeLater(display::updateFrame);
    }

    public GuiSwingDisplay getDisplay(){
        return display;
    }
}
