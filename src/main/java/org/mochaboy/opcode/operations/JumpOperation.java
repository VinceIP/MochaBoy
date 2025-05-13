package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;

import java.util.function.Supplier;

public class JumpOperation implements MicroOperation{

    private final Type type;
    private final Supplier<Integer> address;
    private final Opcode opcode;

    public JumpOperation(Type type, Opcode opcode, Supplier<Integer> address){
        this.type = type;
        this. opcode = opcode;
        this.address = address;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        switch (type){
            case CALL -> {

            }
        }
        return this;
    }

    @Override
    public int getCycles() {
        return 0;
    }

    @Override
    public int getResult() {
        return 0;
    }

    public enum Type{
        CALL,
        JP,
        JR,
        RET,
        RETI,
        RST
    }
}
