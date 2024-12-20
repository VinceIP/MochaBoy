package org.mochaboy;

import java.util.LinkedList;

public class Stack {
    private CPU cpu;
    private LinkedList<Integer> debugStack;

    public Stack(CPU cpu) {
        this.cpu = cpu;
        debugStack = new LinkedList<>();
    }

    public void push(int value) {
        int sp = cpu.getRegisters().getSP();

        // Push high byte first
        sp = (sp - 1) & 0xFFFF;
        cpu.getMemory().writeByte(sp, (value >> 8) & 0xFF);
        debugStack.push((value >> 8) & 0xFF);

        // Then push low byte
        sp = (sp - 1) & 0xFFFF;
        cpu.getMemory().writeByte(sp, value & 0xFF);
        debugStack.push(value & 0xFF);

        cpu.getRegisters().setSP(sp);
    }

    public int pop() {
        int sp = cpu.getRegisters().getSP();

        // Read low byte first
        int lowByte = cpu.getMemory().readByte(sp);
        int poppedLow = debugStack.pop();
        if (poppedLow != (lowByte & 0xFF)) {
            System.out.println("STACK ERROR: Popped low byte does not match.");
        }

        sp = (sp + 1) & 0xFFFF; // Increment SP to point to high byte

        // Read high byte
        int highByte = cpu.getMemory().readByte(sp);
        int poppedHigh = debugStack.pop();
        if (poppedHigh != (highByte & 0xFF)) {
            System.out.println("STACK ERROR: Popped high byte does not match.");
        }

        sp = (sp + 1) & 0xFFFF; // Increment SP again to point to the next value on the stack
        cpu.getRegisters().setSP(sp);

        return ((highByte << 8) | lowByte) & 0xFFFF;
    }

    public int peek() {
        int sp = cpu.getRegisters().getSP();
        return cpu.getMemory().readWord(sp);
    }

    public String getDebugStackContents() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < debugStack.size(); i++) {
            sb.append(i).append(": 0x").append(String.format("%02X", debugStack.get(i))).append(" ");
        }
        return sb.toString();
    }
}
