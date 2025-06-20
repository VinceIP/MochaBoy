package org.mochaboy.registers;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.HashMap;
import java.util.Map;

public class Interrupt {

    private CPU cpu;
    private Memory memory;

    private final Map<String, Integer> map;
    private final Map<String, Integer> interruptMap;

    public Interrupt(CPU cpu, Memory memory) {
        this.cpu = cpu;
        this.memory = memory;
        map = this.memory.getMemoryMap();
        interruptMap = new HashMap<>();
        populateInterruptMap();
    }

    private void populateInterruptMap() {
        interruptMap.put("VBLANK", 0x0040);
        interruptMap.put("STAT", 0x0048);
        interruptMap.put("TIMER", 0x0050);
        interruptMap.put("SERIAL", 0x0058);
        interruptMap.put("JOYPAD", 0x0060);
    }

    public void setInterrupt(INTERRUPT interrupt) {
        int bit = interrupt.ordinal();
        int IF = memory.readByte(map.get("IF"));
        IF = (IF | (1 << bit));
        memory.writeByte(map.get("IF"), IF);
        cpu.setHalt(false);
    }

    public void handleInterrupt(INTERRUPT interrupt) {
        cpu.getStack().push(cpu.getRegisters().getPC());
        cpu.getRegisters().setPC(interruptMap.get(interrupt.name()));
        cpu.setIME(false);
    }

    public enum INTERRUPT {
        VBLANK,
        STAT,
        TIMER,
        SERIAL,
        JOYPAD
    }

    public String getInterruptsAsString() {
        int IF = memory.readByte(map.get("IF"));
        StringBuilder sb = new StringBuilder();
        sb.append("Interrupts: ");
        for (int i = 0; i < 5; i++) {
            sb.append("\n");
            if ((IF >> i) != 0) {
                switch (i) {
                    case 0:
                        sb.append("vblank");
                        break;
                    case 1:
                        sb.append("stat");
                        break;
                    case 2:
                        sb.append("timer");
                        break;
                    case 3:
                        sb.append("serial");
                        break;
                    case 4:
                        sb.append("joypad");
                        break;
                }
            } else sb.append("------");
        }
        return sb.toString();
    }
}
