package org.mochaboy;

public class FrameBuffer {
    private final int width;
    private final int height;
    private final int[] pixels;

    public FrameBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
    }

    public synchronized void setPixel(int x, int y, int color) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        pixels[y * width + x] = color;
    }

    public synchronized int[] getPixels() {
        return pixels.clone();
    }

    public synchronized int getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        else return pixels[y * width + x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
