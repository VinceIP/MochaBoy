package org.mochaboy;

import org.mochaboy.gui.GuiSwingDisplay;
import org.mochaboy.registers.Interrupt;

import javax.swing.*;
import java.util.Map;

public class PPU {
    private PPU_MODE ppuMode;
    private int cycleCounter;
    private boolean lcdEnabled;
    private boolean statInterruptLine;
    private boolean lastStatInterruptLine;
    private boolean lycEqualsLy;
    private final Map<String, Integer> memoryMap;
    private static final int SCANLINE_CYCLES = 456; //cycles per scanline

    private final Memory memory;
    private final FrameBuffer frameBuffer;
    private final GuiSwingDisplay display;
    private Interrupt interrupt;

    private CPU cpu;

    public PPU(Memory memory, FrameBuffer frameBuffer, GuiSwingDisplay display) {
        this.memory = memory;
        this.frameBuffer = frameBuffer;
        this.display = display;
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
        memory.writeByteUnrestricted(map.get("LY"), 0x00);
        memory.writeByteUnrestricted(map.get("LCDC"), 0x91); //screen on, bg on
        memory.writeByteUnrestricted(map.get("BGP"), 0xFC); //bg palette
        memory.writeByteUnrestricted(map.get("OBP0"), 0xFF); //Sprite palette 0
        memory.writeByteUnrestricted(map.get("OBP1"), 0xFF); //Sprite palette 1
        //loadTestTiles();
    }

    public void step(int cycles) {
        int lcdc = memory.readByteUnrestricted(memoryMap.get("LCDC"));
        lcdEnabled = (lcdc & 0x80) != 0;
//        if (!isLcdEnabled()) {
//            memory.writeByteUnrestricted(memoryMap.get("LY"), 0x00);
//            setPpuMode(PPU_MODE.HBLANK);
//            return;
//        }
        int lyAddress = memory.getMemoryMap().get("LY");
        int ly = memory.readByteUnrestricted(lyAddress);

        cycleCounter += cycles;

        if (ly >= 144) {
            // Enter V-Blank mode at LY = 144
            if (ppuMode != PPU_MODE.VBLANK) {
                setPpuMode(PPU_MODE.VBLANK);
                SwingUtilities.invokeLater(display::updateFrame);
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
        int lcdc = memory.readByteUnrestricted(memoryMap.get("LCDC"));
        int scx = memory.readByteUnrestricted(memoryMap.get("SCX"));
        int scy = memory.readByteUnrestricted(memoryMap.get("SCY"));
        int ly = memory.readByteUnrestricted(memoryMap.get("LY"));
        int bgp = memory.readByteUnrestricted(memoryMap.get("BGP"));

        //if (!lcdEnabled || (lcdc & 0x01) == 0) return;

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
            int tileNum = memory.readByteUnrestricted(tileAddress);
            if (tileDataBase == 0x8800) tileNum = (byte) tileNum; //Sign data if in 8800 method
            int tileDataAddress = tileDataBase + (tileNum * 16);

            int tileY = (scy + ly) % 8;
            int tileData1 = memory.readByteUnrestricted(tileDataAddress + (tileY * 2));
            int tileData2 = memory.readByteUnrestricted(tileDataAddress + (tileY * 2) + 1);

            int tileX = (scx + pixel) % 8;

            int colorBitLow = (tileData1 >> 7 - tileX) & 1;
            int colorBitHigh = (tileData2 >> (7 - tileX)) & 1;

            int colorIndex = (colorBitHigh << 1) | colorBitLow;

            int color = (bgp >> (colorIndex * 2)) & 0x03;

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

    public int incrementLY() {
        Map<String, Integer> map = memory.getMemoryMap();
        int LYAddress = map.get("LY");
        int LY = memory.readByteUnrestricted(LYAddress);
        //LY = (LY + 1) % 154;
        LY++;
        if (LY == 154) {
            LY = 0;
            setPpuMode(PPU_MODE.OAM_SCAN);
        }
        memory.writeByteUnrestricted(LYAddress, LY);
        checkLyCoincidence();
        return LY;
    }

    public void printVRAM() {
        System.out.println("-------------------- VRAM (0x8000 - 0x9FFF) --------------------");

        for (int row = 0; row < 96; row++) { // 96 rows of 16 bytes each = 1.5KB (0x8000-0x97FF)
            for (int col = 0; col < 16; col++) {
                int address = 0x8000 + (row * 16) + col;
                int value = memory.readByteUnrestricted(address);

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
                int value = memory.readByteUnrestricted(address);

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
        this.ppuMode = ppuMode;
        switch (ppuMode) {
            case HBLANK:
                memory.setVramBlocked(false);
                memory.setOamBlocked(false);
                break;
            case VBLANK:
                memory.setVramBlocked(false);
                memory.setOamBlocked(false);
                interrupt.setInterrupt(Interrupt.INTERRUPT.VBLANK);
                display.setFrameReady(true);
            case OAM_SCAN:
                memory.setVramBlocked(false);
                memory.setOamBlocked(true);
                break;
            case DRAWING:
                memory.setVramBlocked(true);
                memory.setOamBlocked(true);
                break;
        }
        updateStatRegister();
        checkStatInterrupts();
    }

    private void updateStatRegister() {
        Map<String, Integer> map = memory.getMemoryMap();
        int statAddress = map.get("STAT");
        int stat = memory.readByteUnrestricted(statAddress);

        //Keep bits 7-2, get bits 1-0 from ppuMode enum
        stat = (stat & 0xFC) | ppuMode.ordinal();

        //Set lyc = ly flag (bit 2)
        //Isolates bit 2 and clears it before combining with the boolean status
        stat = (stat & ~0x04) | (lycEqualsLy ? 0x04 : 0x00);

        memory.writeByteUnrestricted(statAddress, stat);
    }

    private void checkStatInterrupts() {
        if (!lcdEnabled) return;

        lastStatInterruptLine = statInterruptLine;
        statInterruptLine = false;

        int stat = memory.readByteUnrestricted(memoryMap.get("STAT"));

        //Check interrupt sources
        if ((stat & 0x40) != 0 && lycEqualsLy) {
            statInterruptLine = true;
        }
        if ((stat & 0x20) != 0 && ppuMode == PPU_MODE.OAM_SCAN) {
            statInterruptLine = true;
        }
        if ((stat & 0x10) != 0 && ppuMode == PPU_MODE.VBLANK) {
            statInterruptLine = true;
        }
        if ((stat & 0x08) != 0 && ppuMode == PPU_MODE.HBLANK) {
            statInterruptLine = true;
        }

        //check rising edge
        if (!lastStatInterruptLine && statInterruptLine) {
            interrupt.setInterrupt(Interrupt.INTERRUPT.STAT);
        }
    }

    private void checkLyCoincidence() {
        int ly = memory.readByteUnrestricted(memoryMap.get("LY"));
        int lyc = memory.readByteUnrestricted(memoryMap.get("LYC"));
        lycEqualsLy = (ly == lyc);
        updateStatRegister();
        checkStatInterrupts();
    }

    public void setCPU(CPU cpu) {
        this.cpu = cpu;
        interrupt = cpu.getInterrupt(); //This is stupid
    }

    private boolean isLcdEnabled() {
        int lcdc = memory.readByteUnrestricted(memoryMap.get("LCDC"));
        return ((lcdc >> 7) & 0x1) == 1;
    }
}
