package org.mochaboy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RetOperationTest {

    private Cartridge cartridge;
    private Memory memory;
    private CPU cpu;
    private OpcodeLoader opcodeLoader;
    private OpcodeHandler opcodeHandler;
    private OpcodeWrapper opcodeWrapper;
    private Flags flags;

    @BeforeEach
    void setUp() throws IOException {
        cartridge = new Cartridge(new File("./././Tetris.gb").toPath());
        memory = new Memory(cartridge);
        cpu = new CPU(memory);
        opcodeLoader = new OpcodeLoader();
        opcodeWrapper = opcodeLoader.getOpcodeWrapper();
        opcodeHandler = new OpcodeHandler(opcodeWrapper);
        flags = new Flags();
    }

    /**
     * Helper method to set CPU flags using the Flags class.
     * Converts boolean flags to string representations.
     */
    private void setCPUFlags(boolean z, boolean n, boolean h, boolean c) {
        flags.setZ(z ? "1" : "0");
        flags.setN(n ? "1" : "0");
        flags.setH(h ? "1" : "0");
        flags.setC(c ? "1" : "0");

        // Update CPU flags based on Flags class
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, z);
        cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT, n);
        cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY, h);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, c);
    }

    /**
     * Test the unconditional RET instruction.
     * Ensures that the program counter is set to the address popped from the stack.
     */
    @Test
    void testRET_Unconditional() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC9"); // RET

        // Set up the stack with return address 0x8000
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getMemory().writeByte(0xFFFE, 0x00); // Low byte
        cpu.getMemory().writeByte(0xFFFF, 0x80); // High byte

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x8000, cpu.getRegisters().getPC(), "Program counter should be set to 0x8000 after RET");
    }

    /**
     * Test the RET Z (Return if Zero flag is set) instruction.
     * Ensures that the program counter is set correctly when the Zero flag is true.
     */
    @Test
    void testRET_IfZero_Set() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC8"); // RET Z

        // Set Zero flag using Flags class
        setCPUFlags(true, false, false, false);
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getMemory().writeByte(0xFFFE, 0x56); // Low byte
        cpu.getMemory().writeByte(0xFFFF, 0xAB); // High byte

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xAB56, cpu.getRegisters().getPC(), "Program counter should be set to 0xAB56 when Zero flag is set");
    }

    /**
     * Test the RET Z (Return if Zero flag is set) instruction when Zero flag is not set.
     * Ensures that the program counter is not modified and the next instruction is executed.
     */
    @Test
    void testRET_IfZero_NotSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC8"); // RET Z

        // Clear Zero flag using Flags class
        setCPUFlags(false, false, false, false);
        cpu.getRegisters().setPC(0x2000); // Current PC

        opcodeHandler.execute(cpu, opcodeInfo);

        // RET Z should not return, so PC should move to the next instruction
        assertEquals(0x2001, cpu.getRegisters().getPC(), "Program counter should increment by 1 when Zero flag is not set");
    }

    /**
     * Test the RET NZ (Return if Zero flag is not set) instruction.
     * Ensures that the program counter is set correctly when the Zero flag is false.
     */
    @Test
    void testRET_IfNotZero_Set() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC0"); // RET NZ

        // Clear Zero flag using Flags class
        setCPUFlags(false, false, false, false);
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getMemory().writeByte(0xFFFE, 0x34); // Low byte
        cpu.getMemory().writeByte(0xFFFF, 0x12); // High byte

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1234, cpu.getRegisters().getPC(), "Program counter should be set to 0x1234 when Zero flag is not set");
    }

    /**
     * Test the RET NZ (Return if Zero flag is not set) instruction when Zero flag is set.
     * Ensures that the program counter is not modified and the next instruction is executed.
     */
    @Test
    void testRET_IfNotZero_NotSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC0"); // RET NZ

        // Set Zero flag using Flags class
        setCPUFlags(true, false, false, false);
        cpu.getRegisters().setPC(0x3000); // Current PC

        opcodeHandler.execute(cpu, opcodeInfo);

        // RET NZ should not return, so PC should move to the next instruction
        assertEquals(0x3001, cpu.getRegisters().getPC(), "Program counter should increment by 1 when Zero flag is set");
    }

    /**
     * Test the RET C (Return if Carry flag is set) instruction.
     * Ensures that the program counter is set correctly when the Carry flag is true.
     */
    @Test
    void testRET_IfCarry_Set() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD8"); // RET C

        // Set Carry flag using Flags class
        setCPUFlags(false, false, false, true);
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getMemory().writeByte(0xFFFE, 0x78); // Low byte
        cpu.getMemory().writeByte(0xFFFF, 0x56); // High byte

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x5678, cpu.getRegisters().getPC(), "Program counter should be set to 0x5678 when Carry flag is set");
    }

    /**
     * Test the RET C (Return if Carry flag is set) instruction when Carry flag is not set.
     * Ensures that the program counter is not modified and the next instruction is executed.
     */
    @Test
    void testRET_IfCarry_NotSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD8"); // RET C

        // Clear Carry flag using Flags class
        setCPUFlags(false, false, false, false);
        cpu.getRegisters().setPC(0x4000); // Current PC

        opcodeHandler.execute(cpu, opcodeInfo);

        // RET C should not return, so PC should move to the next instruction
        assertEquals(0x4001, cpu.getRegisters().getPC(), "Program counter should increment by 1 when Carry flag is not set");
    }

    /**
     * Test the RET NC (Return if Carry flag is not set) instruction.
     * Ensures that the program counter is set correctly when the Carry flag is false.
     */
    @Test
    void testRET_IfNotCarry_Set() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD0"); // RET NC

        // Clear Carry flag using Flags class
        setCPUFlags(false, false, false, false);
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getMemory().writeByte(0xFFFE, 0x9A); // Low byte
        cpu.getMemory().writeByte(0xFFFF, 0xBC); // High byte

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xBC9A, cpu.getRegisters().getPC(), "Program counter should be set to 0xBC9A when Carry flag is not set");
    }

    /**
     * Test the RET NC (Return if Carry flag is not set) instruction when Carry flag is set.
     * Ensures that the program counter is not modified and the next instruction is executed.
     */
    @Test
    void testRET_IfNotCarry_NotSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD0"); // RET NC

        // Set Carry flag using Flags class
        setCPUFlags(false, false, false, true);
        cpu.getRegisters().setPC(0x5000); // Current PC

        opcodeHandler.execute(cpu, opcodeInfo);

        // RET NC should not return, so PC should move to the next instruction
        assertEquals(0x5001, cpu.getRegisters().getPC(), "Program counter should increment by 1 when Carry flag is set");
    }

    /**
     * Edge case where the stack pointer points to the lowest possible address.
     * Ensures that RET correctly wraps around if necessary.
     */
    @Test
    void testRET_StackPointerEdgeCase() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC9"); // RET

        // Set stack pointer to lowest address
        cpu.getRegisters().setSP(0x0000);
        cpu.getMemory().writeByte(0x0000, 0xFF); // Low byte
        cpu.getMemory().writeByte(0x0001, 0x7F); // High byte

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x7FFF, cpu.getRegisters().getPC(), "Program counter should correctly handle stack pointer edge case");
    }

    /**
     * Test RET when the stack contains a zero address.
     * Ensures that RET correctly sets the PC to zero.
     */
    @Test
    void testRET_ReturnToZero() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC9"); // RET

        // Set stack with zero address
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getMemory().writeByte(0xFFFE, 0x00); // Low byte
        cpu.getMemory().writeByte(0xFFFF, 0x00); // High byte

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0000, cpu.getRegisters().getPC(), "Program counter should be set to 0x0000 when RET returns to zero address");
    }
}
