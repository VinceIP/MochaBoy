import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class LdOperationTest {

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

    // **LD r8, r8**
    @Test
    void testLD_A_B() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x78"); // LD A, B

        cpu.getRegisters().setB(0x42);
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x42, cpu.getRegisters().getA());
    }

    // **LD r8, n8**
    @Test
    void testLD_A_n8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x3E"); // LD A, n8

        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0x42); // Write 0x42 at PC
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x42, cpu.getRegisters().getA());
        assertEquals(0x1001, cpu.getRegisters().getPC()); // PC should increment by 1
    }

    // **LD r16, n16**
    @Test
    void testLD_HL_n16() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x21"); // LD HL, n16

        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeWord(0x1000, 0x8000); // Write 0x8000 at PC
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x8000, cpu.getRegisters().getHL());
        assertEquals(0x1002, cpu.getRegisters().getPC()); // PC should increment by 2
    }

    // **LD [HL], r8**
    @Test
    void testLD_HL_B() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x70"); // LD [HL], B

        cpu.getRegisters().setHL(0x8000);
        cpu.getRegisters().setB(0x42);
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x42, cpu.getMemory().readByte(0x8000));
    }

    // **LD [HL], n8**
    @Test
    void testLD_HL_n8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x36"); // LD [HL], n8

        cpu.getRegisters().setHL(0x8000);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0x42); // Write 0x42 at PC
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x42, cpu.getMemory().readByte(0x8000));
        assertEquals(0x1001, cpu.getRegisters().getPC()); // PC should increment by 1
    }

    // **LD r8, [HL]**
    @Test
    void testLD_B_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x46"); // LD B, [HL]

        cpu.getRegisters().setHL(0x8000);
        cpu.getMemory().writeByte(0x8000, 0x42); // Write 0x42 at [HL]
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x42, cpu.getRegisters().getB());
    }

    // **LD [BC], A**
    @Test
    void testLD_BC_A() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x02"); // LD [BC], A

        cpu.getRegisters().setA(0x42);
        cpu.getRegisters().setBC(0x8000);
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x42, cpu.getMemory().readByte(0x8000));
    }

    // **LD [DE], A**
    @Test
    void testLD_DE_A() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x12"); // LD [DE], A

        cpu.getRegisters().setA(0x42);
        cpu.getRegisters().setDE(0x8000);
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x42, cpu.getMemory().readByte(0x8000));
    }

    // **LD [n16], A**
    @Test
    void testLD_n16_A() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xEA"); // LD [n16], A

        cpu.getRegisters().setA(0x42);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeWord(0x1000, 0x8000); // Write 0x8000 at PC
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x42, cpu.getMemory().readByte(0x8000));
        assertEquals(0x1002, cpu.getRegisters().getPC()); // PC should increment by 2
    }

    // **LD A, [n16]**
    @Test
    void testLD_A_n16() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xFA"); // LD A, [n16]

        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeWord(0x1000, 0x8000); // Write 0x8000 at PC
        cpu.getMemory().writeByte(0x8000, 0x42); // Write 0x42 at [n16]
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x42, cpu.getRegisters().getA());
        assertEquals(0x1002, cpu.getRegisters().getPC()); // PC should increment by 2
    }

    // **LD SP, HL**
    @Test
    void testLD_SP_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xF9"); // LD SP, HL

        cpu.getRegisters().setHL(0x8000);
        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x8000, cpu.getRegisters().getSP());
    }
}
