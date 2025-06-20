package org.mochaboy.gui.fx;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.mochaboy.FrameBuffer;

public class GuiFxDisplay {
    private final int width = 160;
    private final int height = 144;
    private final IntegerProperty scale = new SimpleIntegerProperty(4);

    private FrameBuffer frameBuffer;
    private WritableImage writableImage;
    private PixelWriter pixelWriter;
    private int[] frame;
    private boolean enabled;
    private boolean frameReady;

    public GuiFxDisplay() {
        init();
    }

    private void init() {
        writableImage = new WritableImage(width, height);
        pixelWriter = writableImage.getPixelWriter();
        frameBuffer = new FrameBuffer(160, 144);
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
        return scale.get();
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
        this.scale.set(scale);
    }

    public IntegerProperty scaleProperty() {
        return scale;
    }

    public IntegerBinding scaledWidthProperty() {
        return scale.multiply(width);
    }

    public IntegerBinding scaledHeightProperty() {
        return scale.multiply(height);
    }

    public WritableImage getWritableImage() {
        return writableImage;
    }

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }


    public void setFrameReady(boolean frameReady) {
        this.frameReady = frameReady;
    }
}
