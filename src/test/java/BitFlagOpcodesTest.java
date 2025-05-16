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

class BitFlagOpcodesTest {
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

    // ---- BIT u3, r8 and BIT u3, [HL] ----
    static Stream<Arguments> bitProvider() {
        return Stream.of(
                // reg tests: (mnemonic, opcode, bitIndex, register, valueWithBitSet, valueWithBitClear)
                arguments("BIT 0,B", 0x40, 0, "B", 0b0000_0001, 0b0000_0010),
                arguments("BIT 7,A", 0x7F, 7, "A", 0b1000_0000, 0b0111_1111),
                // memory tests: BIT 3,[HL]
                arguments("BIT 3,(HL)", 0x5E, 3, null, 0b0000_1000, 0b1111_0111)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitProvider")
    void testBit(String name, int lowOpcode, int bit, String reg, int valSet, int valClear) {
        // All these opcodes are prefixed with 0xCB
        memory.writeByteUnrestricted(0, 0xCB);
        memory.writeByteUnrestricted(1, lowOpcode);

        int hlAddr = 0x2000;
        if (reg == null) {
            // memory variant
            cpu.getRegisters().setHL(hlAddr);
            memory.writeByteUnrestricted(hlAddr, valSet);
            while (!cpu.isTestStepComplete()) cpu.step();
            assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Z should be 0 when bit is set");
            assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "N should be 0");
            assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "H should be 1");
            assertEquals(valSet, memory.readByteUnrestricted(hlAddr), "memory value should be unchanged");

            // now clear
            cpu.setTestStepComplete(false);
            cpu.getRegisters().setPC(0);
            memory.writeByteUnrestricted(0, 0xCB);
            memory.writeByteUnrestricted(1, lowOpcode);
            memory.writeByteUnrestricted(hlAddr, valClear);
            while (!cpu.isTestStepComplete()) cpu.step();
            assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Z should be 1 when bit is clear");
        } else {
            // register variant
            cpu.getRegisters().setByName(reg, valSet);
            while (!cpu.isTestStepComplete()) cpu.step();
            assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Z should be 0 when bit is set");

            cpu.setTestStepComplete(false);
            cpu.getRegisters().setPC(0);
            cpu.getRegisters().setByName(reg, valClear);
            while (!cpu.isTestStepComplete()) cpu.step();
            assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Z should be 1 when bit is clear");
        }
    }

    // ---- RES u3, r8 and RES u3, [HL] ----
    static Stream<Arguments> resProvider() {
        return Stream.of(
                arguments("RES 0,B", 0x80, 0, "B"),
                arguments("RES 7,A", 0xBF, 7, "A"),
                arguments("RES 3,(HL)", 0x8E, 3, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("resProvider")
    void testRes(String name, int lowOpcode, int bit, String reg) {
        memory.writeByteUnrestricted(0, 0xCB);
        memory.writeByteUnrestricted(1, lowOpcode);

        int hlAddr = 0x3000;
        if (reg == null) {
            cpu.getRegisters().setHL(hlAddr);
            memory.writeByteUnrestricted(hlAddr, 0xFF); // all bits set
            while (!cpu.isTestStepComplete()) cpu.step();
            assertEquals((byte)(0xFF & ~(1 << bit)) & 0xFF,
                    memory.readByteUnrestricted(hlAddr),
                    "bit " + bit + " should be reset in memory");
        } else {
            cpu.getRegisters().setByName(reg, 0xFF);
            while (!cpu.isTestStepComplete()) cpu.step();
            assertEquals(0xFF & ~(1 << bit),
                    cpu.getRegisters().getByName(reg),
                    "bit " + bit + " should be reset in " + reg);
        }
    }

    // ---- SET u3, r8 and SET u3, [HL] ----
    static Stream<Arguments> setProvider() {
        return Stream.of(
                arguments("SET 0,B", 0xC0, 0, "B"),
                arguments("SET 7,A", 0xFF, 7, "A"),
                arguments("SET 3,(HL)", 0xCE, 3, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("setProvider")
    void testSet(String name, int lowOpcode, int bit, String reg) {
        memory.writeByteUnrestricted(0, 0xCB);
        memory.writeByteUnrestricted(1, lowOpcode);

        int hlAddr = 0x4000;
        if (reg == null) {
            cpu.getRegisters().setHL(hlAddr);
            memory.writeByteUnrestricted(hlAddr, 0x00); // all bits clear
            while (!cpu.isTestStepComplete()) cpu.step();
            assertEquals(1 << bit,
                    memory.readByteUnrestricted(hlAddr),
                    "bit " + bit + " should be set in memory");
        } else {
            cpu.getRegisters().setByName(reg, 0x00);
            while (!cpu.isTestStepComplete()) cpu.step();
            assertEquals(1 << bit,
                    cpu.getRegisters().getByName(reg),
                    "bit " + bit + " should be set in " + reg);
        }
    }
}
