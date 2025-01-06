package org.mochaboy.opcode;

import org.mochaboy.CPU;
import org.mochaboy.DataType;
import org.mochaboy.Memory;
import org.mochaboy.opcode.operations.MicroOperation;

import java.util.LinkedList;

public class Opcode {
    private final LinkedList<MicroOperation> microOps;
    private OpcodeInfo opcodeInfo;
    private String cc;
    private String destinationOperandString;
    private String sourceOperandString;
    private String extraOperandString;
    private String opcodeHex;
    private Operand destinationOperand;
    private Operand sourceOperand;
    private Operand extraOperand;
    private DataType sourceType;
    private DataType destinationType;
    private int fetchedAt;
    private int sourceValue;
    private int destinationValue;
    private int extraValue;
    private int cyclesConsumed;
    private boolean increment;
    private boolean decrement;
    private boolean operationsRemaining;
    private boolean unimplError = false;


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

    public OpcodeInfo getOpcodeInfo() {
        return opcodeInfo;
    }

    public void setOpcodeInfo(OpcodeInfo opcodeInfo) {
        this.opcodeInfo = opcodeInfo;
    }

    public int getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(int fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public boolean isUnimplError() {
        return unimplError;
    }

    public void setUnimplError(boolean unimplError) {
        this.unimplError = unimplError;
    }

    public int getCyclesConsumed() {
        return cyclesConsumed;
    }

    public void setCyclesConsumed(int cyclesConsumed) {
        this.cyclesConsumed = cyclesConsumed;
    }

    public String getOpcodeHex() {
        return opcodeHex;
    }

    public void setOpcodeHex(String opcodeHex) {
        this.opcodeHex = opcodeHex;
    }

    public DataType getSourceType() {
        return sourceType;
    }

    public void setSourceType(DataType sourceType) {
        this.sourceType = sourceType;
    }

    public DataType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(DataType destinationType) {
        this.destinationType = destinationType;
    }

    public String toString() {
        Operand[] o = opcodeInfo.getOperands();
        String mnemonic = opcodeInfo.getMnemonic();
        if (unimplError) mnemonic = "unimplemented " + mnemonic;
        String hs = getOpcodeHex();
        String fs = String.format("%04X", fetchedAt);
        String ds = String.format("%04X", destinationValue);
        String ss = String.format("%04X", sourceValue);

        boolean destinationIsImmediate;
        boolean sourceIsImmediate = false;

        boolean destinationIsDec;
        boolean sourceIsDec = false;
        boolean destinationIsInc;
        boolean sourceIsInc = false;

        destinationIsImmediate = o[0].isImmediate();
        destinationIsDec = o[0].isDecrement();
        destinationIsInc = o[0].isIncrement();

        if (o.length > 1) {
            sourceIsImmediate = o[1].isImmediate();
            sourceIsDec = o[1].isDecrement();
            sourceIsInc = o[1].isIncrement();
        }

        StringBuilder sb = new StringBuilder();

        sb.append(fs).append(": ").append(hs).append(" ").append(mnemonic).append(" ");
        if (!destinationIsImmediate) sb.append("[");
        sb.append(o[0].getName());
        if (destinationIsDec) sb.append("-");
        if (destinationIsInc) sb.append("+");
        if (!destinationIsImmediate) sb.append("]");
        sb.append("(").append(ds).append(") ");

        if (o.length > 1) {
            if (!sourceIsImmediate) sb.append("[");
            sb.append(o[1].getName());
            if (sourceIsDec) sb.append("-");
            if (sourceIsInc) sb.append("+");
            if (!sourceIsImmediate) sb.append("]");
            sb.append("(").append(ss).append(") ");
        }
        return sb.toString();
    }

    public String toString(boolean showCyclesConsumed){
        return showCyclesConsumed ? toString().concat(" cycles: " + getCyclesConsumed()) : toString();
    }
}
