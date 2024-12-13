import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CPLOperationTest {

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

    @Test
    void testCPL_complementAccumulator() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x2F"); // CPL opcode

        cpu.getRegisters().setA(0x3C); // Set register A to 0x3C

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xC3, cpu.getRegisters().getA(), "Accumulator A should be complemented (A = ~A)");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should be set");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "Half-carry flag should be set");
    }

    @Test
    void testCPL_complementAccumulator_edgeCase() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x2F"); // CPL opcode

        cpu.getRegisters().setA(0xFF); // Set register A to 0xFF (all bits set)

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA(), "Accumulator A should be complemented (A = ~A), resulting in 0x00");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should be set");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "Half-carry flag should be set");
    }
}
