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
    }

    private void registerArithmeticCalculators() {
        //ADD
        calculators.put("ADD", (xVal, yVal, operands) -> {
            FlagConditions conditions = new FlagConditions();



            return conditions;
        });
    }

    private void registerBitOperationCalculators() {

    }
}

@FunctionalInterface
interface FlagConditionCalculator {
    FlagConditions calculate(int xVal, int yVal, Operand[] operands);
}