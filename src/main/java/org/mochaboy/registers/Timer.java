package org.mochaboy.registers;

import org.mochaboy.Memory;

import java.util.Map;

public class Timer {

    public final static int DIV_INC_TIME_NS = 61035;

    private final Memory memory;
    private final Interrupt interrupt;
    private final Map<String, Integer> map;
    private int overflowDelay = 0;

    public Timer(Memory memory, Interrupt interrupt) {
        this.memory = memory;
        this.interrupt = interrupt;
        map = this.memory.getMemoryMap();

    }

    public void incDiv() {
        int div = getDiv() & 0xFF;
        int address = map.get("DIV");
        div++;
        memory.writeByteUnrestricted(address, div & 0xFF);
    }

    public void resetDiv() {
        memory.writeByteUnrestricted(map.get("DIV"), 0x00);
    }

    public void incTima() {
        int tima = getTima();
        if (tima == 0xFF) {
            resetTima(0x00);
            overflowDelay = 1;
        } else {
            memory.writeByteUnrestricted(map.get("TIMA"), (tima + 1) & 0xFF);
        }
    }

    public int getTima() {
        return memory.readByteUnrestricted(map.get("TIMA"));
    }

    public void resetTima(int value) {
        memory.writeByteUnrestricted(map.get("TIMA"), value & 0xFF);
    }

    public int getTma() {
        return memory.readByteUnrestricted(map.get("TMA"));
    }

    public int getTac() {
        return memory.readByteUnrestricted(map.get("TAC"));
    }


    public boolean isTacEnabled() {
        return (getTac() & 0b100) != 0;
    }

    public int getTacRate() {
        int tac = getTac() & 0x03;
        return switch (tac) {
            //In m-cycles
            case 0x00 -> 256;
            case 0x01 -> 4;
            case 0x02 -> 16;
            case 0x03 -> 64;
            default -> 0;
        };
    }


    public int getDiv() {
        return memory.readByteUnrestricted(map.get("DIV"));
    }
}
