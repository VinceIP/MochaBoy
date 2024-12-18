package org.mochaboy;

public class PPU {
    private int LCDC;
    private int STAT;
    private int SCX, SCY;
    private int LY, LYC;
    private int[] VRAM;
    private int[] OAM;
    private PPU_MODE ppuMode;

    private Memory memory;

    public PPU(Memory memory) {
        this.memory = memory;
    }

    public enum PPU_MODE {
        HBLANK,
        VBLANK,
        OAM_SCAN,
        DRAWING
    }

    public void drawScanline() {

    }

    public void incrementLY() {
        LY = (LY + 1) % 154;
        memory.writeByte(memory.getMemoryMap().get("LY"), LY);
    }

    public PPU_MODE getPpuMode() {
        return ppuMode;
    }

    public void setPpuMode(PPU_MODE ppuMode) {
        this.ppuMode = ppuMode;
    }
}
