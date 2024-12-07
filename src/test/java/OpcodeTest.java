import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class OpcodeTest {
    private Memory memory;
    private Registers registers;
    private CPU cpu;

    @BeforeEach
    void setup() throws IOException {
        Cartridge cartridge = new Cartridge(Paths.get("./././Tetris.gb"));
        memory = new Memory(cartridge);
        cpu = new CPU(memory);
        registers = cpu.getRegisters();
    }

    @Test
    void testNOP() {
        int opcode = 0;
        Opcode.execute(cpu, opcode);

        assertEquals(0x0001, registers.getPC(), "PC should increment by 1 for NOP.");
        assertEquals(4, cpu.getTStateCounter(), "T states should increase by 4 for NOP.");
    }

    @Test
    void testLDBC_d16() {
        int opcode = 1;
        memory.writeByte(0x0000, 0x34);
        memory.writeByte(0x0001, 0x12);

        Opcode.execute(cpu, opcode);

        assertEquals(0x1234, registers.getBC(), "BC should have 0x1234.");
        assertEquals(0x0003, registers.getPC(), "PC should increment by 3.");
        assertEquals(12, cpu.getTStateCounter(), "T states should increment by 12.");
    }

    @Test
    void testLD_BC_A() {
        int opcode = 2;
        registers.setA(0x56);
        registers.setBC(0x2000);

        Opcode.execute(cpu, opcode);

        // Assertions
        assertEquals(0x56, memory.readByte(0x2000), "Memory at BC (0x2000) should be set to A (0x56)");
        assertEquals(0x0001, registers.getPC(), "PC should increment by 1 for LD [BC], A");
        assertEquals(8, cpu.getTStateCounter(), "T-states should increment by 8 for LD [BC], A");
    }

    @Test
    void testINC_BC() {
        // Opcode 3
        int opcode = 3; // INC BC

        // Setup registers
        registers.setBC(0x1234);

        // Execute opcode
        Opcode.execute(cpu, opcode);

        // Assertions
        assertEquals(0x1235, registers.getBC(), "BC should be incremented to 0x1235");
        assertEquals(0x0001, registers.getPC(), "PC should increment by 1 for INC BC");
        assertEquals(8, cpu.getTStateCounter(), "T-states should increment by 8 for INC BC");
    }

    @Test
    void testINC_B() {
        // Opcode 4
        int opcode = 4; // INC B

        // Setup registers
        registers.setB(0x0F); // Set B to 15

        // Execute opcode
        Opcode.execute(cpu, opcode);

        // Assertions
        assertEquals(0x10, registers.getB(), "B should be incremented to 16 (0x10)");
        assertFalse(registers.isFlagSet(Registers.FLAG_ZERO), "Zero flag should be cleared");
        assertFalse(registers.isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should be cleared");
        assertTrue(registers.isFlagSet(Registers.FLAG_HALF_CARRY), "Half Carry flag should be set");
        assertEquals(0x0001, registers.getPC(), "PC should increment by 1 for INC B");
        assertEquals(4, cpu.getTStateCounter(), "T-states should increment by 4 for INC B");
    }

    @Test
    void testDEC_B() {
        // Opcode 0x05: DEC B
        int opcode = 5;

        // Setup initial value
        registers.setB(1);

        // Execute opcode
        Opcode.execute(cpu, opcode);

        // Assertions
        assertEquals(0, registers.getB(), "B should decrement to 0");
        assertTrue(registers.isFlagSet(Registers.FLAG_ZERO), "Zero flag should be set");
        assertTrue(registers.isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should be set");
        assertFalse(registers.isFlagSet(Registers.FLAG_HALF_CARRY), "Half Carry flag should be cleared");
        assertEquals(1, registers.getPC(), "PC should increment by 1");
        assertEquals(4, cpu.getTStateCounter(), "T-states should increment by 4");
    }

    @Test
    void testLD_B_n8() {
        // Opcode 0x06: LD B, n8
        int opcode = 6;

        // Setup memory with opcode and immediate value
        memory.writeByte(0, opcode);
        memory.writeByte(1, 66); // n8 = 66

        // Execute opcode
        Opcode.execute(cpu, opcode);

        // Assertions
        assertEquals(66, registers.getB(), "B should be loaded with 66");
        assertEquals(2, registers.getPC(), "PC should increment by 2");
        assertEquals(8, cpu.getTStateCounter(), "T-states should increment by 8");

        // Flags should remain unaffected
        assertFalse(registers.isFlagSet(Registers.FLAG_ZERO), "Zero flag should be unaffected");
        assertFalse(registers.isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should be unaffected");
        assertFalse(registers.isFlagSet(Registers.FLAG_HALF_CARRY), "Half Carry flag should be unaffected");
        assertFalse(registers.isFlagSet(Registers.FLAG_CARRY), "Carry flag should be unaffected");
    }

    @Test
    void testRLCA() {
        // Opcode 0x07: RLCA
        int opcode = 7;

        registers.setA(0x80);
        registers.clearFlag(Registers.FLAG_CARRY);

        Opcode.execute(cpu, opcode);

        // Expected A after RLCA: 0x01 (00000001)
        assertEquals(0x01, registers.getA(), "A should be rotated left to 0x01");

        // Carry flag should be set to original bit 7 (which was 1)
        assertTrue(registers.isFlagSet(Registers.FLAG_CARRY), "Carry flag should be set");

        // Other flags should remain unaffected (assuming they were initially cleared)
        assertFalse(registers.isFlagSet(Registers.FLAG_ZERO), "Zero flag should be unaffected");
        assertFalse(registers.isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should be unaffected");
        assertFalse(registers.isFlagSet(Registers.FLAG_HALF_CARRY), "Half Carry flag should be unaffected");

        // PC should increment by 1
        assertEquals(1, registers.getPC(), "PC should increment by 1");

        // T-states should increment by 4
        assertEquals(4, cpu.getTStateCounter(), "T-states should increment by 4");
    }
}
