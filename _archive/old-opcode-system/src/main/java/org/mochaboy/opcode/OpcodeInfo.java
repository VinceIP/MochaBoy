package org.mochaboy.opcode;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\nopcode=0x%02X, ", opcode));
        sb.append("\nmnemonic=").append(mnemonic).append(", ");
        sb.append("\nprefixed=").append(prefixed).append(", ");
        sb.append("\ncbprefixed=").append(cbprefixed).append(", ");
        sb.append(String.format("\nbytes=0x%02X, ", bytes));
        sb.append("\ncycles=[");
        if (cycles != null) {
            for (int i = 0; i < cycles.length; i++) {
                sb.append(String.format("0x%02X", cycles[i]));
                if (i < cycles.length - 1) sb.append(", ");
            }
        }
        sb.append("], \noperands=");
        if (operands != null) {
            for (Operand op : operands) sb.append(op.getName()).append(" ");
        }
        sb.append(", \nimmediate=").append(immediate).append(", ");
        sb.append("\nflags=").append(flags.toString());
        return sb.toString();
    }

}
