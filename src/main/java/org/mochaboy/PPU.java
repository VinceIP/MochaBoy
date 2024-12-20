package org.mochaboy;

import java.util.Map;

public class PPU {
    private PPU_MODE ppuMode;
    private int cycleCounter;
    private static final int SCANLINE_CYCLES = 456; //cycles per scanline

    private Memory memory;

    public PPU(Memory memory) {
        this.memory = memory;
        this.cycleCounter = 0;
        init();
    }

    public enum PPU_MODE {
        HBLANK,
        VBLANK,
        OAM_SCAN,
        DRAWING
    }

    private void init() {
        //Init values for PPU at boot time
        setPpuMode(PPU_MODE.OAM_SCAN);
        Map<String, Integer> map = memory.getMemoryMap();
        memory.writeByte(map.get("LY"), 0x00);
        memory.writeByte(map.get("LCDC"), 0x91); //screen on, bg on
        memory.writeByte(map.get("BGP"), 0xFC); //bg palette
        memory.writeByte(map.get("OBP0"), 0xFF); //Sprite palette 0
        memory.writeByte(map.get("OBP1"), 0xFF); //Sprite palette 1
    }

    public void step(int cycles) {
        int LYAddress = memory.getMemoryMap().get("LY");
        int LY = memory.readByte(LYAddress);
        cycleCounter += cycles;
        if(LY == 144) {
            //System.out.println("LY is 144!!!!");
            //System.out.println("LY in memory is: " + memory.readByte(LYAddress));
        }
        //System.out.println("LY: " + LY);
        //System.out.println("PPU cycles: " + cycleCounter);
        //System.out.println("PPU state: " + getPpuMode());

        if (LY >= 144) {
            // Enter V-Blank mode at LY = 144
            if (ppuMode != PPU_MODE.VBLANK) {
                setPpuMode(PPU_MODE.VBLANK);
                triggerVBlankInterrupt(); // Only trigger once when entering V-Blank
            }
        } else if (cycleCounter < 80) {
            // Enter OAM Scan Mode
            if (ppuMode != PPU_MODE.OAM_SCAN) {
                setPpuMode(PPU_MODE.OAM_SCAN);
            }
        } else if (cycleCounter < 252) {
            // Enter Drawing Mode
            if (ppuMode != PPU_MODE.DRAWING) {
                setPpuMode(PPU_MODE.DRAWING);
                drawScanline();
            }
        } else if (cycleCounter < SCANLINE_CYCLES) {
            // Enter H-Blank Mode
            if (ppuMode != PPU_MODE.HBLANK) {
                setPpuMode(PPU_MODE.HBLANK);
            }
        }

        //If the current scanline is complete
        if (cycleCounter >= SCANLINE_CYCLES) {
            cycleCounter -= SCANLINE_CYCLES;
            LY = incrementLY();
            //Reset mode, except during VBLANK
            if (LY < 144) setPpuMode(PPU_MODE.OAM_SCAN);


        }
    }

    public void drawScanline() {

    }

    private void triggerVBlankInterrupt() {
        //Set IF to reflect vblank bit
        int IF = memory.readByte(0xFF0F);
        memory.writeByte(0xFF0F, IF | 0x01);
    }

    /**
     * Listen for and handle changes at PPU-relevant addresses
     *
     * @param address
     */
    public void update(int address) {

    }

    public int incrementLY() {
        Map<String, Integer> map = memory.getMemoryMap();
        int LYAddress = map.get("LY");
        int LY = memory.readByte(LYAddress);
        LY = (LY + 1) % 154;
        memory.writeByte(LYAddress, LY);
        return LY;
    }

    public PPU_MODE getPpuMode() {
        return ppuMode;
    }

    public void setPpuMode(PPU_MODE ppuMode) {
        Map<String, Integer> map = memory.getMemoryMap();
        this.ppuMode = ppuMode;
        int STATAddress = map.get("STAT");
        int STAT = memory.readByte(STATAddress);
        STAT = (STAT & 0xFC) | ppuMode.ordinal();
        memory.writeByte(STATAddress, STAT);
    }
}
