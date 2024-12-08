package org.mochaboy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Registers {

    private final Map<String, Supplier<Integer>> registerMap; //Will be needed to dynamically access registers as referenced by OpcodeInfo
    private int A, B, C, D, E, F, H, L;
    private int SP, PC;
    public static final int FLAG_ZERO = 1 << 7;
    public static final int FLAG_SUBTRACT = 1 << 6;
    public static final int FLAG_HALF_CARRY = 1 << 5;
    public static final int FLAG_CARRY = 1 << 4;

    public Registers() {
        registerMap = new HashMap<>();
        registerMap.put("A", () -> A);
        registerMap.put("B", () -> B);
        registerMap.put("C", () -> C);
        registerMap.put("D", () -> D);
        registerMap.put("E", () -> E);
        registerMap.put("F", () -> F);
        registerMap.put("H", () -> H);
        registerMap.put("L", () -> L);
        registerMap.put("SP", () -> SP);
        registerMap.put("PC", () -> PC);
        registerMap.put("AF", this::getAF);
        registerMap.put("BC", this::getBC);
        registerMap.put("DE", this::getDE);
        registerMap.put("HL", this::getHL);
    }

    public int getA() {
        return A;
    }

    public void setA(int a) {
        A = a;
    }

    public int getB() {
        return B;
    }

    public void setB(int b) {
        B = b;
    }

    public int getC() {
        return C;
    }

    public void setC(int c) {
        C = c;
    }

    public int getD() {
        return D;
    }

    public void setD(int d) {
        D = d;
    }

    public int getE() {
        return E;
    }

    public void setE(int e) {
        E = e;
    }

    public int getF() {
        return F;
    }

    public void setF(int f) {
        F = f;
    }

    public int getH() {
        return H;
    }

    public void setH(int h) {
        H = h;
    }

    public int getL() {
        return L;
    }

    public void setL(int l) {
        L = l;
    }

    public int getSP() {
        return SP;
    }

    public void setSP(int SP) {
        this.SP = SP;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public void incrementPC() {
        setPC(getPC() + 1);
    }

    public void incrementPC(int val) {
        setPC(getPC() + val);
    }

    //16-bit combined registers
    public int getAF() { //Accumulator and flags
        return (A << 8) | F;
    }

    public void setAF() {
        System.out.println("setAF unimplemented");
    }

    public int getBC() {
        return (B << 8) | C;
    }

    public void setBC(int val) {
        B = (val >> 8) & 0xFF;
        C = val & 0xFF;
    }

    public int getDE() {
        return (D << 8) | E;
    }

    public void setDE(int val) {
        D = (val >> 8) & 0xFF;
        E = val & 0xFF;
    }

    public int getHL() {
        return (H << 8) | L;
    }

    public void setHL(int val) {
        H = (val >> 8) & 0xFF;
        L = val & 0xFF;
    }

    //Flags

    public boolean isFlagSet(int flag) {
        return (F & flag) != 0;
    }

    public void setFlag(int flag) {
        F |= flag;
    }

    public void clearFlag(int flag) {
        F &= ~flag;
    }

    public int getByName(String name) {
        return registerMap.get(name).get();
    }
}
