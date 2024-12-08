package org.mochaboy;

import java.util.Map;

public class OpcodeHandler {
    private Map<Integer, OpcodeInfo> opcodeMap;

    public OpcodeHandler(Map<Integer,OpcodeInfo> opcodeMap){
        this.opcodeMap = opcodeMap;
    }

    public void execute(OpcodeInfo opcode){
        //do logic on cpu
        //inc PC
        //add to cpu timer
    }
}

