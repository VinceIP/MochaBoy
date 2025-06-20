import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class LdOpcodesTest {
    private Memory memory;
    private CPU cpu;

    @BeforeEach
    void setUp() throws IOException {
        // Use Tetris.gb in project root as cartridge for testing
        //Cartridge cart = new Cartridge(Paths.get("Tetris.gb"));
        memory = new Memory();
        cpu = new CPU(null, memory);
        memory.setCpu(cpu);
        cpu.setTestStepComplete(false);
        cpu.setTestMode(true);
        cpu.setCpuState(CPU.CPUState.FETCH);
        cpu.getRegisters().setPC(0);
    }

    // Register-to-register LD r,r'
    static Stream<Arguments> regToRegProvider() {
        return Stream.of(
                arguments("LD B,B", 0x40, "B", "B"),
                arguments("LD B,C", 0x41, "B", "C"),
                arguments("LD B,D", 0x42, "B", "D"),
                arguments("LD B,E", 0x43, "B", "E"),
                arguments("LD B,H", 0x44, "B", "H"),
                arguments("LD B,L", 0x45, "B", "L"),
                arguments("LD B,A", 0x47, "B", "A"),
                arguments("LD C,B", 0x48, "C", "B"),
                arguments("LD C,C", 0x49, "C", "C"),
                arguments("LD C,D", 0x4A, "C", "D"),
                arguments("LD C,E", 0x4B, "C", "E"),
                arguments("LD C,H", 0x4C, "C", "H"),
                arguments("LD C,L", 0x4D, "C", "L"),
                arguments("LD C,A", 0x4F, "C", "A"),
                arguments("LD D,B", 0x50, "D", "B"),
                arguments("LD D,C", 0x51, "D", "C"),
                arguments("LD D,D", 0x52, "D", "D"),
                arguments("LD D,E", 0x53, "D", "E"),
                arguments("LD D,H", 0x54, "D", "H"),
                arguments("LD D,L", 0x55, "D", "L"),
                arguments("LD D,A", 0x57, "D", "A"),
                arguments("LD E,B", 0x58, "E", "B"),
                arguments("LD E,C", 0x59, "E", "C"),
                arguments("LD E,D", 0x5A, "E", "D"),
                arguments("LD E,E", 0x5B, "E", "E"),
                arguments("LD E,H", 0x5C, "E", "H"),
                arguments("LD E,L", 0x5D, "E", "L"),
                arguments("LD E,A", 0x5F, "E", "A"),
                arguments("LD H,B", 0x60, "H", "B"),
                arguments("LD H,C", 0x61, "H", "C"),
                arguments("LD H,D", 0x62, "H", "D"),
                arguments("LD H,E", 0x63, "H", "E"),
                arguments("LD H,H", 0x64, "H", "H"),
                arguments("LD H,L", 0x65, "H", "L"),
                arguments("LD H,A", 0x67, "H", "A"),
                arguments("LD L,B", 0x68, "L", "B"),
                arguments("LD L,C", 0x69, "L", "C"),
                arguments("LD L,D", 0x6A, "L", "D"),
                arguments("LD L,E", 0x6B, "L", "E"),
                arguments("LD L,H", 0x6C, "L", "H"),
                arguments("LD L,L", 0x6D, "L", "L"),
                arguments("LD L,A", 0x6F, "L", "A"),
                arguments("LD A,B", 0x78, "A", "B"),
                arguments("LD A,C", 0x79, "A", "C"),
                arguments("LD A,D", 0x7A, "A", "D"),
                arguments("LD A,E", 0x7B, "A", "E"),
                arguments("LD A,H", 0x7C, "A", "H"),
                arguments("LD A,L", 0x7D, "A", "L"),
                arguments("LD A,A", 0x7F, "A", "A")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("regToRegProvider")
    void testRegisterToRegister(String name, int opcode, String destReg, String srcReg) {
        cpu.getRegisters().setByName(srcReg, 0x5A);
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) {
            cpu.step();
        }
        int result = cpu.getRegisters().getByName(destReg);
        assertEquals(0x5A, result, name + " should load value from " + srcReg + " into " + destReg);
    }

    // Register-immediate LD r,n8
    static Stream<Arguments> regImmediateProvider() {
        return Stream.of(
                arguments("LD B,n8", 0x06, "B", (byte) 0xA5),
                arguments("LD C,n8", 0x0E, "C", (byte) 0xB6),
                arguments("LD D,n8", 0x16, "D", (byte) 0xC7),
                arguments("LD E,n8", 0x1E, "E", (byte) 0xD8),
                arguments("LD H,n8", 0x26, "H", (byte) 0xE9),
                arguments("LD L,n8", 0x2E, "L", (byte) 0xFA),
                arguments("LD A,n8", 0x3E, "A", (byte) 0x1F)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("regImmediateProvider")
    void testRegisterImmediate(String name, int opcode, String destReg, byte immediate) {
        memory.writeByteUnrestricted(0, opcode);
        memory.writeByteUnrestricted(1, immediate);
        while (!cpu.isTestStepComplete()) {
            cpu.step();
        }
        int result = cpu.getRegisters().getByName(destReg);
        assertEquals(Byte.toUnsignedInt(immediate), result, name + " should load immediate into " + destReg);
    }

    // (HL)-to-register LD r,(HL)
    static Stream<Arguments> memHlToRegProvider() {
        return Stream.of(
                arguments("LD B,(HL)", 0x46, "B"),
                arguments("LD C,(HL)", 0x4E, "C"),
                arguments("LD D,(HL)", 0x56, "D"),
                arguments("LD E,(HL)", 0x5E, "E"),
                arguments("LD H,(HL)", 0x66, "H"),
                arguments("LD L,(HL)", 0x6E, "L"),
                arguments("LD A,(HL)", 0x7E, "A")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("memHlToRegProvider")
    void testMemoryHlToRegister(String name, int opcode, String destReg) {
        cpu.getRegisters().setHL(0x1234);
        memory.writeByteUnrestricted(0x1234, 0x3C);
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) {
            cpu.step();
        }
        int result = cpu.getRegisters().getByName(destReg);
        assertEquals(0x3C, result, name + " should load memory[HL] into " + destReg);
    }

    // Register-to-(HL) LD (HL),r
    static Stream<Arguments> regToMemHlProvider() {
        return Stream.of(
                arguments("LD (HL),B", 0x70, "B"),
                arguments("LD (HL),C", 0x71, "C"),
                arguments("LD (HL),D", 0x72, "D"),
                arguments("LD (HL),E", 0x73, "E"),
                arguments("LD (HL),A", 0x77, "A")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("regToMemHlProvider")
    void testRegisterToMemoryHl(String name, int opcode, String srcReg) {
        cpu.getRegisters().setHL(0x2345);
        cpu.getRegisters().setByName(srcReg, 0x7F);
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) {
            cpu.step();
        }
        int result = memory.readByte(0x2345);
        assertEquals(0x7F, result, name + " should store " + srcReg + " into memory[HL]");
    }

    @Test
    void testMemoryHlImmediate() {
        cpu.getRegisters().setHL(0x3456);
        memory.writeByteUnrestricted(0, 0x36);
        memory.writeByteUnrestricted(1, (byte) 0x9A);
        while (!cpu.isTestStepComplete()) {
            cpu.step();
        }
        assertEquals(0x9A, memory.readByte(0x3456));
    }

    // LDH (a8),A and LDH A,(a8)
    static Stream<Arguments> ldHProvider() {
        return Stream.of(
                arguments("LDH (a8),A", 0xE0, "A", (byte) 0x10),
                arguments("LDH A,(a8)", 0xF0, "A", (byte) 0x20)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("ldHProvider")
    void testLdH(String name, int opcode, String reg, byte offset) {
        int addr = 0xFF00 | Byte.toUnsignedInt(offset);
        if (opcode == 0xE0) {
            cpu.getRegisters().setByName(reg, 0xCD);
            memory.writeByteUnrestricted(0, opcode);
            memory.writeByteUnrestricted(1, offset);
            while (!cpu.isTestStepComplete()) {
                cpu.step();
            }
            assertEquals(0xCD, memory.readByte(addr));
        } else {
            memory.writeByteUnrestricted(addr, 0xEF);
            memory.writeByteUnrestricted(0, opcode);
            memory.writeByteUnrestricted(1, offset);
            while (!cpu.isTestStepComplete()) {
                cpu.step();
            }
            assertEquals(0xEF, cpu.getRegisters().getByName(reg), name + " should load (a8) into " + reg);
        }
    }

    @Test
    void testMemoryLDA16A() {
        cpu.getRegisters().setByName("A", 0x12);
        // LD (a16),A
        memory.writeByteUnrestricted(0, 0xEA);
        memory.writeByteUnrestricted(1, 0x00);
        memory.writeByteUnrestricted(2, (byte) 0xC0);
        while (!cpu.isTestStepComplete()) {
            cpu.step();
        }
        assertEquals(0x12, memory.readByte(0xC000));
    }

    @Test
    void testMemoryLDAA16() {
        // LD A,(a16)
        memory.writeByteUnrestricted(0, 0xFA);
        memory.writeByteUnrestricted(1, 0x00);
        memory.writeByteUnrestricted(2, (byte) 0xC0);
        memory.writeByteUnrestricted(0xC000, 0x34);
        cpu.getRegisters().setPC(0);
        while (!cpu.isTestStepComplete()) {
            cpu.step();
        }
        assertEquals(0x34, cpu.getRegisters().getA());
    }

    @Test
    void testSpHlAndHlSpOps() {
        // LD HL,SP+e8
        cpu.getRegisters().setSP(0xFFF8);
        memory.writeByteUnrestricted(0, 0xF8);
        memory.writeByteUnrestricted(1, (byte) 0xF8);
        while (!cpu.isTestStepComplete()) {
            cpu.step();
        }
        assertEquals(0xFFF0, cpu.getRegisters().getHL());
        // LD SP,HL
        cpu.setTestStepComplete(false);
        cpu.getRegisters().setPC(0);
        cpu.setCpuState(CPU.CPUState.FETCH);
        cpu.getRegisters().setHL(0x1234);
        memory.writeByteUnrestricted(0, 0xF9);
        cpu.getRegisters().setPC(0);
        while (!cpu.isTestStepComplete()) {
            cpu.step();
        }
        assertEquals(0x1234, cpu.getRegisters().getSP());
    }

    static Stream<Arguments> regPairImmediateProvider() {
        return Stream.of(
                arguments("LD BC,d16", 0x01, "BC", 0x1122),
                arguments("LD DE,d16", 0x11, "DE", 0x3344),
                arguments("LD HL,d16", 0x21, "HL", 0x5566),
                arguments("LD SP,d16", 0x31, "SP", 0x7788)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("regPairImmediateProvider")
    void testRegPairImmediate(String name, int opcode, String regPair, int value) {
        memory.writeByteUnrestricted(0, opcode);
        memory.writeByteUnrestricted(1, value & 0xFF);
        memory.writeByteUnrestricted(2, (value >> 8) & 0xFF);
        while (!cpu.isTestStepComplete()) {
            cpu.step();
        }
        switch (regPair) {
            case "BC" -> assertEquals(value, cpu.getRegisters().getBC());
            case "DE" -> assertEquals(value, cpu.getRegisters().getDE());
            case "HL" -> assertEquals(value, cpu.getRegisters().getHL());
            case "SP" -> assertEquals(value, cpu.getRegisters().getSP());
        }
    }

    @Test
    void testLdMemoryA16Sp() {
        cpu.getRegisters().setSP(0xAA55);
        memory.writeByteUnrestricted(0, 0x08);
        memory.writeByteUnrestricted(1, 0x00);
        memory.writeByteUnrestricted(2, (byte) 0xFF);
        while (!cpu.isTestStepComplete()) {
            cpu.step();
        }
        assertEquals(0x55, memory.readByteUnrestricted(0xFF00));
        assertEquals(0xAA, memory.readByteUnrestricted(0xFF01));
    }
}
