package org.mochaboy;

public class Stack {
    private CPU cpu;

    public Stack(CPU cpu) {
        this.cpu = cpu;
    }

    public void push(int value) {
        int sp = (cpu.getRegisters().getSP() - 2) & 0xFFFF;
        cpu.getRegisters().setSP(sp);
        cpu.getMemory().writeWord(sp, value & 0xFFFF);
    }

    public int pop() {
        int sp = cpu.getRegisters().getSP();
        int value = cpu.getMemory().readWord(sp);
        sp = (sp + 2) & 0xFFFF;
        cpu.getRegisters().setSP(sp);
        return value & 0xFFFF;
    }

    public int peek() {
        int sp = cpu.getRegisters().getSP();
        return cpu.getMemory().readWord(sp);
    }
}
