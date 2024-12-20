package org.mochaboy;

import java.util.Map;

public class PPU {
    private PPU_MODE ppuMode;
    private int cycleCounter;
    private boolean lcdEnabled;
    private final Map<String, Integer> memoryMap;
    private final FrameBuffer frameBuffer;
    private static final int SCANLINE_CYCLES = 456; //cycles per scanline

    private Memory memory;

    public PPU(Memory memory, FrameBuffer frameBuffer) {
        this.memory = memory;
        this.frameBuffer = frameBuffer;
        this.cycleCounter = 0;
        memoryMap = memory.getMemoryMap();
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
        lcdEnabled = true;
        Map<String, Integer> map = memory.getMemoryMap();
        memory.writeByte(map.get("LY"), 0x00);
        memory.writeByte(map.get("LCDC"), 0x91); //screen on, bg on
        memory.writeByte(map.get("BGP"), 0xFC); //bg palette
        memory.writeByte(map.get("OBP0"), 0xFF); //Sprite palette 0
        memory.writeByte(map.get("OBP1"), 0xFF); //Sprite palette 1
    }

    public void step(int cycles) {
        int lcdc = memory.readByte(memoryMap.get("LCDC"));
        lcdEnabled = (lcdc & 0x80) != 0;
        if (!lcdEnabled) {
            memory.writeByte(memoryMap.get("LY"), 0x00);
            setPpuMode(PPU_MODE.HBLANK);
            return;
        }
        int lyAddress = memory.getMemoryMap().get("LY");
        int ly = memory.readByte(lyAddress);
        cycleCounter += cycles;

        if (ly >= 144) {
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
            ly = incrementLY();
            //Reset mode, except during VBLANK
            if (ly < 144) setPpuMode(PPU_MODE.OAM_SCAN);


        }
    }

    public void drawScanline() {
        int lcdc = memory.readByte(memoryMap.get("LCDC"));
        int scx = memory.readByte(memoryMap.get("SCX"));
        int scy = memory.readByte(memoryMap.get("SCY"));
        int ly = memory.readByte(memoryMap.get("LY"));
        int bgp = memory.readByte(memoryMap.get("BGP"));

        if (!lcdEnabled || (lcdc & 0x01) == 0) return;

//        System.out.println("drawScanline: LCDC = 0x" + String.format("%02X", lcdc));
//        System.out.println("drawScanline: SCY = 0x" + String.format("%02X", scy));
//        System.out.println("drawScanline: SCX = 0x" + String.format("%02X", scx));
//        System.out.println("drawScanline: LY = 0x" + String.format("%02X", ly));
//        System.out.println("drawScanline: BGP = 0x" + String.format("%02X", bgp));

        int tileMapBase = ((lcdc & 0x08) != 0) ? 0x9C00 : 0x9800;
        int tileDataBase = ((lcdc & 0x10) != 0) ? 0x8000 : 0x8800;

        int tileRow = (scy + ly) / 8;

        for (int pixel = 0; pixel < 160; pixel++) {
            int tileCol = (scx + pixel) / 8;
            int tileAddress = tileMapBase + (tileRow * 32) + tileCol;
            int tileNum = memory.readByte(tileAddress);
            if (tileDataBase == 0x8800) tileNum = (byte) tileNum; //Sign data if in 8800 method
            int tileDataAddress = tileDataBase + (tileNum * 16);

            int tileY = (scy + ly) % 8;
            int tileData1 = memory.readByte(tileDataAddress + (tileY * 2));
            int tileData2 = memory.readByte(tileDataAddress + (tileY * 2) + 1);

//            System.out.println(String.format("Tile data address: %02X", tileAddress));
//            System.out.println("Tile data 1: " + tileData1);
//            System.out.println("Tile data 2: " + tileData1);


            int tileX = (scx + pixel) % 8;

            int colorBitLow = (tileData1 >> 7 - tileX) & 1;
            int colorBitHigh = (tileData2 >> (7 - tileX)) & 1;

            int colorIndex = (colorBitHigh << 1) | colorBitLow;

            int palette = memory.readByte(bgp);
            int color = (palette >> (colorIndex * 2)) & 0x03;

            int actualColor = getColor(color);

            frameBuffer.setPixel(pixel, ly, actualColor);


        }

    }

    private int getColor(int colorIndex) {
        switch (colorIndex) {
            case 0:
                return 0xFFFFFFFF; // White
            case 1:
                return 0xFFC0C0C0; // Light Gray
            case 2:
                return 0xFF606060; // Dark Gray
            case 3:
                return 0xFF000000; // Black
            default:
                return 0xFF000000;
        }
    }

    private void triggerVBlankInterrupt() {
        //Set IF to reflect vblank bit
        int IF = memory.readByte(0xFF0F);
        memory.writeByte(0xFF0F, IF | 0x01);
    }

    public int incrementLY() {
        Map<String, Integer> map = memory.getMemoryMap();
        int LYAddress = map.get("LY");
        int LY = memory.readByte(LYAddress);
        LY = (LY + 1) % 154;
        memory.writeByte(LYAddress, LY);
        return LY;
    }

    public void printVRAM() {
        System.out.println("-------------------- VRAM (0x8000 - 0x9FFF) --------------------");

        for (int row = 0; row < 96; row++) { // 96 rows of 16 bytes each = 1.5KB (0x8000-0x97FF)
            for (int col = 0; col < 16; col++) {
                int address = 0x8000 + (row * 16) + col;
                int value = memory.readByte(address);

                // Print address for the first byte of each row
                if (col == 0) {
                    System.out.printf("0x%04X: ", address);
                }

                // Print the value in hex
                System.out.printf("%02X ", value);
            }
            System.out.println(); // Newline after each row
        }
        for (int row = 0; row < 32; row++) {
            for (int col = 0; col < 32; col++) {
                int address = 0x9800 + (row * 32) + col;
                int value = memory.readByte(address);

                // Print address for the first byte of each row
                if (col == 0) {
                    System.out.printf("0x%04X: ", address);
                }

                // Print the value in hex
                System.out.printf("%02X ", value);
            }
            System.out.println(); // Newline after each row
        }

        System.out.println("-----------------------------------------------------------------");
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
        int lcdc = memory.readByte(memoryMap.get("LCDC"));
        if (!lcdEnabled) {
            memory.writeByte(map.get("STAT"), (STAT & 0xFC));
        }
    }
}
