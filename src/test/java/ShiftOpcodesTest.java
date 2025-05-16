import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.registers.Registers;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ShiftOpcodesTest {
    private Memory memory;
    private CPU cpu;

    @BeforeEach
    void setUp() throws IOException {
        memory = new Memory();
        cpu = new CPU(null, memory);
        memory.setCpu(cpu);
        cpu.setTestStepComplete(false);
        cpu.setTestMode(true);
        cpu.setCpuState(CPU.CPUState.FETCH);
        cpu.getRegisters().setPC(0);
    }

    // RL r8
    static Stream<Arguments> rlProvider() {
        return Stream.of(
                // name, opcode low byte, init val, init carry, expected val, expected carry, expected zero
                arguments("RL B no carry", 0x10, 0x80, false, 0x00, true, true),
                arguments("RL B with carry", 0x10, 0x80, true, 0x01, true, false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rlProvider")
    void testRlRegister(String name, int lowOpcode, int init, boolean initCarry,
                        int expected, boolean expCarry, boolean expZero) {
        memory.writeByteUnrestricted(0, 0xCB);
        memory.writeByteUnrestricted(1, lowOpcode);
        cpu.getRegisters().setByName("B", init);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, initCarry);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(expected, cpu.getRegisters().getByName("B"));
        assertEquals(expCarry, cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertEquals(expZero, cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    // RL [HL]
    @Test
    void testRlMemory() {
        memory.writeByteUnrestricted(0, 0xCB);
        memory.writeByteUnrestricted(1, 0x16);
        int addr = 0x2000;
        cpu.getRegisters().setHL(addr);
        memory.writeByteUnrestricted(addr, 0x01);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x02, memory.readByteUnrestricted(addr));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
    }

    // RLA
    @Test
    void testRla() {
        memory.writeByteUnrestricted(0, 0x17);
        cpu.getRegisters().setByName("A", 0x80);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x00, cpu.getRegisters().getByName("A"));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // RLA always clears Z
    }

    // RLC r8
    static Stream<Arguments> rlcProvider() {
        return Stream.of(
                arguments("RLC B", 0x00, 0x80, 0x01, true, false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rlcProvider")
    void testRlcRegister(String name, int lowOpcode, int init, int expected,
                         boolean expCarry, boolean expZero) {
        memory.writeByteUnrestricted(0, 0xCB);
        memory.writeByteUnrestricted(1, lowOpcode);
        cpu.getRegisters().setByName("B", init);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(expected, cpu.getRegisters().getByName("B"));
        assertEquals(expCarry, cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertEquals(expZero, cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
    }

    // RLCA
    @Test
    void testRlca() {
        memory.writeByteUnrestricted(0, 0x07);
        cpu.getRegisters().setByName("A", 0x80);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x01, cpu.getRegisters().getByName("A"));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }

    // RR r8
    static Stream<Arguments> rrProvider() {
        return Stream.of(
                arguments("RR B no carry", 0x18, 0x01, false, 0x00, true),
                arguments("RR B with carry", 0x18, 0x00, true, 0x80, false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rrProvider")
    void testRrRegister(String name, int lowOpcode, int init, boolean initCarry,
                        int expected, boolean expCarry) {
        memory.writeByteUnrestricted(0, 0xCB);
        memory.writeByteUnrestricted(1, lowOpcode);
        cpu.getRegisters().setByName("B", init);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, initCarry);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(expected, cpu.getRegisters().getByName("B"));
        assertEquals(expCarry, cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }

    // RRA
    @Test
    void testRra() {
        memory.writeByteUnrestricted(0, 0x1F);
        cpu.getRegisters().setByName("A", 0x01);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x00, cpu.getRegisters().getByName("A"));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }

    // SLA r8
    @Test
    void testSlaRegister() {
        memory.writeByteUnrestricted(0, 0xCB);
        memory.writeByteUnrestricted(1, 0x20); // SLA B
        cpu.getRegisters().setByName("B", 0x80);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x00, cpu.getRegisters().getByName("B"));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
    }

    // SRA r8
    @Test
    void testSraRegister() {
        memory.writeByteUnrestricted(0, 0xCB);
        memory.writeByteUnrestricted(1, 0x28); // SRA B
        cpu.getRegisters().setByName("B", 0x01);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x00, cpu.getRegisters().getByName("B"));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }

    // SRL r8
    @Test
    void testSrlRegister() {
        memory.writeByteUnrestricted(0, 0xCB);
        memory.writeByteUnrestricted(1, 0x38); // SRL B
        cpu.getRegisters().setByName("B", 0x01);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x00, cpu.getRegisters().getByName("B"));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }

    // SWAP r8
    @Test
    void testSwapRegister() {
        memory.writeByteUnrestricted(0, 0xCB);
        memory.writeByteUnrestricted(1, 0x30); // SWAP B
        cpu.getRegisters().setByName("B", 0xF0);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x0F, cpu.getRegisters().getByName("B"));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }
}
