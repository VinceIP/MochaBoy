package org.mochaboy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RrOperationTest {

    private Cartridge cartridge;
    private Memory memory;
    private CPU cpu;
    private OpcodeLoader opcodeLoader;
    private OpcodeHandler opcodeHandler;
    private OpcodeWrapper opcodeWrapper;

    @BeforeEach
    void setUp() throws IOException {
        cartridge = new Cartridge(new File("./././Tetris.gb").toPath());
        memory = new Memory(cartridge);
        cpu = new CPU(memory);
        opcodeLoader = new OpcodeLoader();
        opcodeWrapper = opcodeLoader.getOpcodeWrapper();
        opcodeHandler = new OpcodeHandler(opcodeWrapper);
    }

    @Test
    void testRR_A_LSBSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x1F"); // RR A

        cpu.getRegisters().setA(0xB5); // 10110101
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xDA, cpu.getRegisters().getA()); // 10110101 >> 1 with carry in becomes 11011010 (0xDA)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b0 = 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is not zero
    }

    @Test
    void testRR_B_LSBNotSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x18"); // RR B

        cpu.getRegisters().setB(0x34); // 00110100
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1A, cpu.getRegisters().getB()); // 00110100 >> 1 with no carry becomes 00011010 (0x1A)
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b0 = 0
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is not zero
    }

    @Test
    void testRR_C_ResultIsZero() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x19"); // RR C

        cpu.getRegisters().setC(0x00); // 00000000
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getC()); // Rotating 0 gives 0
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b0 = 0
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
    }

    @Test
    void testRR_D_AllOnes() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x1A"); // RR D

        cpu.getRegisters().setD(0xFF); // 11111111
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x7F, cpu.getRegisters().getD()); // 11111111 >> 1 becomes 01111111 (0x7F)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b0 = 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is not zero
    }

    @Test
    void testRR_HL_Memory() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x1E"); // RR (HL)

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x01); // 00000001
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        int memoryValue = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        assertEquals(0x00, memoryValue); // 00000001 >> 1 becomes 00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b0 = 1
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
    }

    @Test
    void testRR_H_ResultZeroWithCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x1C"); // RR H

        cpu.getRegisters().setH(0x01); // 00000001
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getH()); // 00000001 >> 1 becomes 00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b0 = 1
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
    }
}
