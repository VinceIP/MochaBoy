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

class StackOpcodesTest {
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

    // ADD HL,SP
    @Test
    void testAddHlSp() {
        cpu.getRegisters().setHL(0x1000);
        cpu.getRegisters().setSP(0x0100);
        memory.writeByteUnrestricted(0, 0x39);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x1100, cpu.getRegisters().getHL());
        // Flags: N=0, H=carry from bit 11?
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // ADD SP,e8
    @Test
    void testAddSpE8() {
        cpu.getRegisters().setSP(0xFFF0);
        memory.writeByteUnrestricted(0, 0xE8);
        memory.writeByteUnrestricted(1, (byte)0x10);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x0000, cpu.getRegisters().getSP());
    }

    @Test
    void testDecSp() {
        cpu.getRegisters().setSP(0x0001);
        memory.writeByteUnrestricted(0, 0x3C); // DEC SP opcode 0x3C? Actually DEC SP is 0x3B
        memory.writeByteUnrestricted(0, 0x3B);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x0000, cpu.getRegisters().getSP());
    }

    @Test
    void testIncSp() {
        cpu.getRegisters().setSP(0x0000);
        memory.writeByteUnrestricted(0, 0x33); // INC SP
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x0001, cpu.getRegisters().getSP());
    }

    @Test
    void testLdSpImmediate() {
        memory.writeByteUnrestricted(0, 0x31);
        memory.writeByteUnrestricted(1, 0x34);
        memory.writeByteUnrestricted(2, 0x12);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x1234, cpu.getRegisters().getSP());
    }

    @Test
    void testLdMemorySpImmediate() {
        cpu.getRegisters().setSP(0xAA55);
        memory.writeByteUnrestricted(0, 0x08);
        memory.writeByteUnrestricted(1, 0x00);
        memory.writeByteUnrestricted(2, (byte)0xFF);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x55, memory.readByteUnrestricted(0xFF00));
        assertEquals(0xAA, memory.readByteUnrestricted(0xFF01));
    }

    @Test
    void testLdHlSpPlusE8() {
        cpu.getRegisters().setSP(0x1000);
        memory.writeByteUnrestricted(0, 0xF8);
        memory.writeByteUnrestricted(1, (byte)0x10);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x1010, cpu.getRegisters().getHL());
    }

    @Test
    void testLdSpHl() {
        cpu.getRegisters().setHL(0x1234);
        memory.writeByteUnrestricted(0, 0xF9);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x1234, cpu.getRegisters().getSP());
    }

    @Test
    void testPopAf() {
        cpu.getRegisters().setSP(0x1000);
        memory.writeByteUnrestricted(0x1000, 0x34);   // low byte (flags)
        memory.writeByteUnrestricted(0x1001, 0x12);   // high byte (A)
        memory.writeByteUnrestricted(0, 0xF1);        // POP AF
        while (!cpu.isTestStepComplete()) cpu.step();

        // lower nibble of F must be 0
        assertEquals(0x1230, cpu.getRegisters().getAF());
        assertEquals(0x1002, cpu.getRegisters().getSP());
    }


    static Stream<Arguments> pop16Provider() {
        return Stream.of(
                arguments("POP BC", 0xC1, 0x56, 0x78, "BC", 0x5678),
                arguments("POP DE", 0xD1, 0x56, 0x78, "DE", 0x5678),
                arguments("POP HL", 0xE1, 0x56, 0x78, "HL", 0x5678),
                arguments("POP AF", 0xF1, 0x56, 0x78, "AF", 0x5670) // ← mask low nibble
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("pop16Provider")
    void testPop16(String name,
                   int opcode,
                   int high, int low,
                   String reg,
                   int expected) {

        cpu.getRegisters().setSP(0x2000);
        memory.writeByteUnrestricted(0x2000, low);
        memory.writeByteUnrestricted(0x2001, high);
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) cpu.step();

        assertEquals(expected, cpu.getRegisters().getByName(reg));
        assertEquals(0x2002, cpu.getRegisters().getSP());
    }


    static Stream<Arguments> push16Provider() {
        return Stream.of(
                arguments("PUSH BC", 0xC5, 0x1234),
                arguments("PUSH DE", 0xD5, 0x3344),
                arguments("PUSH HL", 0xE5, 0x5566),
                arguments("PUSH AF", 0xF5, 0x7788)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("push16Provider")
    void testPush16(String name, int opcode, int val) {
        cpu.getRegisters().setByName(name.split(" ")[1], val);
        cpu.getRegisters().setSP(0x3000);
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) cpu.step();
        int sp = cpu.getRegisters().getSP();
        int lo = memory.readByteUnrestricted(sp);
        int hi = memory.readByteUnrestricted(sp+1);
        assertEquals(val, (hi<<8)|lo);
    }
}
