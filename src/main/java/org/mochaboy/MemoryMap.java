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
        map.put("DIV", 0xFF04);
        map.put("TIMA", 0xFF05);
        map.put("TMA", 0xFF06);
        map.put("TAC", 0xFF07);
        map.put("IF", 0xFF0F);
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
    }

    public Map<String, Integer> getMap() {
        return map;
    }
}
