import org.junit.jupiter.api.BeforeEach;
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

public class MiscTest {
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
        cpu.getRegisters().setSP(0);
    }

    static Stream<Arguments> incDecMem() {
        return Stream.of(
                arguments("INC [HL] (0)", 0x34, "HL", 0x7000, 0x00, 1, false, false, false, false),
                arguments("INC [HL] (FF", 0x34, "HL", 0x7000, 0xFF, 0, true, false, true, false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incDecMem")
    void testIncDec(String name, int opcode, String srcReg,
                    int address, int value, int expected,
                    boolean expectedZ, boolean expectedN, boolean expectedH, boolean expectedC) {
        cpu.getRegisters().setByName(srcReg, address);
        memory.writeByteUnrestricted(address, value);
        memory.writeByteUnrestricted(0, opcode);

        while (!cpu.isTestStepComplete()) cpu.step();

        int result = memory.readByteUnrestricted(address);

        assertEquals(expected, result);
        assertEquals(expectedZ, cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertEquals(expectedN, cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertEquals(expectedH, cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertEquals(expectedC, cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }
}
