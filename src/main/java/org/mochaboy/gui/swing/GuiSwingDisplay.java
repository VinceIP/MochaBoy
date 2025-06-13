package org.mochaboy.gui.swing;

import org.mochaboy.FrameBuffer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GuiSwingDisplay extends JPanel {
    private final FrameBuffer frameBuffer;
    private final int width = 160;
    private final int height = 144;
    private final int scale = 4;
    private BufferedImage image;
    private int[] frame;
    private boolean enabled;
    private boolean frameReady;

    public GuiSwingDisplay(FrameBuffer frameBuffer) {
        this.frameBuffer = frameBuffer;
        frame = frameBuffer.getPixels();
        init();
    }

    private void init() {
        setPreferredSize(new Dimension(
                (width * scale), height * scale)
        );
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        enabled = true;
        frameReady = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(image, 0, 0, image.getWidth() * scale, image.getHeight() * scale, null);
    }

    public void updateFrame() {
        if (enabled) {
            if (frameReady) {
                setFrameReady(false);
                frame = frameBuffer.getPixels();
                image.setRGB(0, 0, width, height, frame, 0, width);
                validate();
                repaint();
            }
        }
    }

    public int getScale() {
        return scale;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFrameReady() {
        return frameReady;
    }

    public void setFrameReady(boolean frameReady) {
        this.frameReady = frameReady;
    }
}
