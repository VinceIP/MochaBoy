package org.mochaboy;

import java.util.Map;

public class OpcodeHandler {
    private Map<Integer, OpcodeInfo> opcodeMap;

    public OpcodeHandler(Map<Integer, OpcodeInfo> opcodeMap) {
        this.opcodeMap = opcodeMap;
    }

    public void execute(CPU cpu, OpcodeInfo opcode) {
        //do logic on cpu
        //inc PC based on opcode info
        //add to cpu timer based on opcode info
        //consider increasing t states during specific operations (reading and writing) for accuracy instead?
    }

    /**
     * Add carry - Add value of y plus carry flag to x
     *
     * @param cpu
     * @param opcode
     */
    private void ADC(CPU cpu, OpcodeInfo opcode) {
        //No need to get an xOpr - ADC's x operand is always A, so we alays know where to get the value
        Operand yOpr = opcode.getOperands()[1];
        int xVal = cpu.getRegisters().getA() & 0xFF;
        int yVal;
        if(yOpr.getName().equals("n8")) yVal = cpu.getRegisters().getPC();
        else yVal = cpu.getRegisters().getByName(yOpr.getName()) & 0xFF;
        int result = yVal + xVal & 0xFF;
        cpu.getRegisters().setA(result);
    }


}

