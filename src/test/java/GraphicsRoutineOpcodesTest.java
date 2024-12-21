package org.mochaboy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GraphicsRoutineOpcodesTest {

    private Cartridge cartridge;
    private Memory memory;
    private CPU cpu;
    private OpcodeLoader opcodeLoader;
    private OpcodeHandler opcodeHandler;
    private OpcodeWrapper opcodeWrapper;
    private PPU ppu;

    @BeforeEach
    void setUp() throws IOException {
        cartridge = new Cartridge(new File("./././Tetris.gb").toPath());
        memory = new Memory(cartridge);
        FrameBuffer frameBuffer = new FrameBuffer(160,144);
        ppu = new PPU(memory,frameBuffer);
        cpu = new CPU(ppu, memory);
        opcodeLoader = new OpcodeLoader();
        opcodeWrapper = opcodeLoader.getOpcodeWrapper();
        opcodeHandler = new OpcodeHandler(opcodeWrapper);
    }

    @Test
    void testRLC() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x01"); // RLC C

        // Test rotating 0x85 (10000101)
        cpu.getRegisters().setPC(0x0000);
        cpu.getRegisters().setC(0x85);
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0B, cpu.getRegisters().getC()); // C should be 0x0B (00001011)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));  // Carry flag should be set
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Zero flag should be reset
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N flag should be reset
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag should be reset

        // Test rotating 0x00
        cpu.getRegisters().setPC(0x0000);
        cpu.getRegisters().setC(0x00);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getC()); // C should be 0x00
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry flag should be reset
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));  // Zero flag should be set
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N flag should be reset
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag should be reset
    }

    @Test
    void testRLA() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x17"); // RLA

        // Test rotating 0x85 (10000101) with Carry flag set
        cpu.getRegisters().setPC(0x0000);
        cpu.getRegisters().setA(0x85);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY);
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0B, cpu.getRegisters().getA()); // A should be 0x0B (00001011)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry flag should be set
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Zero flag should be reset
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N flag should be reset
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag should be reset

        // Test rotating 0x00 with Carry flag reset
        cpu.getRegisters().setPC(0x0000);
        cpu.getRegisters().setA(0x00);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA()); // A should be 0x00
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry flag should be reset
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Zero flag should be reset
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N flag should be reset
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag should be reset
    }

    @Test
    void testPUSH_BC() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC5"); // PUSH BC

        cpu.getRegisters().setBC(0x1234);
        cpu.getRegisters().setSP(0xFFFE);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xFFFC, cpu.getRegisters().getSP());
        assertEquals(0x12, cpu.getMemory().readByte(0xFFFD));
        assertEquals(0x34, cpu.getMemory().readByte(0xFFFC));
    }

    @Test
    void testPOP_BC() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC1"); // POP BC

        // Initialize SP to the top of the usable stack area
        cpu.getRegisters().setSP(0xFFFE);

        // Push a value onto the stack using your Stack.push() method
        cpu.getStack().push(0x1234);

        // Now, executing POP BC should retrieve the value from the stack
        opcodeHandler.execute(cpu, opcodeInfo);

        // Check that SP is incremented correctly
        assertEquals(0xFFFE, cpu.getRegisters().getSP(), "SP should be incremented by 2 after POP");

        // Check that BC contains the value that was popped from the stack
        assertEquals(0x1234, cpu.getRegisters().getBC(), "BC should have the value popped from the stack");
    }

    @Test
    void testLD_HL_INC_A() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x22"); // LD (HL+),A

        cpu.getRegisters().setA(0x55);
        cpu.getRegisters().setHL(0xC000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x55, cpu.getMemory().readByte(0xC000));
        assertEquals(0xC001, cpu.getRegisters().getHL());
    }

    @Test
    void testINC_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x23"); // INC HL

        cpu.getRegisters().setHL(0x1234);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1235, cpu.getRegisters().getHL());
    }

    @Test
    void testLD_C_A() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x4F"); // LD C, A
        cpu.getRegisters().setA(0x55);
        cpu.getRegisters().setC(0x00);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x55, cpu.getRegisters().getA());
        assertEquals(0x55, cpu.getRegisters().getC());
    }

    @Test
    void testLD_A_DE() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x1A"); // LD A,(DE)
        cpu.getRegisters().setDE(0x8000);
        cpu.getMemory().writeByte(0x8000, 0x99);
        cpu.getRegisters().setA(0x00);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x99, cpu.getRegisters().getA());
    }

    @Test
    void testBIT_7_H() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x7C");

        cpu.getRegisters().setH(0b10000000);
        cpu.getRegisters().setF(0);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Zero flag should not be set");
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should not be set");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "Half Carry flag should be set");

        cpu.getRegisters().setH(0b01111111);
        cpu.getRegisters().setF(0);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Zero flag should be set");
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should not be set");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "Half Carry flag should be set");
    }
}