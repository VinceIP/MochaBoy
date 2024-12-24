package org.mochaboy.registers;

import org.mochaboy.Memory;

import java.util.Map;

public class Interrupt {

    private Memory memory;

    private final Map<String, Integer> map;

    public Interrupt(Memory memory) {
        this.memory = memory;
        map = this.memory.getMemoryMap();
    }

    public void setInterrupt(INTERRUPT interrupt) {
        int bit = interrupt.ordinal();
        int IF = memory.readByte(map.get("IF"));
        IF = IF | (1 << bit);
        memory.writeByte(map.get("IF"), IF);
    }

    public enum INTERRUPT {
        VBLANK,
        LCD,
        TIMER,
        SERIAL,
        JOYPAD
    }
}
