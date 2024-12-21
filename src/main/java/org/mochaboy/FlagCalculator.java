package org.mochaboy;

import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for getting conditions in how a register flag is set after an operation on the CPU.
 */
public class FlagCalculator {
    private Map<String, FlagConditionCalculator> calculators = new HashMap<>();

    public FlagCalculator() {
        registerArithmeticCalculators();
        registerBitOperationCalculators();
        registerComparisonCalculators();
        registerMiscCalculators();
    }

    private void registerArithmeticCalculators() {
        //ADD
        calculators.put("ADD", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            switch (operands[1].getName()) {
                //Immediate 8-bit values
                case "e8":
                    int lowerByte = xVal & 0xFF;
                    int e8 = yVal & 0xFF;
                    conditions.isHalfCarry = (((xVal & 0x0FFF) + (yVal & 0x0FFF)) & 0x1000) != 0;
                    conditions.isCarry = lowerByte + e8 > 0xFF;
                    break;
                //values of any 16-bit register
                case "AF":
                case "BC":
                case "DE":
                case "HL":
                case "SP":
                case "PC":
                    conditions.isZero = ((xVal + yVal) & 0xFF) == 0;
                    conditions.isHalfCarry = (((xVal & 0x0FFF) + (yVal & 0x0FFF)) & 0x1000) != 0;
                    conditions.isCarry = ((xVal & 0xFFFF) + (yVal & 0xFFFF)) > 0xFFFF;
                    break;
                default:
                    conditions.isZero = ((xVal + yVal) & 0xFF) == 0;
                    conditions.isHalfCarry = (xVal & 0xF) + (yVal & 0xF) > 0xF;
                    conditions.isCarry = (xVal + yVal) > 0xFF;
            }
            return conditions;
        });

        calculators.put("ADC", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            int carry = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
            conditions.isZero = ((xVal + yVal + carry) & 0xFF) == 0;
            conditions.isHalfCarry = (xVal & 0xF) + (yVal & 0xF) + carry > 0xF;
            conditions.isCarry = (xVal + yVal + carry) > 0xFF;
            return conditions;
        });

        calculators.put("DEC", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            //Flags only affected under these conditions
            if (OpcodeHandler.is8BitRegister(operands[0].getName()) || operands[0].getName().equals("HL")) {
                conditions.isZero = ((xVal - 1) & 0xFF) == 0; // Much simpler and accurate
                conditions.isHalfCarry = (xVal & 0xF) < 1;  // Correct half-carry logic
                conditions.isSubtract = true; // Forgot to set this flag
            }
            return conditions;
        });

        calculators.put("INC", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            if (OpcodeHandler.is8BitRegister(operands[0].getName()) || operands[0].getName().equals("HL")) {
                conditions.isZero = ((xVal + yVal) & 0xFF) == 0;
                conditions.isHalfCarry = (xVal & 0xF) + (yVal & 0xF) > 0xF;
            }
            return conditions;
        });

        calculators.put("SBC", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            int carry = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
            conditions.isZero = ((xVal - (yVal + carry)) & 0xFF) == 0;
            conditions.isHalfCarry = ((xVal ^ yVal ^ ((xVal - (yVal + carry)) & 0xFF)) & 0x10) != 0; //My brain
            conditions.isCarry = (yVal + carry) > cpu.getRegisters().getA();
            return conditions;
        });

        calculators.put("SUB", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isZero = ((xVal - (yVal)) & 0xFF) == 0;
            conditions.isHalfCarry = ((xVal ^ yVal ^ ((xVal - yVal) & 0xFF)) & 0x10) != 0; //My brain
            conditions.isCarry = (yVal > cpu.getRegisters().getA());
            return conditions;
        });
    }

    private void registerBitOperationCalculators() {
        calculators.put("AND", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isZero = ((xVal & yVal) & 0xFF) == 0;
            return conditions;
        });

        calculators.put("BIT", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isZero = (yVal & (1 << xVal)) == 0;
            return conditions;
        });

        calculators.put("OR", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isZero = ((xVal | yVal) & 0xFF) == 0;
            return conditions;
        });

        calculators.put("RL", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isZero = (xVal == 0);
            conditions.isCarry = (yVal == 1);
            return conditions;
        });

        calculators.put("RLA", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isCarry = (yVal == 1);
            return conditions;
        });

        calculators.put("RLC", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isZero = (xVal == 0);
            conditions.isCarry = (yVal == 1);
            return conditions;
        });

        calculators.put("RLCA", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isCarry = (yVal == 1);
            return conditions;
        });

        calculators.put("RR", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isZero = (xVal == 0);
            conditions.isCarry = (yVal == 1);
            return conditions;
        });

        calculators.put("RRA", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isCarry = (yVal == 1);
            return conditions;
        });

        calculators.put("RRC", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isZero = (xVal == 0);
            conditions.isCarry = (yVal == 1);
            return conditions;
        });

        calculators.put("RRCA", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isCarry = (yVal == 1);
            return conditions;
        });

        calculators.put("SLA", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            int result = (xVal << 1) & 0xFF;
            conditions.isZero = (result == 0);
            conditions.isCarry = ((xVal & 0x80) != 0);
            return conditions;
        });

        calculators.put("SRA", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            int result = (xVal >> 1) & 0xFF;
            conditions.isZero = (result == 0);
            conditions.isCarry = ((xVal & 0x01) != 0);
            return conditions;
        });

        calculators.put("SRL", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            int result = xVal >> 1;
            conditions.isZero = (result == 0);
            conditions.isCarry = ((xVal & 0x1) != 0);
            return conditions;
        });

        calculators.put("SWAP", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            int result = ((xVal >> 4) | (xVal << 4));
            conditions.isZero = (result == 0);
            return conditions;
        });

        calculators.put("XOR", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isZero = ((xVal ^ yVal) & 0xFF) == 0;
            return conditions;
        });
    }

    private void registerComparisonCalculators() {
        calculators.put("CP", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            conditions.isZero = ((xVal - yVal) & 0xFF) == 0;
            conditions.isHalfCarry = ((xVal & 0xF0) < (yVal & 0xF0)) || ((xVal & 0xF) < (yVal & 0xF));
            conditions.isCarry = (yVal > xVal);
            return conditions;
        });
    }


    private void registerMiscCalculators() {
        calculators.put("CPL", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            return conditions;
        });
        calculators.put("DAA", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            return conditions;
        });

        calculators.put("POP", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            int poppedVal = xVal & 0xF0; //Get low byte and zero out lower nibble
            //Flags are set based on bits in poppedVal
            cpu.getRegisters().setFlag(Registers.FLAG_ZERO,
                    (poppedVal & 0x80) > 0
            );
            cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT,
                    (poppedVal & 0x40) > 0);
            cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY,
                    (poppedVal & 0x20) > 0);
            cpu.getRegisters().setFlag(Registers.FLAG_CARRY,
                    (poppedVal & 0x10) > 0);
            return conditions;
        });

        calculators.put("LD", (cpu, xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();
            if (operands.length > 2) {
                int sp = xVal & 0xFFFF;
                int offset = (byte) yVal; // sign-extend e8
                int sum = (sp + offset) & 0xFFFF;
                conditions.isHalfCarry = ((sp ^ offset ^ sum) & 0x10) != 0;
                conditions.isCarry = ((sp ^ offset ^ sum) & 0x100) != 0;
                return conditions;
            } else {
                conditions.isHalfCarry = (((xVal & 0x0FFF) + (yVal & 0x0FFF)) & 0x1000) != 0;
                conditions.isCarry = ((xVal & 0xFFFF) + (yVal & 0xFFFF)) > 0xFFFF;
                return conditions;
            }
        });
    }

    public FlagConditions calculateFlags(CPU cpu, String mnemonic, int xVal, int yVal, Operand[] operands) {
        FlagConditionCalculator calculator = calculators.get(mnemonic);
        if (calculator == null) throw new IllegalArgumentException("No flag calculator for " + mnemonic);
        return calculator.calculate(cpu, xVal, yVal, operands);
    }
}

@FunctionalInterface
interface FlagConditionCalculator {
    FlagConditions calculate(CPU cpu, int xVal, int yVal, Operand[] operands);
}