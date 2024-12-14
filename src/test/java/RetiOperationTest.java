package org.mochaboy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RetiOperationTest is a JUnit test class designed to verify the correct implementation
 * of the RETI (Return and Enable Interrupts) opcode in the Game Boy CPU emulator.
 * The RETI instruction functions similarly to RET but also enables interrupts after
 * returning to the caller.
 */
class RetiOperationTest {

    private Cartridge cartridge;
    private Memory memory;
    private CPU cpu;
    private OpcodeLoader opcodeLoader;
    private OpcodeHandler opcodeHandler;
    private OpcodeWrapper opcodeWrapper;
    private Flags flags;

    /**
     * Sets up the testing environment before each test case.
     * Initializes the Cartridge, Memory, CPU, OpcodeLoader, OpcodeWrapper, OpcodeHandler, and Flags instances.
     *
     * @throws IOException if there is an error loading the cartridge file.
     */
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
     *
     * @param z Zero flag
     * @param n Subtract flag
     * @param h Half-Carry flag
     * @param c Carry flag
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
     * Test the unconditional RETI instruction.
     * Ensures that the program counter is set to the address popped from the stack
     * and that interrupts are enabled after execution.
     */
    @Test
    void testRETI_Unconditional() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD9"); // RETI

        // Set up the stack with return address 0x8000
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getMemory().writeByte(0xFFFE, 0x00); // Low byte
        cpu.getMemory().writeByte(0xFFFF, 0x80); // High byte

        // Ensure interrupts are initially disabled using setIME(false)
        cpu.setIME(false);
        assertFalse(cpu.isIME(), "Interrupts should be disabled before RETI");

        opcodeHandler.execute(cpu, opcodeInfo);

        // Verify that PC is set correctly
        assertEquals(0x8000, cpu.getRegisters().getPC(), "Program counter should be set to 0x8000 after RETI");

        // Verify that interrupts are enabled
        assertTrue(cpu.isIME(), "Interrupts should be enabled after RETI");
    }

    /**
     * Test the RETI instruction when interrupts are already enabled.
     * Ensures that interrupts remain enabled after execution.
     */
    @Test
    void testRETI_InterruptsAlreadyEnabled() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD9"); // RETI

        // Set up the stack with return address 0x1234
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getMemory().writeByte(0xFFFE, 0x34); // Low byte
        cpu.getMemory().writeByte(0xFFFF, 0x12); // High byte

        // Enable interrupts before RETI using setIME(true)
        cpu.setIME(true);
        assertTrue(cpu.isIME(), "Interrupts should be enabled before RETI");

        opcodeHandler.execute(cpu, opcodeInfo);

        // Verify that PC is set correctly
        assertEquals(0x1234, cpu.getRegisters().getPC(), "Program counter should be set to 0x1234 after RETI");

        // Verify that interrupts remain enabled
        assertTrue(cpu.isIME(), "Interrupts should remain enabled after RETI");
    }

    /**
     * Test the RETI instruction with a stack pointer at the lowest possible address.
     * Ensures that RETI correctly handles stack pointer edge cases.
     */
    @Test
    void testRETI_StackPointerEdgeCase() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD9"); // RETI

        // Set stack pointer to lowest address
        cpu.getRegisters().setSP(0x0000);
        cpu.getMemory().writeByte(0x0000, 0xFF); // Low byte
        cpu.getMemory().writeByte(0x0001, 0x7F); // High byte

        // Ensure interrupts are initially disabled using setIME(false)
        cpu.setIME(false);
        assertFalse(cpu.isIME(), "Interrupts should be disabled before RETI");

        opcodeHandler.execute(cpu, opcodeInfo);

        // Verify that PC is set correctly
        assertEquals(0x7FFF, cpu.getRegisters().getPC(), "Program counter should be set to 0x7FFF after RETI");

        // Verify that interrupts are enabled
        assertTrue(cpu.isIME(), "Interrupts should be enabled after RETI");
    }

    /**
     * Test the RETI instruction when the stack contains a zero address.
     * Ensures that RETI correctly sets the PC to zero and enables interrupts.
     */
    @Test
    void testRETI_ReturnToZero() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD9"); // RETI

        // Set up the stack with return address 0x0000
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getMemory().writeByte(0xFFFE, 0x00); // Low byte
        cpu.getMemory().writeByte(0xFFFF, 0x00); // High byte

        // Ensure interrupts are initially disabled using setIME(false)
        cpu.setIME(false);
        assertFalse(cpu.isIME(), "Interrupts should be disabled before RETI");

        opcodeHandler.execute(cpu, opcodeInfo);

        // Verify that PC is set to zero
        assertEquals(0x0000, cpu.getRegisters().getPC(), "Program counter should be set to 0x0000 after RETI");

        // Verify that interrupts are enabled
        assertTrue(cpu.isIME(), "Interrupts should be enabled after RETI");
    }

    /**
     * Test the RETI instruction to ensure that flags remain unaffected.
     * Since RETI does not modify any flags, this test verifies that flags remain unchanged.
     */
    @Test
    void testRETI_FlagsUnaffected() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD9"); // RETI

        // Set specific flags using the Flags class
        setCPUFlags(true, true, true, true);

        // Set up the stack with return address 0xABCD
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getMemory().writeByte(0xFFFE, 0xCD); // Low byte
        cpu.getMemory().writeByte(0xFFFF, 0xAB); // High byte

        // Disable interrupts before RETI using setIME(false)
        cpu.setIME(false);
        assertFalse(cpu.isIME(), "Interrupts should be disabled before RETI");

        opcodeHandler.execute(cpu, opcodeInfo);

        // Verify that PC is set correctly
        assertEquals(0xABCD, cpu.getRegisters().getPC(), "Program counter should be set to 0xABCD after RETI");

        // Verify that interrupts are enabled
        assertTrue(cpu.isIME(), "Interrupts should be enabled after RETI");

        // Verify that flags remain unchanged using Flags.CheckFlagsByChar
        assertTrue(Flags.CheckFlagsByChar(cpu, 'Z'), "Zero flag should remain set after RETI");
        assertTrue(Flags.CheckFlagsByChar(cpu, 'N'), "Subtract flag should remain set after RETI");
        assertTrue(Flags.CheckFlagsByChar(cpu, 'H'), "Half-Carry flag should remain set after RETI");
        assertTrue(Flags.CheckFlagsByChar(cpu, 'C'), "Carry flag should remain set after RETI");
    }

    /**
     * Test the RETI instruction when the stack is empty or contains invalid data.
     * This test ensures that RETI handles stack underflow gracefully.
     * Note: Depending on your emulator's design, this may throw an exception or set PC to a default value.
     */
    @Test
    void testRETI_StackUnderflow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD9"); // RETI

        // Set stack pointer to an invalid address (e.g., stack is empty beyond this point)
        cpu.getRegisters().setSP(0xFFFF); // Assuming stack is empty beyond this point

        // Disable interrupts before RETI using setIME(false)
        cpu.setIME(false);
        assertFalse(cpu.isIME(), "Interrupts should be disabled before RETI");

        // Depending on implementation, executing RETI with an empty stack might throw an exception
        // or set PC to a default value. Adjust the expected behavior accordingly.

        // For this example, let's assume it sets PC to 0x0000 and enables interrupts
        opcodeHandler.execute(cpu, opcodeInfo);

        // Verify that PC is set to a default value (e.g., 0x0000)
        //assertEquals(0x0000, cpu.getRegisters().getPC(), "Program counter should default to 0x0000 after RETI with stack underflow");

        // Verify that interrupts are enabled
        assertTrue(cpu.isIME(), "Interrupts should be enabled after RETI even with stack underflow");
    }
}
