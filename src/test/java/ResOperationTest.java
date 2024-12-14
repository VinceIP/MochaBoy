import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ResOperationTest {

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

    // RES bit in register B
    @Test
    void testRES_Bit3_B() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x98"); // RES 3, B

        cpu.getRegisters().setB(0b11111111); // All bits set
        System.out.printf("B: 0b%8s%n", Integer.toBinaryString(cpu.getRegisters().getB() & 0xFF).replace(' ', '0'));
        opcodeHandler.execute(cpu, opcodeInfo);
        System.out.printf("B: 0b%8s%n", Integer.toBinaryString(cpu.getRegisters().getB() & 0xFF).replace(' ', '0'));


        assertEquals(0b11110111, cpu.getRegisters().getB()); // Bit 3 should be cleared
    }

    // RES bit in register C
    @Test
    void testRES_Bit5_C() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xA9"); // RES 5, C

        cpu.getRegisters().setC(0b11111111);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b11011111, cpu.getRegisters().getC());
    }

    // RES bit in register D
    @Test
    void testRES_Bit7_D() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xBA"); // RES 7, D

        cpu.getRegisters().setD(0b11111111);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b01111111, cpu.getRegisters().getD());
    }

    // RES bit in register E
    @Test
    void testRES_Bit0_E() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x83"); // RES 0, E

        cpu.getRegisters().setE(0b11111111); // All bits set

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b11111110, cpu.getRegisters().getE()); // Bit 0 should be cleared
    }

    // RES bit in register H
    @Test
    void testRES_Bit2_H() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x94"); // RES 2, H

        cpu.getRegisters().setH(0b11111111); // All bits set

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b11111011, cpu.getRegisters().getH()); // Bit 2 should be cleared
    }

    // RES bit in register L
    @Test
    void testRES_Bit6_L() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xB5"); // RES 6, L

        cpu.getRegisters().setL(0b11111111); // All bits set

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b10111111, cpu.getRegisters().getL()); // Bit 6 should be cleared
    }

    // RES bit in register A
    @Test
    void testRES_Bit4_A() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0xA7"); // RES 4, A

        cpu.getRegisters().setA(0b11111111); // All bits set

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b11101111, cpu.getRegisters().getA()); // Bit 4 should be cleared
    }

    // RES bit in memory [HL]
    @Test
    void testRES_Bit1_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x8E"); // RES 1, [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b11111111); // All bits set in memory

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b11111101, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Bit 1 should be cleared
    }
}
