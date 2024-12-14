import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PushOperationTest {

    private Cartridge cartridge;
    private Memory memory;
    private CPU cpu;
    private OpcodeLoader opcodeLoader;
    private OpcodeHandler opcodeHandler;
    private OpcodeWrapper opcodeWrapper;
    private Stack stack;

    @BeforeEach
    void setUp() throws IOException {
        cartridge = new Cartridge(new File("./././Tetris.gb").toPath());
        memory = new Memory(cartridge);
        cpu = new CPU(memory);
        opcodeLoader = new OpcodeLoader();
        opcodeWrapper = opcodeLoader.getOpcodeWrapper();
        opcodeHandler = new OpcodeHandler(opcodeWrapper);
        stack = new Stack(cpu);
    }

    // PUSH AF test
    @Test
    void testPUSH_AF() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xF5"); // PUSH AF

        cpu.getRegisters().setA(0x12);
        cpu.getRegisters().setF(0xF4); // F will be masked to 0xF0

        opcodeHandler.execute(cpu, opcodeInfo);

        int sp = cpu.getRegisters().getSP();
        int value = cpu.getMemory().readWord(sp);

        assertEquals(0x12F0, value); // F is masked to 0xF0, not 0xF4
    }

    // PUSH BC test
    @Test
    void testPUSH_BC() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC5"); // PUSH BC

        cpu.getRegisters().setB(0x12);
        cpu.getRegisters().setC(0x34);

        opcodeHandler.execute(cpu, opcodeInfo);

        int sp = cpu.getRegisters().getSP();
        int value = cpu.getMemory().readWord(sp);

        assertEquals(0x1234, value); // B and C combined as a 16-bit value
    }

    // PUSH DE test
    @Test
    void testPUSH_DE() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD5"); // PUSH DE

        cpu.getRegisters().setD(0x56);
        cpu.getRegisters().setE(0x78);

        opcodeHandler.execute(cpu, opcodeInfo);

        int sp = cpu.getRegisters().getSP();
        int value = cpu.getMemory().readWord(sp);

        assertEquals(0x5678, value); // D and E combined as a 16-bit value
    }

    // PUSH HL test
    @Test
    void testPUSH_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xE5"); // PUSH HL

        cpu.getRegisters().setH(0x9A);
        cpu.getRegisters().setL(0xBC);

        opcodeHandler.execute(cpu, opcodeInfo);

        int sp = cpu.getRegisters().getSP();
        int value = cpu.getMemory().readWord(sp);

        assertEquals(0x9ABC, value); // H and L combined as a 16-bit value
    }

    // Edge case for PUSH AF where F has lower nibble set (it should be masked out)
    @Test
    void testPUSH_AF_MaskF() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xF5"); // PUSH AF

        cpu.getRegisters().setA(0xAA);
        cpu.getRegisters().setF(0xFF); // F will be masked to 0xF0

        opcodeHandler.execute(cpu, opcodeInfo);

        int sp = cpu.getRegisters().getSP();
        int value = cpu.getMemory().readWord(sp);

        assertEquals(0xAAF0, value); // F is masked to 0xF0, not 0xFF
    }

    // Edge case for PUSH where SP is at the minimum boundary of the stack
    @Test
    void testPUSH_SPBoundary() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xF5"); // PUSH AF

        cpu.getRegisters().setSP(0x0000);
        cpu.getRegisters().setA(0xAB);
        cpu.getRegisters().setF(0xCD); // F will be masked to 0xC0

        opcodeHandler.execute(cpu, opcodeInfo);

        int sp = cpu.getRegisters().getSP();
        int value = cpu.getMemory().readWord(sp);

        assertEquals(0xABC0, value); // F is masked to 0xC0, not 0xCD
    }
}
