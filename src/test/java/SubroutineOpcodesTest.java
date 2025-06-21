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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SubroutineOpcodesTest {
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

    // CALL n16
    @Test
    void testCallImmediate() {
        // Write opcode and immediate address 0x1234
        memory.writeByteUnrestricted(0, 0xCD);
        memory.writeByteUnrestricted(1, 0x34);
        memory.writeByteUnrestricted(2, 0x12);
        while (!cpu.isTestStepComplete()) cpu.step();
        // After call, PC should be at 0x1234
        assertEquals(0x1234, cpu.getRegisters().getPC());
        // Stack should contain return address 0x0003
        int sp = cpu.getRegisters().getSP();
        int lo = memory.readByteUnrestricted(sp);
        int hi = memory.readByteUnrestricted(sp + 1);
        assertEquals(3, (hi << 8) | lo);
    }

    // CALL cc,n16
    static Stream<Arguments> callCcProvider() {
        return Stream.of(
                arguments("CALL Z,n16", 0xCC, "Z"),
                arguments("CALL C,n16", 0xDC, "C")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("callCcProvider")
    void testCallConditional(String name, int opcode, String flag) {
        // Set the condition flag true
        cpu.getRegisters().setFlag(
                flag.equals("Z") ? Registers.FLAG_ZERO : Registers.FLAG_CARRY,
                true
        );
        // Write opcode and immediate
        memory.writeByteUnrestricted(0, opcode);
        memory.writeByteUnrestricted(1, 0x00);
        memory.writeByteUnrestricted(2, 0x10);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x1000, cpu.getRegisters().getPC());
    }

    // JP HL
    @Test
    void testJpHl() {
        cpu.getRegisters().setHL(0x3456);
        memory.writeByteUnrestricted(0, 0xE9);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x3456, cpu.getRegisters().getPC());
    }

    // JP n16
    @Test
    void testJpImmediate() {
        memory.writeByteUnrestricted(0, 0xC3);
        memory.writeByteUnrestricted(1, 0x78);
        memory.writeByteUnrestricted(2, 0x56);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x5678, cpu.getRegisters().getPC());
    }

    // JP cc,n16
    static Stream<Arguments> jpCcProvider() {
        return Stream.of(
                arguments("JP Z,n16", 0xCA, "Z"),
                arguments("JP C,n16", 0xDA, "C")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("jpCcProvider")
    void testJpConditional(String name, int opcode, String flag) {
        cpu.getRegisters().setFlag(
                flag.equals("Z") ? Registers.FLAG_ZERO : Registers.FLAG_CARRY,
                true
        );
        memory.writeByteUnrestricted(0, opcode);
        memory.writeByteUnrestricted(1, 0x00);
        memory.writeByteUnrestricted(2, 0x20);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x2000, cpu.getRegisters().getPC());
    }

    // JR n8
    @Test
    void testJrImmediate() {
        memory.writeByteUnrestricted(0, 0x18);
        memory.writeByteUnrestricted(1, (byte)0x05);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x0007, cpu.getRegisters().getPC());
    }

    // JR cc,n8
    static Stream<Arguments> jrCcProvider() {
        return Stream.of(
                arguments("JR Z,n8", 0x28, "Z"),
                arguments("JR C,n8", 0x38, "C")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("jrCcProvider")
    void testJrConditional(String name, int opcode, String flag) {
        cpu.getRegisters().setFlag(
                flag.equals("Z") ? Registers.FLAG_ZERO : Registers.FLAG_CARRY,
                true
        );
        memory.writeByteUnrestricted(0, opcode);
        memory.writeByteUnrestricted(1, (byte)0xFF); // -1 offset
        while (!cpu.isTestStepComplete()) cpu.step();
        // Should jump back one (from 0->0, then -1 + 2 bytes = 1)?
        assertEquals(1, cpu.getRegisters().getPC());
    }

    // RET cc and RET
    static Stream<Arguments> retCcProvider() {
        return Stream.of(
                arguments("RET Z", 0xC8, "Z"),
                arguments("RET C", 0xD8, "C")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("retCcProvider")
    void testRetConditional(String name, int opcode, String flag) {
        // push return address 0x1234
        cpu.getRegisters().setSP(0x1000);
        memory.writeByteUnrestricted(cpu.getRegisters().getSP(), 0x34);
        memory.writeByteUnrestricted(cpu.getRegisters().getSP()+1, 0x12);
        cpu.getRegisters().setFlag(
                flag.equals("Z") ? Registers.FLAG_ZERO : Registers.FLAG_CARRY,
                true
        );
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x1234, cpu.getRegisters().getPC());
    }

    @Test
    void testRet() {
        cpu.getRegisters().setSP(0x1000);
        memory.writeByteUnrestricted(cpu.getRegisters().getSP(),0x78);
        memory.writeByteUnrestricted(cpu.getRegisters().getSP() + 1, 0x56);
        memory.writeByteUnrestricted(0, 0xC9);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x5678, cpu.getRegisters().getPC());
    }

    @Test
    void testReti() {
        cpu.getRegisters().setSP(0x1000);
        memory.writeByteUnrestricted(cpu.getRegisters().getSP(), 0x11);
        memory.writeByteUnrestricted(cpu.getRegisters().getSP()+1, 0x22);
        memory.writeByteUnrestricted(0, 0xD9);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x2211, cpu.getRegisters().getPC());
        // RETI should re-enable interrupts immediately
        assertTrue(cpu.isIME(), "RETI did not enable IME");
    }

    // RST x
    static Stream<Arguments> rstProvider() {
        return Stream.of(
                arguments("RST 00h", 0xC7, 0x00),
                arguments("RST 08h", 0xCF, 0x08),
                arguments("RST 10h", 0xD7, 0x10),
                arguments("RST 18h", 0xDF, 0x18),
                arguments("RST 20h", 0xE7, 0x20),
                arguments("RST 28h", 0xEF, 0x28),
                arguments("RST 30h", 0xF7, 0x30),
                arguments("RST 38h", 0xFF, 0x38)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rstProvider")
    void testRst(String name, int opcode, int addr) {
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(addr, cpu.getRegisters().getPC());
    }
}
