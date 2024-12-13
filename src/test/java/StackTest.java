import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class StackTest {

    private Stack stack;
    private CPU cpu;
    private Memory memory;

    @BeforeEach
    void setUp() throws IOException {
        Cartridge cartridge = new Cartridge(new File("./././Tetris.gb").toPath());
        memory = new Memory(cartridge);
        cpu = new CPU(memory);
        stack = new Stack(cpu);
    }

    @Test
    void testPushAndPopSingleValue() {
        cpu.getRegisters().setSP(0xFFFE); // Start SP at 0xFFFE (default starting position)

        stack.push(0xABCD);

        assertEquals(0xFFFC, cpu.getRegisters().getSP(), "SP should decrement by 2 after push");
        assertEquals(0xABCD, memory.readWord(0xFFFC), "Value should be stored at SP location");

        int value = stack.pop();

        assertEquals(0xABCD, value, "The popped value should match the pushed value");
        assertEquals(0xFFFE, cpu.getRegisters().getSP(), "SP should increment by 2 after pop");
    }

    @Test
    void testPushPopMultipleValues() {
        cpu.getRegisters().setSP(0xFFFE); // Start SP at 0xFFFE

        stack.push(0x1234);
        stack.push(0xABCD);

        assertEquals(0xFFFA, cpu.getRegisters().getSP(), "SP should be 0xFFFA after pushing 2 words");

        int val1 = stack.pop();
        assertEquals(0xABCD, val1, "The most recently pushed value should be popped first");

        int val2 = stack.pop();
        assertEquals(0x1234, val2, "The earlier pushed value should be popped after");

        assertEquals(0xFFFE, cpu.getRegisters().getSP(), "SP should return to 0xFFFE after popping 2 words");
    }

    @Test
    void testPeek() {
        cpu.getRegisters().setSP(0xFFFE); // Start SP at 0xFFFE

        stack.push(0x1234);

        int peekedValue = stack.peek();
        assertEquals(0x1234, peekedValue, "Peek should return the value at the top of the stack");

        assertEquals(0xFFFC, cpu.getRegisters().getSP(), "SP should not change after peek");
    }

    @Test
    void testPushWraparound() {
        cpu.getRegisters().setSP(0x0000); // Start SP at 0x0000 (to test wraparound)

        stack.push(0xBEEF);

        assertEquals(0xFFFE, cpu.getRegisters().getSP(), "SP should wrap around to 0xFFFE after decrementing from 0x0000");
        assertEquals(0xBEEF, memory.readWord(0xFFFE), "Value should be stored at SP location");
    }

    @Test
    void testPopWraparound() {
        cpu.getRegisters().setSP(0xFFFE); // Start SP at 0xFFFE

        stack.push(0xBEEF);
        cpu.getRegisters().setSP(0x0000); // Force SP to 0x0000

        int value = stack.pop();

        assertEquals(0xBEEF, value, "The value popped should match the pushed value");
        assertEquals(0x0002, cpu.getRegisters().getSP(), "SP should increment to 0x0002 after popping from 0x0000");
    }

    @Test
    void testPushMultipleValuesWraparound() {
        cpu.getRegisters().setSP(0x0004); // Start SP near the bottom to trigger wraparound

        stack.push(0xABCD);
        stack.push(0x1234);

        assertEquals(0xFFFE, cpu.getRegisters().getSP(), "SP should wrap to 0xFFFE when pushing with SP=0x0002");
        assertEquals(0x1234, memory.readWord(0xFFFE), "Second value pushed should be at 0xFFFE");
        assertEquals(0xABCD, memory.readWord(0x0000), "First value pushed should be at 0x0000");
    }


    @Test
    void testMultiplePushPopSequence() {
        cpu.getRegisters().setSP(0xFFFE); // Start SP at 0xFFFE

        stack.push(0x1111);
        stack.push(0x2222);
        stack.push(0x3333);

        assertEquals(0xFFF8, cpu.getRegisters().getSP(), "SP should be 0xFFF8 after 3 pushes");

        int val1 = stack.pop();
        assertEquals(0x3333, val1, "The first value popped should be 0x3333");

        stack.push(0x4444);
        assertEquals(0xFFF8, cpu.getRegisters().getSP(), "SP should be 0xFFF8 after popping and pushing");

        int val2 = stack.pop();
        assertEquals(0x4444, val2, "The next value popped should be 0x4444");

        int val3 = stack.pop();
        assertEquals(0x2222, val3, "The next value popped should be 0x2222");

        int val4 = stack.pop();
        assertEquals(0x1111, val4, "The last value popped should be 0x1111");

        assertEquals(0xFFFE, cpu.getRegisters().getSP(), "SP should return to 0xFFFE after all pops");
    }
}
