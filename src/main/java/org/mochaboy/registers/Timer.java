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
        memory.writeByte(map.get("DIV"), div & 0xFF);
    }

    public void resetDiv() {
        memory.writeByte(map.get("DIV"), 0x00);
    }

    public void incTima() {
        int tima = getTima();
        if (tima == 0xFF) {
            resetTima(0x00);
            overflowDelay = 1;
        } else {
            memory.writeByte(map.get("TIMA"), (tima + 1) & 0xFF);
        }
    }

    public int getTima() {
        return memory.readByte(map.get("TIMA"));
    }

    public void resetTima(int value) {
        memory.writeByte(map.get("TIMA"), value & 0xFF);
    }

    public int getTma() {
        return memory.readByte(map.get("TMA"));
    }

    public int getTac() {
        return memory.readByte(map.get("TAC"));
    }


    public boolean isTacEnabled() {
        return (getTac() & 0x4) != 0;
    }

    public int getTacPeriod() {
        int tac = getTac() & 0x03;
        return switch (tac) {
            case 0x00 -> 1024;
            case 0x01 -> 16;
            case 0x02 -> 64;
            case 0x03 -> 256;
            default -> 0;
        };
    }

    public void update(int cycles) {
        if (overflowDelay > 0) {
            overflowDelay -= cycles;
            if (overflowDelay <= 0) {
                resetTima(getTma());
                interrupt.setInterrupt(Interrupt.INTERRUPT.TIMER);
                overflowDelay = 0;
            }
        }
    }

    public int getDiv() {
        return memory.readByte(map.get("DIV"));
    }
}
