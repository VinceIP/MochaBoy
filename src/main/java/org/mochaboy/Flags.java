package org.mochaboy;

public class Flags {
    private String Z;
    private String N;
    private String H;
    private String C;

    public Flags() {
    }

    public String getZ() {
        return Z;
    }

    public void setZ(String z) {
        Z = z;
    }

    public String getN() {
        return N;
    }

    public void setN(String n) {
        N = n;
    }

    public String getH() {
        return H;
    }

    public void setH(String h) {
        H = h;
    }

    public String getC() {
        return C;
    }

    public void setC(String c) {
        C = c;
    }

    public static boolean CheckFlagsByChar(CPU cpu, char flagString) {
        switch (flagString) {
            case 'Z':
                return cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
            case 'N':
                return cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT);
            case 'H':
                return cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY);
            case 'C':
                return cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
            default:
                return false;
        }
    }
}
