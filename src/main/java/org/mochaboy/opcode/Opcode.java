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
    private String destinationOperandString;
    private String sourceOperandString;
    private String extraOperandString;
    private Operand destinationOperand;
    private Operand sourceOperand;
    private Operand extraOperand;
    private int sourceValue;
    private int destinationValue;
    private int extraValue;
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

    public String getDestinationOperandString() {
        return destinationOperandString;
    }

    public void setDestinationOperandString(String destinationOperandString) {
        this.destinationOperandString = destinationOperandString;
    }

    public String getSourceOperandString() {
        return sourceOperandString;
    }

    public void setSourceOperandString(String sourceOperandString) {
        this.sourceOperandString = sourceOperandString;
    }

    public String getExtraOperandString() {
        return extraOperandString;
    }

    public void setExtraOperandString(String extraOperandString) {
        this.extraOperandString = extraOperandString;
    }

    public int getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(int sourceValue) {
        this.sourceValue = sourceValue;
    }

    public int getDestinationValue() {
        return destinationValue;
    }

    public void setDestinationValue(int destinationValue) {
        this.destinationValue = destinationValue;
    }

    public int getExtraValue() {
        return extraValue;
    }

    public void setExtraValue(int extraValue) {
        this.extraValue = extraValue;
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

    public Operand getDestinationOperand() {
        return destinationOperand;
    }

    public void setDestinationOperand(Operand destinationOperand) {
        this.destinationOperand = destinationOperand;
    }

    public Operand getSourceOperand() {
        return sourceOperand;
    }

    public void setSourceOperand(Operand sourceOperand) {
        this.sourceOperand = sourceOperand;
    }

    public Operand getExtraOperand() {
        return extraOperand;
    }

    public void setExtraOperand(Operand extraOperand) {
        this.extraOperand = extraOperand;
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
