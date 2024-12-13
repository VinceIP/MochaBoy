import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JpOperationTest {

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

    // **Unconditional JP Tests**
    @Test
    void testJP_Unconditional() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC3"); // JP nn
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x2000); // Write the target address at PC (0x1000)

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x2000, cpu.getRegisters().getPC()); // PC should be set to 0x2000
    }

    @Test
    void testJP_Unconditional_EndOfRAM() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC3"); // JP nn
        cpu.getRegisters().setPC(0xFFFC);
        memory.writeWord(0xFFFC, 0x3000); // Write the target address at PC (0xFFFC)

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x3000, cpu.getRegisters().getPC()); // PC should be set to 0x3000
    }

    // **Conditional JP Tests (Z, NZ, C, NC)**
    @Test
    void testJP_Z_Condition_True() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCA"); // JP Z, nn
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x1234); // Target address is 0x1234
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, true); // Z flag is set

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1234, cpu.getRegisters().getPC()); // PC should jump to 0x1234
    }

    @Test
    void testJP_Z_Condition_False() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCA"); // JP Z, nn
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x1234); // Target address is 0x1234
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, false); // Z flag is clear

        opcodeHandler.execute(cpu, opcodeInfo);

        assertNotEquals(0x1234, cpu.getRegisters().getPC()); // PC should NOT jump
        assertEquals(0x1002, cpu.getRegisters().getPC()); // PC should increment by 2
    }

    @Test
    void testJP_NZ_Condition_True() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC2"); // JP NZ, nn
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x5678); // Target address is 0x5678
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, false); // Z flag is clear

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x5678, cpu.getRegisters().getPC()); // PC should jump to 0x5678
    }

    @Test
    void testJP_NZ_Condition_False() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC2"); // JP NZ, nn
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x5678); // Target address is 0x5678
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, true); // Z flag is set

        opcodeHandler.execute(cpu, opcodeInfo);

        assertNotEquals(0x5678, cpu.getRegisters().getPC()); // PC should NOT jump
        assertEquals(0x1002, cpu.getRegisters().getPC()); // PC should increment by 2
    }

    @Test
    void testJP_C_Condition_True() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xDA"); // JP C, nn
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x3456); // Target address is 0x3456
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true); // C flag is set

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x3456, cpu.getRegisters().getPC()); // PC should jump to 0x3456
    }

    @Test
    void testJP_C_Condition_False() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xDA"); // JP C, nn
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x3456); // Target address is 0x3456
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, false); // C flag is clear

        opcodeHandler.execute(cpu, opcodeInfo);

        assertNotEquals(0x3456, cpu.getRegisters().getPC()); // PC should NOT jump
        assertEquals(0x1002, cpu.getRegisters().getPC()); // PC should increment by 2
    }

    @Test
    void testJP_NC_Condition_True() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD2"); // JP NC, nn
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x6789); // Target address is 0x6789
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, false); // C flag is clear

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x6789, cpu.getRegisters().getPC()); // PC should jump to 0x6789
    }

    @Test
    void testJP_NC_Condition_False() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD2"); // JP NC, nn
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x6789); // Target address is 0x6789
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true); // C flag is set

        opcodeHandler.execute(cpu, opcodeInfo);

        assertNotEquals(0x6789, cpu.getRegisters().getPC()); // PC should NOT jump
        assertEquals(0x1002, cpu.getRegisters().getPC()); // PC should increment by 2
    }

    @Test
    void testJP_ZeroEdgeCase() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC3"); // JP nn
        cpu.getRegisters().setPC(0x0000);
        memory.writeWord(0x0000, 0x1234); // Target address is 0x1234

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1234, cpu.getRegisters().getPC()); // PC should jump to 0x1234
    }

    @Test
    void testJP_MaxAddress() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC3"); // JP nn
        cpu.getRegisters().setPC(0xFFF0);
        memory.writeWord(0xFFF0, 0xFFFF); // Target address is 0xFFFF

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xFFFF, cpu.getRegisters().getPC()); // PC should jump to 0xFFFF
    }
}
