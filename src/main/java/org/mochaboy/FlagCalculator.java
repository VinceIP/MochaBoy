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
                conditions.isZero = ((xVal - yVal) & 0xFF) == 0;
                conditions.isHalfCarry = ((xVal & 0xF0) < (yVal & 0xF0)) || ((xVal & 0xF) < (yVal & 0xF));
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