package org.mochaboy.gui;

import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.mochaboy.FrameBuffer;

public class GuiFxDisplay {
    private final FrameBuffer frameBuffer;
    private final int width = 160;
    private final int height = 144;
    private int scale = 4;

    private WritableImage writableImage;
    private PixelWriter pixelWriter;
    private int[] frame;
    private boolean enabled;
    private boolean frameReady;

    public GuiFxDisplay(FrameBuffer frameBuffer) {
        this.frameBuffer = frameBuffer;
        frame = frameBuffer.getPixels();
        init();
    }

    private void init() {
        writableImage = new WritableImage(width, height);
        pixelWriter = writableImage.getPixelWriter();
        enabled = true;
        frameReady = false;
    }

    public void updateFrame() {
        if (enabled) {
            if (frameReady) {
                setFrameReady(false);
                frame = frameBuffer.getPixels();
                pixelWriter.setPixels(0, 0, width, height,
                        PixelFormat.getIntArgbInstance(),
                        frame, 0, width);
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getScale() {
        return scale;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFrameReady() {
        return frameReady;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public WritableImage getWritableImage() {
        return writableImage;
    }



    public void setFrameReady(boolean frameReady) {
        this.frameReady = frameReady;
    }
}
