package org.mochaboy.registers;

import org.mochaboy.Memory;

import java.util.Map;

public class Timer {

    public final static int DIV_INC_TIME_NS = 61035;

    private final Memory memory;
    private final Interrupt interrupt;
    private final Map<String, Integer> map;

    public Timer(Memory memory, Interrupt interrupt) {
        this.memory = memory;
        this.interrupt = interrupt;
        map = this.memory.getMemoryMap();

    }

    public void incDiv() {
        byte div = (byte) ((byte) getDiv() & 0xFF);
        int address = map.get("DIV");
        div++;
        memory.getMemoryArray()[address] = div;
    }

    public void resetDiv() {
        memory.writeByte(map.get("DIV"), 0x00);
    }

    public void incTima() {
        int tima = getTima();
        if (tima == 0xFF) {
            resetTima(getTma());
            //Request interrupt
            interrupt.setInterrupt(Interrupt.INTERRUPT.TIMER);
        }
        memory.writeByte(map.get("TIMA"), tima + 1 & 0xFF);
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
        return (getTac() >> 2) != 0;
    }

    public int getTacFreq() {
        int tac = getTac() & 0x03;
        int tacFreq = 0;
        switch (tac) {
            case 0x00:
                tacFreq = 256;
                break;
            case 0x01:
                tacFreq = 4;
                break;
            case 0x02:
                tacFreq = 16;
                break;
            case 0x03:
                tacFreq = 64;
                break;
        }
        return tacFreq;
    }

    public int getDiv() {
        return memory.readByte(map.get("DIV"));
    }
}
