package org.mochaboy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Registers {

    private final Map<String, Supplier<Integer>> registerGettersMap; //Will be needed to dynamically access registers as referenced by OpcodeInfo
    private final Map<String, BiConsumer<Registers, Integer>> registerSettersMap;
    private int A, B, C, D, E, F, H, L;
    private int SP, PC;
    public static final int FLAG_ZERO = 1 << 7;
    public static final int FLAG_SUBTRACT = 1 << 6;
    public static final int FLAG_HALF_CARRY = 1 << 5;
    public static final int FLAG_CARRY = 1 << 4;

    public Registers() {
        registerGettersMap = new HashMap<>();
        registerGettersMap.put("A", () -> A);
        registerGettersMap.put("B", () -> B);
        registerGettersMap.put("C", () -> C);
        registerGettersMap.put("D", () -> D);
        registerGettersMap.put("E", () -> E);
        registerGettersMap.put("F", () -> F);
        registerGettersMap.put("H", () -> H);
        registerGettersMap.put("L", () -> L);
        registerGettersMap.put("SP", () -> SP);
        registerGettersMap.put("PC", () -> PC);
        registerGettersMap.put("AF", this::getAF);
        registerGettersMap.put("BC", this::getBC);
        registerGettersMap.put("DE", this::getDE);
        registerGettersMap.put("HL", this::getHL);

        registerSettersMap = new HashMap<>();
        registerSettersMap.put("A", Registers::setA);
        registerSettersMap.put("B", Registers::setB);
        registerSettersMap.put("C", Registers::setC);
        registerSettersMap.put("D", Registers::setD);
        registerSettersMap.put("E", Registers::setE);
        registerSettersMap.put("H", Registers::setH);
        registerSettersMap.put("L", Registers::setL);
        registerSettersMap.put("SP", Registers::setSP);
        registerSettersMap.put("PC", Registers::setPC);
        registerSettersMap.put("AF", Registers::setAF);
        registerSettersMap.put("BC", Registers::setBC);
        registerSettersMap.put("DE", Registers::setDE);
        registerSettersMap.put("HL", Registers::setHL);

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

    public int getF(){
        return F;
    }

    public void setF(int value){
        F = (value & 0xF0);
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

    public void setAF(int val) {
        A = (val >> 8) & 0xFF;
        F = val & 0xF0; // Mask out the lower nibble
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

    public void setFlag(int flag, boolean condition) {
        F = (condition) ? (F | flag) & 0xF0 : (F & ~flag) & 0xF0;
    }

    public void clearFlag(int flag) {
        F &= ~flag;
    }

    public int getByName(String name) {
        if(registerGettersMap.get(name).get() == null) throw new NullPointerException("Invalid register name: " + name );
        return registerGettersMap.get(name).get();
    }

    public void setByName(String name, int value) {
        registerSettersMap.get(name).accept(this, value);
    }

}
