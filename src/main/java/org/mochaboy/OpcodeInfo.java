package org.mochaboy;

public class OpcodeInfo {
    private boolean prefixed;
    private boolean cbprefixed;
    private int opcode;
    private String mnemonic;
    private int bytes;
    private int[] cycles;
    private Operand[] operands;
    private boolean immediate;
    private Flags flags;

    public OpcodeInfo() {
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

    public int[] getCycles() {
        return cycles;
    }

    public Operand[] getOperands() {
        return operands;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public Flags getFlags() {
        return flags;
    }

    public boolean isCbprefixed() {
        return cbprefixed;
    }

    public void setPrefixed(boolean prefixed) {
        this.prefixed = prefixed;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public void setCycles(int[] cycles) {
        this.cycles = cycles;
    }

    public void setOperands(Operand[] operands) {
        this.operands = operands;
    }

    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    public void setCbprefixed(boolean cbprefixed) {
        this.cbprefixed = cbprefixed;
    }
}
