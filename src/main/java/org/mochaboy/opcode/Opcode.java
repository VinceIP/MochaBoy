package org.mochaboy.opcode;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.operations.MicroOperation;

import java.util.LinkedList;

public class Opcode {
    private final LinkedList<MicroOperation> microOps;
    private String destinationRegister;
    private String sourceRegister;
    private String cc;
    private int operand1;
    private int operand2;
    private int operand3;
    private int incrementOperand = 0;
    private int decrementOperand = 0;
    private boolean operationsRemaining;


    public Opcode() {
        microOps = new LinkedList<>();
    }

    public void execute(CPU cpu, Memory memory) {
        MicroOperation mo = microOps.pop();
        mo.execute(cpu, memory);
        if (microOps.isEmpty()) operationsRemaining = false;
    }

    public void addOp(MicroOperation microOperation) {
        operationsRemaining = true;
        microOps.add(microOperation);
    }

    public int getOperand1() {
        return operand1;
    }

    public void setOperand1(int operand1) {
        this.operand1 = operand1;
    }

    public int getOperand2() {
        return operand2;
    }

    public void setOperand2(int operand2) {
        this.operand2 = operand2;
    }

    public int getOperand3() {
        return operand3;
    }

    public void setOperand3(int operand3) {
        this.operand3 = operand3;
    }

    public LinkedList<MicroOperation> getMicroOps() {
        return microOps;
    }

    public boolean hasOperationsRemaining() {
        return operationsRemaining;
    }

    public void setOperationsRemaining(boolean val) {
        operationsRemaining = val;
    }

    public String getDestinationRegister() {
        return destinationRegister;
    }

    public void setDestinationRegister(String destinationRegister) {
        this.destinationRegister = destinationRegister;
    }

    public String getSourceRegister() {
        return sourceRegister;
    }

    public void setSourceRegister(String sourceRegister) {
        this.sourceRegister = sourceRegister;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public int getIncrementOperand() {
        return incrementOperand;
    }

    public void setIncrementOperand(int incrementOperand) {
        this.incrementOperand = incrementOperand;
    }

    public int getDecrementOperand() {
        return decrementOperand;
    }

    public void setDecrementOperand(int decrementOperand) {
        this.decrementOperand = decrementOperand;
    }
}
