package org.mochaboy.opcode;

public class Operand {
    private String name;
    private int bytes;
    private boolean immediate;
    private boolean increment;
    private boolean decrement;
    private boolean register;
    private boolean memory;

    public Operand() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }

    public boolean isIncrement() {
        return increment;
    }

    public void setIncrement(boolean increment) {
        this.increment = increment;
    }

    public boolean isDecrement() {
        return decrement;
    }

    public void setDecrement(boolean decrement) {
        this.decrement = decrement;
    }

    public boolean isRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }

    public boolean isMemory() {
        return memory;
    }

    public void setMemory(boolean memory) {
        this.memory = memory;
    }
}
