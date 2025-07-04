import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.FrameBuffer;
import org.mochaboy.Memory;
import org.mochaboy.PPU;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PPUTest {
    private Memory memory;
    private FrameBuffer frameBuffer;
    private PPU ppu;

    @BeforeEach
    void setUp() {
        memory = new Memory();
        frameBuffer = new FrameBuffer(160, 144);
        ppu = new PPU(memory, frameBuffer, null);
        memory.setPpu(ppu);
    }

    @Test
    void testSpritePriority() {
        Map<String, Integer> map = memory.getMemoryMap();
        memory.writeByteUnrestricted(map.get("LCDC"), 0x83); // LCD on, BG and sprites enabled
        memory.writeByteUnrestricted(map.get("SCY"), 0);
        memory.writeByteUnrestricted(map.get("SCX"), 0);
        memory.writeByteUnrestricted(map.get("BGP"), 0xE4);
        memory.writeByteUnrestricted(map.get("OBP0"), 0xE4);
        memory.writeByteUnrestricted(map.get("LY"), 0);

        for (int row = 0; row < 8; row++) {
            // tile 0 - white background
            memory.writeByteUnrestricted(0x8000 + row * 2, 0x00);
            memory.writeByteUnrestricted(0x8000 + row * 2 + 1, 0x00);
            // tile 1 - sprite (light gray)
            memory.writeByteUnrestricted(0x8010 + row * 2, 0xFF);
            memory.writeByteUnrestricted(0x8010 + row * 2 + 1, 0x00);
            // tile 2 - black background
            memory.writeByteUnrestricted(0x8020 + row * 2, 0xFF);
            memory.writeByteUnrestricted(0x8020 + row * 2 + 1, 0xFF);
        }

        memory.writeByteUnrestricted(0x9800, 0x02); // first tile black
        memory.writeByteUnrestricted(0x9801, 0x00); // second tile white

        // Sprite over black background (hidden) at x=0
        memory.writeByteUnrestricted(0xFE00, 16);
        memory.writeByteUnrestricted(0xFE01, 8);
        memory.writeByteUnrestricted(0xFE02, 1);
        memory.writeByteUnrestricted(0xFE03, 0x80); // behind BG

        // Sprite over white background (visible) at x=8
        memory.writeByteUnrestricted(0xFE04, 16);
        memory.writeByteUnrestricted(0xFE05, 16);
        memory.writeByteUnrestricted(0xFE06, 1);
        memory.writeByteUnrestricted(0xFE07, 0x80); // behind BG

        ppu.drawScanline();

        assertEquals(0xFF000000, frameBuffer.getPixel(0, 0));
        assertEquals(0xFFC0C0C0, frameBuffer.getPixel(8, 0));
    }

    @Test
    void testWindowPosition() {
        Map<String, Integer> map = memory.getMemoryMap();
        memory.writeByteUnrestricted(map.get("LCDC"), 0xE1); // LCD on, BG+Window
        memory.writeByteUnrestricted(map.get("SCY"), 0);
        memory.writeByteUnrestricted(map.get("SCX"), 0);
        memory.writeByteUnrestricted(map.get("BGP"), 0xE4);
        memory.writeByteUnrestricted(map.get("WY"), 2);
        memory.writeByteUnrestricted(map.get("WX"), 10); // window starts at x=3
        memory.writeByteUnrestricted(map.get("LY"), 2);

        for (int row = 0; row < 8; row++) {
            // tile 0 - white
            memory.writeByteUnrestricted(0x8000 + row * 2, 0x00);
            memory.writeByteUnrestricted(0x8000 + row * 2 + 1, 0x00);
            // tile 2 - black for window
            memory.writeByteUnrestricted(0x8020 + row * 2, 0xFF);
            memory.writeByteUnrestricted(0x8020 + row * 2 + 1, 0xFF);
        }

        memory.writeByteUnrestricted(0x9800, 0x00); // BG uses white tile
        for (int i = 0; i < 32; i++) {
            memory.writeByteUnrestricted(0x9C00 + i, 0x02); // window uses black tile
        }

        ppu.drawScanline();

        assertEquals(0xFFFFFFFF, frameBuffer.getPixel(0, 2));
        assertEquals(0xFF000000, frameBuffer.getPixel(3, 2));
    }
}