package org.mochaboy;

public class CPU {

    private Memory memory;
    private Registers registers;

    public CPU(Memory memory) {
        this.memory = memory;
        this.registers = new Registers();
    }

    private void init() {

    }

    public void run() {
        try {
            while (registers.getPC() < memory.getMemoryLength()) {
                int opcode = (fetch() & 0xFF);
                execute(opcode);
            }
        } catch (OutOfMemoryError e) {
            System.out.println("Out of mem error: " + e.getMessage());
        }

    }

    public byte fetch() {
        return memory.readByte(registers.getPC());
    }

    public void execute(int opcode) {
        Opcode.execute(this, opcode);
    }

    public Memory getMemory() {
        return memory;
    }

    public Registers getRegisters() {
        return registers;
    }

}
