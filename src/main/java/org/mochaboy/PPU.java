package org.mochaboy;

import javafx.application.Platform;
import org.mochaboy.gui.fx.GuiFxDisplay;
import org.mochaboy.registers.Interrupt;

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
    private final GuiFxDisplay display;
    private Interrupt interrupt;

    private CPU cpu;

    public PPU(Memory memory, FrameBuffer frameBuffer, GuiFxDisplay display) {
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
        setPpuMode(PPU_MODE.HBLANK);
        lcdEnabled = false;
        Map<String, Integer> map = memory.getMemoryMap();
        memory.writeByteUnrestricted(map.get("LY"), 0x00);
        memory.writeByteUnrestricted(map.get("LCDC"), 0x0);
        memory.writeByteUnrestricted(map.get("BGP"), 0xFC); //bg palette
        memory.writeByteUnrestricted(map.get("OBP0"), 0xFF); //Sprite palette 0
        memory.writeByteUnrestricted(map.get("OBP1"), 0xFF); //Sprite palette 1
    }

    public void step(int cycles) {

        int lcdc = memory.readByteUnrestricted(memoryMap.get("LCDC"));
        lcdEnabled = isLcdEnabled();
        if (!isLcdEnabled()) {
            // LCD is off – hardware forces LY=0 and STAT mode=HBlank
            memory.writeByteUnrestricted(memoryMap.get("LY"), 0);
            cycleCounter = 0;
            setPpuMode(PPU_MODE.HBLANK);
            return;
        }
        int lyAddress = memory.getMemoryMap().get("LY");
        int ly = memory.readByteUnrestricted(lyAddress);

        cycleCounter += cycles;

        if (ly >= 144) {
            // Enter V-Blank mode at LY = 144
            if (ppuMode != PPU_MODE.VBLANK) {
                setPpuMode(PPU_MODE.VBLANK);
                Platform.runLater(display::updateFrame);
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
        int wy = memory.readByteUnrestricted(memoryMap.get("WY"));
        int wx = memory.readByteUnrestricted(memoryMap.get("WX")) - 7;
        int ly = memory.readByteUnrestricted(memoryMap.get("LY"));
        int bgp = memory.readByteUnrestricted(memoryMap.get("BGP"));
        int obp0 = memory.readByteUnrestricted(memoryMap.get("OBP0"));
        int obp1 = memory.readByteUnrestricted(memoryMap.get("OBP1"));

        int[] lineColors = new int[160];

        int bgTileMapBase = ((lcdc & 0x08) != 0) ? 0x9C00 : 0x9800;
        int winTileMapBase = ((lcdc & 0x40) != 0) ? 0x9c00 : 0x9800;
        int tileDataBase = ((lcdc & 0x10) != 0) ? 0x8000 : 0x9000;

        for (int pixel = 0; pixel < 160; pixel++) {
            boolean window = ((lcdc & 0x20) != 0) && ly >= wy && pixel >= wx;
            int tileMapBase = window ? winTileMapBase : bgTileMapBase;
            int xOffset = window ? (pixel - wx) & 0xFF : (scx + pixel) & 0xFF;
            int yOffset = window ? (ly - wy) & 0xFF : (scy + ly) & 0xFF;

            int tileRow = (yOffset >> 3) & 31;
            int tileCol = (xOffset >> 3) & 31;
            int tileAddress = tileMapBase + tileRow * 32 + tileCol;
            int tileNum = memory.readByteUnrestricted(tileAddress);
            if (tileDataBase == 0x9000) tileNum = (byte) tileNum;
            int tileDataAddress = tileDataBase + tileNum * 16;
            tileDataAddress = 0x8000 | ((tileDataAddress - 0x8000) & 0x1FFF);

            int tileY = yOffset % 8;
            int tileData1 = memory.readByteUnrestricted((tileDataAddress + (tileY * 2)) & 0xFFFF);
            int tileData2 = memory.readByteUnrestricted((tileDataAddress + (tileY * 2) + 1) & 0xFFFF);
            int tileX = xOffset % 8;

            int colorBitLow = (tileData1 >> (7 - tileX)) & 1;
            int colorBitHigh = (tileData2 >> (7 - tileX)) & 1;
            int colorIndex = (colorBitHigh << 1) | colorBitLow;

            int color = (bgp >> (colorIndex * 2)) & 0x03;
            lineColors[pixel] = color;
        }

        int spriteHeight = ((lcdc & 0x04) != 0) ? 16 : 8;
        for (int i = 0; i < 40; i++) {
            int oamAddr = 0xFE00 + i * 4;
            int spriteX = memory.readByteUnrestricted(oamAddr) - 16;
            int spriteY = memory.readByteUnrestricted(oamAddr + 1) - 8;
            int tile = memory.readByteUnrestricted(oamAddr + 2);
            int attr = memory.readByteUnrestricted(oamAddr + 3);

            if (ly < spriteY || ly >= spriteY + spriteHeight) continue;

            int line = ly - spriteY;
            if ((attr & 0x40) != 0) line = spriteHeight - 1 - line;
            if (spriteHeight == 16) {
                tile &= 0xFE;
                if (line >= 8) {
                    tile += 1;
                    line -= 8;
                }
            }

            int tileDataAddress = 0x8000 + tile * 16;
            int tileData1 = memory.readByteUnrestricted(tileDataAddress + line * 2);
            int tileData2 = memory.readByteUnrestricted(tileDataAddress + line * 2 + 1);

            for (int x = 0; x < 8; x++) {
                int pixelX = spriteX + ((attr & 0x20) != 0 ? (7 - x) : x);
                if (pixelX < 0 || pixelX >= 160) continue;

                int low = (tileData1 >> (7 - x)) & 1;
                int high = (tileData2 >> (7 - x)) & 1;
                int index = (high << 1) | low;
                if (index == 0) continue;

                int palette = (attr & 0x10) != 0 ? obp1 : obp0;
                int color = (palette >> (index * 2)) & 0x03;
                lineColors[pixelX] = color;
            }
        }

        for (int pixel = 0; pixel < 160; pixel++) {
            int actualColor = getColor(lineColors[pixel]);
            frameBuffer.setPixel(pixel, ly, actualColor);
        }

    }

    private int getColor(int colorIndex) {
        return switch (colorIndex) {
            case 0 -> 0xFFFFFFFF; // White
            case 1 -> 0xFFC0C0C0; // Light Gray
            case 2 -> 0xFF606060; // Dark Gray
            case 3 -> 0xFF000000; // Black
            default -> 0xFF000000;
        };
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
                memory.setOamBlocked(true);
                break;
            case VBLANK:
                memory.setVramBlocked(false);
                memory.setOamBlocked(false);
                interrupt.setInterrupt(Interrupt.INTERRUPT.VBLANK);
                display.setFrameReady(true);
                break;
            case OAM_SCAN:
                memory.setVramBlocked(false);
                memory.setOamBlocked(true);
                break;
            case DRAWING:
                memory.setVramBlocked(true);
                memory.setOamBlocked(true);
                break;
        }
        checkLyCoincidence();
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

    public boolean isLcdEnabled() {
        int lcdc = memory.readByteUnrestricted(memoryMap.get("LCDC"));
        return ((lcdc >> 7) & 0x1) == 1;
    }
}