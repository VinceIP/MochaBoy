package org.mochaboy;

import java.util.HashMap;
import java.util.Map;

public class MemoryMap {
    private final Map<String, Integer> map;

    public MemoryMap() {
        this.map = new HashMap<>();
        mapMemory();
    }

    private void mapMemory(){
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
    }

    public Map<String, Integer> getMap() {
        return map;
    }
}
