package org.mochaboy;

import java.util.Map;

public class Input {
    private Memory memory;
    private Map<String, Integer> map;

    public Input(Memory memory) {
        this.memory = memory;
        map = memory.getMemoryMap();

        init();
    }

    private void init() {
        memory.writeByte(map.get("JOYP"), 0xCF);
    }

}
