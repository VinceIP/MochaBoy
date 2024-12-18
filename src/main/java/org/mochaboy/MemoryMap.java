package org.mochaboy;

import java.util.HashMap;
import java.util.Map;

public class MemoryMap {
    private final Map<String, Integer> map;

    public MemoryMap() {
        this.map = new HashMap<>();
    }

    private void mapMemory(){
        map.put("LY", 0xFF44);
    }

    public Map<String, Integer> getMap() {
        return map;
    }
}
