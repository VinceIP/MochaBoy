package org.mochaboy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RotateOperationTest {

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
    void testRLA() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x17"); // RLA

        cpu.getRegisters().setA(0x85); // 10000101
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0B, cpu.getRegisters().getA()); // 10000101 rotated becomes 00001011
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b7 = 1
    }

    @Test
    void testRLCA() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x07"); // RLCA

        cpu.getRegisters().setA(0x85); // 10000101

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0B, cpu.getRegisters().getA()); // 10000101 rotated becomes 00001011
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b7 = 1
    }

    @Test
    void testRRA() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x1F"); // RRA

        cpu.getRegisters().setA(0x85); // 10000101
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xC2, cpu.getRegisters().getA()); // 10000101 >> 1 with carry in becomes 11000010
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b0 = 1
    }

    @Test
    void testRRCA() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x0F"); // RRCA

        cpu.getRegisters().setA(0x85); // 10000101

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xC2, cpu.getRegisters().getA()); // 10000101 >> 1 becomes 11000010
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b0 = 1
    }

    @Test
    void testRL_r8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x10"); // RL B

        cpu.getRegisters().setB(0x85); // 10000101
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, false);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0A, cpu.getRegisters().getB()); // 10000101 rotated becomes 00001010
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b7 = 1
    }

    @Test
    void testRLC_r8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x00"); // RLC B

        cpu.getRegisters().setB(0x85); // 10000101

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0B, cpu.getRegisters().getB()); // 10000101 rotated becomes 00001011
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b7 = 1
    }

    @Test
    void testRR_r8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x18"); // RR B

        cpu.getRegisters().setB(0x85); // 10000101
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xC2, cpu.getRegisters().getB()); // 10000101 >> 1 with carry in becomes 11000010
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b0 = 1
    }

    @Test
    void testRRC_r8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x08"); // RRC B

        cpu.getRegisters().setB(0x85); // 10000101

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xC2, cpu.getRegisters().getB()); // 10000101 >> 1 becomes 11000010
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b0 = 1
    }

    @Test
    void testRL_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x16"); // RL (HL)

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x85); // 10000101
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, false);

        opcodeHandler.execute(cpu, opcodeInfo);

        int memoryValue = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        assertEquals(0x0A, memoryValue); // 10000101 rotated becomes 00001010
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b7 = 1
    }

    @Test
    void testRR_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x1E"); // RR (HL)

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x85); // 10000101
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);

        opcodeHandler.execute(cpu, opcodeInfo);

        int memoryValue = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        assertEquals(0xC2, memoryValue); // 10000101 >> 1 with carry in becomes 11000010
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b0 = 1
    }
}
