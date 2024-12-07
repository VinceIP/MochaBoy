package org.mochaboy;

public class OpcodeInfo {
    private final boolean prefixed;
    private final int opcode;
    private final String mnemonic;
    private final int bytes;
    private final int cycles;
    private final Operand[] operands;
    private final boolean immediate;
    private final String[] flags;

    public OpcodeInfo(boolean prefixed, int opcode, String mnemonic, int bytes, int cycles, Operand[] operands, boolean immediate, String[] flags) {
        this.prefixed = prefixed;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.bytes = bytes;
        this.cycles = cycles;
        this.operands = operands;
        this.immediate = immediate;
        this.flags = flags;
    }

    public boolean isPrefixed() {
        return prefixed;
    }

    public int getOpcode() {
        return opcode;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public int getBytes() {
        return bytes;
    }

    public int getCycles() {
        return cycles;
    }

    public Operand[] getOperands() {
        return operands;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public String[] getFlags() {
        return flags;
    }
}
