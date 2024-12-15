import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SetOperationTest {

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

    // Test SET u3, r8
    @Test
    void testSET_u3_r8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xC0"); // Assume this opcode represents SET 0, B

        cpu.getRegisters().setB(0b00000000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000001, cpu.getRegisters().getB()); // Bit 0 should be set
    }

    @Test
    void testSET_u3_r8_middleBit() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xD2"); // Correct opcode for SET 2, D

        cpu.getRegisters().setD(0b00000000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000100, cpu.getRegisters().getD()); // Bit 2 should be set
    }

    @Test
    void testSET_u3_r8_preserveBits() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xE4"); // Correct opcode for SET 4, H

        cpu.getRegisters().setH(0b00101100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00111100, cpu.getRegisters().getH()); // Bit 4 should be set, preserving other bits
    }

    @Test
    void testSET_u3_r8_alreadySet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xF7"); // Assume this opcode represents SET 6, A

        cpu.getRegisters().setA(0b11000000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b11000000, cpu.getRegisters().getA()); // Bit 6 is already set, no change should happen
    }

    // Test SET u3, [HL]
    @Test
    void testSET_u3_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xC6"); // Assume this opcode represents SET 0, [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b00000000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000001, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Bit 0 should be set
    }

    @Test
    void testSET_u3_HL_middleBit() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xD6"); // Assume this opcode represents SET 2, [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b00000000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000100, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Bit 2 should be set
    }

    @Test
    void testSET_u3_HL_preserveBits() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xE6"); // Assume this opcode represents SET 4, [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b00101100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00111100, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Bit 4 should be set, preserving other bits
    }

    @Test
    void testSET_u3_HL_alreadySet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xF6"); // Assume this opcode represents SET 6, [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b11000000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b11000000, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Bit 6 is already set, no change should happen
    }
}
