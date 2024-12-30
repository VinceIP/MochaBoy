package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;

public interface MicroOperation {
    MicroOperation execute(CPU cpu, Memory memory);

    int getCycles();
    int getResult();
}
