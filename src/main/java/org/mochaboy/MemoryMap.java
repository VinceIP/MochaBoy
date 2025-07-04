package org.mochaboy;

import java.util.HashMap;
import java.util.Map;

public class MemoryMap {
    private final Map<String, Integer> map;

    public MemoryMap() {
        this.map = new HashMap<>();
        mapMemory();
    }

    private void mapMemory() {
        map.put("JOYP", 0xFF00);
        map.put("SB", 0xFF01);
        map.put("SC", 0xFF02);
        map.put("DIV", 0xFF04);
        map.put("TIMA", 0xFF05);
        map.put("TMA", 0xFF06);
        map.put("TAC", 0xFF07);
        map.put("IF", 0xFF0F);

        map.put("NR10", 0xFF10);
        map.put("NR11", 0xFF11);
        map.put("NR12", 0xFF12);
        map.put("NR13", 0xFF13);
        map.put("NR14", 0xFF14);
        map.put("NR30", 0xFF1A);
        map.put("NR31", 0xFF1B);
        map.put("NR32", 0xFF1C);
        map.put("NR33", 0xFF1D);
        map.put("NR34", 0xFF1E);
        map.put("NR41", 0xFF20);
        map.put("NR42", 0xFF21);
        map.put("NR43", 0xFF22);
        map.put("NR44", 0xFF23);
        map.put("NR50", 0xFF24);
        map.put("NR51", 0xFF25);
        map.put("NR52", 0xFF26);

        map.put("LCDC", 0xFF40);
        map.put("STAT", 0xFF41);
        map.put("SCY", 0xFF42);
        map.put("SCX", 0xFF43);
        map.put("LY", 0xFF44);
        map.put("LYC", 0xFF45);
        map.put("BGP", 0xFF47);
        map.put("OBP0", 0xFF48);
        map.put("OBP1", 0xFF49);
        map.put("WY", 0xFF4A);
        map.put("WX", 0xFF4B);
        map.put("IE", 0xFFFF);

        map.put("VRAM_START", 0x8000);
        map.put("VRAM_END", 0x9FFF);
        map.put("OAM_START", 0xFE00);
        map.put("OAM_END", 0xFE9F);
    }

    public Map<String, Integer> getMap() {
        return map;
    }
}
