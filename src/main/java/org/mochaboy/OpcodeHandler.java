package org.mochaboy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class OpcodeHandler {
    private Map<String, OpcodeInfo> opcodeMapUnprefixed;
    private Map<String, OpcodeInfo> opcodeMapPrefixed;
    private Map<String, BiConsumer<CPU, OpcodeInfo>> mnemonicMap;
    private FlagCalculator flagCalculator;

    public OpcodeHandler(OpcodeWrapper opcodeWrapper) {
        this.opcodeMapUnprefixed = opcodeWrapper.getUnprefixed();
        this.opcodeMapPrefixed = opcodeWrapper.getCbprefixed();
        flagCalculator = new FlagCalculator();
        mapMnemonics();
    }

    public int execute(CPU cpu, OpcodeInfo opcodeInfo) {
        //do logic on cpu
        mnemonicMap.get(opcodeInfo.getMnemonic()).accept(cpu, opcodeInfo);
        return opcodeInfo.getCycles()[0];
        //inc PC based on opcode info
        //add to cpu timer based on opcode info
        //consider increasing t states during specific operations (reading and writing) for accuracy instead?
    }

    private void mapMnemonics() {
        this.mnemonicMap = new HashMap<>();
        mnemonicMap.put("ADC", this::ADC);
        mnemonicMap.put("ADD", this::ADD);
        mnemonicMap.put("AND", this::AND);
        mnemonicMap.put("BIT", this::BIT);
        mnemonicMap.put("CALL", this::CALL);
        mnemonicMap.put("CCF", this::CCF);
        mnemonicMap.put("CP", this::CP);
        mnemonicMap.put("CPL", this::CPL);
        mnemonicMap.put("DAA", this::DAA);
        mnemonicMap.put("DEC", this::DEC);
        mnemonicMap.put("DI", this::DI);
        mnemonicMap.put("EI", this::EI);
        mnemonicMap.put("HALT", this::HALT);
        mnemonicMap.put("INC", this::INC);
        mnemonicMap.put("JP", this::JP);
        mnemonicMap.put("JR", this::JR);
        mnemonicMap.put("LD", this::LD);
        mnemonicMap.put("LDH", this::LDH);
        mnemonicMap.put("NOP", this::NOP);
        mnemonicMap.put("OR", this::OR);
        mnemonicMap.put("POP", this::POP);
        mnemonicMap.put("PUSH", this::PUSH);
        mnemonicMap.put("RES", this::RES);
        mnemonicMap.put("RET", this::RET);
        mnemonicMap.put("RETI", this::RETI);
        mnemonicMap.put("RL", this::RL);
        mnemonicMap.put("RLA", this::RLA);
        mnemonicMap.put("RLC", this::RLC);
        mnemonicMap.put("RLCA", this::RLCA);
        mnemonicMap.put("RR", this::RR);
        mnemonicMap.put("RRA", this::RRA);
        mnemonicMap.put("RRC", this::RRC);
        mnemonicMap.put("RRCA", this::RRCA);
        mnemonicMap.put("RST", this::RST);
        mnemonicMap.put("SBC", this::SBC);
        mnemonicMap.put("SCF", this::SCF);
        mnemonicMap.put("SET", this::SET);
        mnemonicMap.put("SLA", this::SLA);
        mnemonicMap.put("SRA", this::SRA);
        mnemonicMap.put("SRL", this::SRL);
        mnemonicMap.put("STOP", this::STOP);
        mnemonicMap.put("SUB", this::SUB);
        mnemonicMap.put("SWAP", this::SWAP);
        mnemonicMap.put("XOR", this::XOR);
    }

    /**
     * Add carry - Add value of y plus carry flag to x
     *
     * @param cpu
     * @param opcodeInfo
     */
    private void ADC(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];
        int xVal = cpu.getRegisters().getA() & 0xFF;
        int yVal;

        if (yOpr.getName().equals("n8")) {
            //Get immediate 8-bit value
            yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
            cpu.getRegisters().incrementPC();
        } else if (yOpr.getName().equals("HL")) {
            //get 16-bit value in [HL]
            yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        } else {
            //Otherwise, get the 8-bit registered specified in the operand
            yVal = cpu.getRegisters().getByName(yOpr.getName()) & 0xFF;
        }

        //Add result plus the current carry bit
        int carry = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
        int result = xVal + yVal + carry;

        processFlags(cpu, opcodeInfo, xVal, yVal);
        applyResult(cpu, xOpr.getName(), result);
    }

    /**
     * Add
     *
     * @param cpu
     * @param opcodeInfo
     */
    private void ADD(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];

        int xVal = cpu.getRegisters().getByName(xOpr.getName());
        int yVal;
        int pc = cpu.getRegisters().getPC();
        Registers registers = cpu.getRegisters();
        Memory memory = cpu.getMemory();
        switch (yOpr.getName()) {
            case "n8":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC() + 1);
                cpu.getRegisters().incrementPC();
                break;
            case "e8":
                yVal = cpu.getMemory().readUnsignedByte(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC();
                break;
            case "HL":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
                break;
            default:
                yVal = cpu.getRegisters().getByName(yOpr.getName());
                break;
        }
        int result = xVal + yVal;
        processFlags(cpu, opcodeInfo, xVal, yVal);
        applyResult(cpu, xOpr.getName(), result);
    }

    private void AND(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];

        int xVal = cpu.getRegisters().getByName(xOpr.getName());
        int yVal;

        switch (yOpr.getName()) {
            case "n8":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC();
                break;
            case "HL":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
                break;
            default:
                yVal = cpu.getRegisters().getByName(yOpr.getName());
                break;
        }
        int result = (xVal & yVal) & 0xFF;
        processFlags(cpu, opcodeInfo, xVal, yVal);
        applyResult(cpu, xOpr.getName(), result);
    }

    private void BIT(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];
        //Get u3 from the opcode
        int xVal = (opcodeInfo.getOpcode() >> 3) & 0x07;
        //cpu.getRegisters().incrementPC();
        int yVal;
        if (yOpr.getName().equals("HL")) yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        else yVal = cpu.getRegisters().getByName(yOpr.getName());

        processFlags(cpu, opcodeInfo, xVal, yVal);
    }

    private void CALL(CPU cpu, OpcodeInfo opcodeInfo) {
        // Read the address to jump to (16-bit) from PC+1 and PC+2
        int callAddress = cpu.getMemory().readWord(cpu.getRegisters().getPC() + 1);
        // Calculate the return address as the current PC + 3 (length of the CALL instruction)
        int returnAddress = cpu.getRegisters().getPC() + 3;

        boolean shouldCall = true;

        // Check for conditional CALL
        if (opcodeInfo.getOperands().length > 1) {
            Operand xOpr = opcodeInfo.getOperands()[0];
            String condition = xOpr.getName(); // "Z", "NZ", "C", "NC"

            switch (condition) {
                case "Z":
                    shouldCall = cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                    break;
                case "NZ":
                    shouldCall = !cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                    break;
                case "C":
                    shouldCall = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                    break;
                case "NC":
                    shouldCall = !cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                    break;
            }
        }

        if (shouldCall) {
            // Push the return address onto the stack
            cpu.getStack().push(returnAddress);
            // Set the PC to the call address
            cpu.getRegisters().setPC(callAddress);
            // Indicate that a jump occurred
            cpu.setDidJump(true);
        }
        // If the condition is not met, we do nothing here;
        // The PC will be incremented correctly in the CPU's run() method because didJump is false
    }

    private void CCF(CPU cpu, OpcodeInfo opcodeInfo) {
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, !cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }

    /**
     * Subtract value in yOpr from xOpr and set flags accordingly, but don't store the result. For comparing values.
     *
     * @param cpu
     * @param opcodeInfo
     */
    private void CP(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];

        int xVal = cpu.getRegisters().getByName(xOpr.getName());
        int yVal;
        switch (yOpr.getName()) {
            case "n8":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC() + 1);
                cpu.getRegisters().incrementPC();
                break;
            case "HL":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
                cpu.getRegisters().incrementPC();
                break;
            default:
                yVal = cpu.getRegisters().getByName(yOpr.getName());
                break;
        }

        processFlags(cpu, opcodeInfo, xVal, yVal);
    }

    private void CPL(CPU cpu, OpcodeInfo opcodeInfo) {
        cpu.getRegisters().setA((~cpu.getRegisters().getA()) & 0xFF);
        processFlags(cpu, opcodeInfo, 0, 0);
    }

    private void DAA(CPU cpu, OpcodeInfo opcodeInfo) {
        //I don't get it. Thanks, ChatGPT.
        int A = cpu.getRegisters().getA();
        boolean n = cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT);
        boolean h = cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY);
        boolean c = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);

        if (!n) {
            if (c || A > 0x99) {
                A += 0x60;
                c = true;
            }
            if (h || (A & 0x0F) > 9) {
                A += 0x06;
            }
        } else {
            if (c) {
                A -= 0x60;
            }
            if (h) {
                A -= 0x06;
            }
        }

        A &= 0xFF;
        cpu.getRegisters().setA(A);

        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, A == 0);
        cpu.getRegisters().clearFlag(Registers.FLAG_HALF_CARRY);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, c);
    }

    private void DEC(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        String registerName = xOpr.getName();

        if (registerName.equals("HL")) {
            // DEC (HL)
            int value = cpu.getMemory().readByte(cpu.getRegisters().getHL());
            int result = (value - 1) & 0xFF;
            cpu.getMemory().writeByte(cpu.getRegisters().getHL(), result);
            processFlags(cpu, opcodeInfo, value, 1);
        } else if (is8BitRegister(registerName)) {
            // 8-bit register decrement
            int value = cpu.getRegisters().getByName(registerName);
            int result = (value - 1) & 0xFF;
            cpu.getRegisters().setByName(registerName, result);
            processFlags(cpu, opcodeInfo, value, 1);
        } else {
            // 16-bit register decrement (no flags affected)
            int value = cpu.getRegisters().getByName(registerName);
            int result = (value - 1) & 0xFFFF;
            cpu.getRegisters().setByName(registerName, result);
        }
    }

    private void DI(CPU cpu, OpcodeInfo opcodeInfo) {
        cpu.setIME(false);
    }

    private void EI(CPU cpu, OpcodeInfo opcodeInfo) {
        if (!cpu.isIME()) cpu.setPendingInterruptSwitch(true);
    }

    private void HALT(CPU cpu, OpcodeInfo opcodeInfo) {
        if (!cpu.isLowPowerMode()) cpu.setLowPowerMode(true);
    }

    private void INC(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int value;

        //For 16-bit registers
        if (!is8BitRegister(xOpr.getName()) && xOpr.isImmediate()) {
            value = cpu.getRegisters().getByName(xOpr.getName());
            int result = (value + 1) & 0xFFFF;
            cpu.getRegisters().setByName(xOpr.getName(), result);
            return; // No need to modify flags for 16-bit INC
        }
        if (!xOpr.isImmediate()) {
            // probably means INC [HL])
            value = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        } else {
            // Otherwise, it’s a normal 8-bit register
            value = cpu.getRegisters().getByName(xOpr.getName());
        }

        int result = (value + 1) & 0xFF; // 8-bit subtraction with wrap-around
        processFlags(cpu, opcodeInfo, value, 1);

        if (!xOpr.isImmediate()) {
            // Write back to memory at address [HL]
            cpu.getMemory().writeByte(cpu.getRegisters().getHL(), result);
        } else {
            // Write back to a standard 8-bit register
            cpu.getRegisters().setByName(xOpr.getName(), result);
        }
    }

    private void JP(CPU cpu, OpcodeInfo opcodeInfo) {
        int address = cpu.getMemory().readWord(cpu.getRegisters().getPC());
        cpu.getRegisters().incrementPC(2);
        boolean shouldJump = false;
        if (opcodeInfo.getOperands().length > 1) {
            Operand xOpr = opcodeInfo.getOperands()[0];
            String condition = xOpr.getName(); // "Z", "NZ", "C", "NC"

            shouldJump = switch (condition) {
                case "Z" -> cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                case "NZ" -> !cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                case "C" -> cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                case "NC" -> !cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                default -> false;
            };
        }
        if (shouldJump) {
            cpu.getRegisters().setPC(address);
            cpu.setDidJump(true);
        }
    }

    private void JR(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr;
        boolean shouldJump = false;
        byte e8 = (byte) cpu.getMemory().readByte(cpu.getRegisters().getPC() + 1);
        cpu.getRegisters().incrementPC(1);
        if (opcodeInfo.getOperands().length > 1) {
            shouldJump = switch (xOpr.getName()) {
                case "Z" -> cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                case "NZ" -> !cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                case "C" -> cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                case "NC" -> !cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                default -> false;
            };
        } else shouldJump = true;

        if (shouldJump) {
            cpu.getRegisters().setPC(((cpu.getRegisters().getPC() + e8) + 1) & 0xFFFF);
            cpu.setDidJump(true);
        }

    }

    private void LD(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand destOpr = opcodeInfo.getOperands()[0];
        Operand sourceOpr = opcodeInfo.getOperands()[1];
        int value = 0;
        int basePC = cpu.getRegisters().getPC();
        incDec incDecState = incDec.NULL;

        // Get value to be copied from Y operand
        switch (sourceOpr.getName()) {
            // Given 8-bit value
            case "n8":
                value = cpu.getMemory().readByte(cpu.getRegisters().getPC() + 1);
                cpu.getRegisters().incrementPC();
                break;
            // [n16] or n16
            case "n16":
                // Is value immediate or a pointer?
                if (sourceOpr.isImmediate())
                    value = cpu.getMemory().readWord(cpu.getRegisters().getPC() + 1);
                else
                    value = cpu.getMemory().readWord(cpu.getRegisters().getByName(sourceOpr.getName()));
                cpu.getRegisters().incrementPC(2);
                break;
            case "a16":
                // Load from absolute 16-bit address
                int address = cpu.getMemory().readWord(cpu.getRegisters().getPC() + 1);
                value = cpu.getMemory().readByte(address);
                cpu.getRegisters().incrementPC(2);
                break;
            // [r16], r16, or SP+e8
            case "AF":
            case "BC":
            case "DE":
            case "HL":
                if (sourceOpr.isIncrement())
                    incDecState = incDec.INC_RIGHT;
                else if (sourceOpr.isDecrement())
                    incDecState = incDec.DEC_RIGHT;
                if (!sourceOpr.isImmediate())
                    value = cpu.getMemory().readByte(cpu.getRegisters().getByName(sourceOpr.getName()));
                else
                    value = cpu.getRegisters().getByName(sourceOpr.getName());
                break;
            case "SP":
                // LD HL, SP+e8
                if (opcodeInfo.getOperands().length > 2) {
                    byte e8 = (byte) cpu.getMemory().readByte(basePC + 1);
                    cpu.getRegisters().incrementPC();
                    int sp = cpu.getRegisters().getSP();
                    value = (sp + e8) & 0xFFFF;
                    processFlags(cpu, opcodeInfo, sp, e8); // Flags are set only here
                }
                break;
            // r8
            default:
                value = cpu.getRegisters().getByName(sourceOpr.getName()) & 0xFF;
                break;
        }

        // Copy value to target location
        switch (destOpr.getName()) {
            case "n16":
            case "a16":
                // Is always an address
                int targetAddress = cpu.getMemory().readWord(basePC); // Read the value before PC was inc above
                cpu.getMemory().writeWord(targetAddress, value);
                break;
            // [r16]
            case "AF":
            case "BC":
            case "DE":
            case "HL":
                if (destOpr.isIncrement())
                    incDecState = incDec.INC_LEFT;
                else if (destOpr.isDecrement())
                    incDecState = incDec.DEC_LEFT;
                if (!destOpr.isImmediate()) {
                    cpu.getMemory().writeWord(cpu.getRegisters().getByName(destOpr.getName()), value);
                } else
                    cpu.getRegisters().setByName(destOpr.getName(), (value &
                            (is8BitRegister(destOpr.getName()) ? 0xFF : 0xFFFF)));
                break;
            case "SP":
                if (destOpr.isImmediate()) {
                    cpu.getRegisters().setByName(destOpr.getName(), (value &
                            (is8BitRegister(destOpr.getName()) ? 0xFF : 0xFFFF)));
                } else
                    cpu.getMemory().writeWord(cpu.getRegisters().getByName(destOpr.getName()), value);
                break;
            default:
                if (is8BitRegister(destOpr.getName()))
                    cpu.getRegisters().setByName(destOpr.getName(), value & 0xFF);
                else
                    cpu.getRegisters().setByName(destOpr.getName(), value & 0xFFFF);
                break;
        }

        if (incDecState != incDec.NULL) {
            switch (incDecState) {
                case DEC_LEFT:
                case DEC_RIGHT:
                    cpu.getRegisters().setByName("HL", (cpu.getRegisters().getHL() - 1) & 0xFFFF);
                    break;
                case INC_LEFT:
                case INC_RIGHT:
                    cpu.getRegisters().setByName("HL", (cpu.getRegisters().getHL() + 1) & 0xFFFF);
                    break;
            }
        }
    }

    private void LDH(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];
        int a8 = cpu.getMemory().readByte(cpu.getRegisters().getPC() + 1);
        cpu.getRegisters().incrementPC();
        if (xOpr.getName().equals("a8")) {
            int address = 0xFF00 + (a8 & 0xFF);
            cpu.getMemory().writeByte(address, cpu.getRegisters().getA());
        } else {
            int value = cpu.getMemory().readByte(0xFF00 + a8);
            cpu.getRegisters().setA(value);
        }
    }

    private void NOP(CPU cpu, OpcodeInfo opcodeInfo) {
        //
    }

    private void OR(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand yOpr = opcodeInfo.getOperands()[1];
        int xVal = cpu.getRegisters().getA();
        int yVal;
        switch (yOpr.getName()) {
            case "n8":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC();
                break;
            case "HL":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
                break;
            default:
                yVal = cpu.getRegisters().getByName(yOpr.getName());
                break;
        }

        int result = xVal | yVal;

        processFlags(cpu, opcodeInfo, xVal, yVal);
        applyResult(cpu, "A", result);
    }

    private void POP(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int poppedVal = cpu.getStack().pop();
        //Flags only affected with AF
        if (xOpr.getName().equals("AF")) {
            processFlags(cpu, opcodeInfo, poppedVal, 0);
        }
        applyResult(cpu, xOpr.getName(), poppedVal);
    }

    private void PUSH(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int pushVal = cpu.getRegisters().getByName(xOpr.getName());
        cpu.getStack().push(pushVal);
    }

    private void RES(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];
        int target;
        int u3 = (opcodeInfo.getOpcode() >> 3) & 0b111;
        if (yOpr.getName().equals("HL")) {
            target = cpu.getMemory().readByte(cpu.getRegisters().getHL());
            cpu.getRegisters().incrementPC();
            int result = target & ~(1 << u3);
            cpu.getMemory().writeByte(cpu.getRegisters().getHL(), result);
        } else {
            target = cpu.getRegisters().getByName(yOpr.getName());
            int result = target & ~(1 << u3);
            applyResult(cpu, yOpr.getName(), result);
        }

    }

    private void RET(CPU cpu, OpcodeInfo opcodeInfo) {
        int returnAddr = cpu.getStack().pop();
        boolean shouldRet = true;
        if (opcodeInfo != null && opcodeInfo.getOperands().length > 0) {
            Operand xOpr = opcodeInfo.getOperands()[0];
            String condition = xOpr.getName(); // "Z", "NZ", "C", "NC"
            switch (condition) {
                case "Z":
                    shouldRet = cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                    break;
                case "NZ":
                    shouldRet = !cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                    break;
                case "C":
                    shouldRet = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                    break;
                case "NC":
                    shouldRet = !cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                    break;
            }
        }
        if (shouldRet) {
            cpu.getRegisters().setPC(returnAddr);
            cpu.setDidJump(true);
        }
        //else cpu.getRegisters().incrementPC();
    }

    private void RETI(CPU cpu, OpcodeInfo opcodeInfo) {
        EI(cpu, null);
        RET(cpu, null);
    }

    private void RL(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int toRotate;
        if (xOpr.getName().equals("HL")) {
            toRotate = cpu.getMemory().readByte(cpu.getRegisters().getHL());
            cpu.getRegisters().incrementPC();
            int b7 = (toRotate >> 7) & 0x1;
            int carryFlag = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
            int rotated = ((toRotate << 1) | carryFlag) & 0xFF;
            processFlags(cpu, opcodeInfo, rotated, b7);
            cpu.getMemory().writeByte(cpu.getRegisters().getHL(), rotated);
        } else {
            toRotate = cpu.getRegisters().getByName(xOpr.getName());
            int b7 = (toRotate >> 7) & 0x1;
            int carryFlag = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
            int rotated = ((toRotate << 1) | carryFlag) & 0xFF;
            processFlags(cpu, opcodeInfo, rotated, b7);
            applyResult(cpu, xOpr.getName(), rotated);
        }
    }

    private void RLA(CPU cpu, OpcodeInfo opcodeInfo) {
        int toRotate = cpu.getRegisters().getA();
        int carryFlag = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
        int b7 = (toRotate >> 7) & 0x1;
        int rotated = ((toRotate << 1) | carryFlag) & 0xFF;
        processFlags(cpu, opcodeInfo, rotated, b7);
        applyResult(cpu, "A", rotated);
    }

    private void RLC(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int toRotate;
        boolean toMem = false;
        if (xOpr.getName().equals("HL")) {
            toMem = true;
            toRotate = cpu.getMemory().readByte(cpu.getRegisters().getHL());
            cpu.getRegisters().incrementPC();
        } else toRotate = cpu.getRegisters().getByName(xOpr.getName());
        int b7 = (toRotate >> 7) & 0x1;
        toRotate = ((toRotate << 1 | b7) & 0xFF);
        processFlags(cpu, opcodeInfo, toRotate, b7);
        if (toMem) cpu.getMemory().writeByte(cpu.getRegisters().getHL(), toRotate & 0xFF);
        else applyResult(cpu, xOpr.getName(), toRotate);

    }

    private void RLCA(CPU cpu, OpcodeInfo opcodeInfo) {
        int toRotate = cpu.getRegisters().getA();
        int b7 = (toRotate >> 7) & 0x1;
        int rotated = ((toRotate << 1) | b7) & 0xFF;
        processFlags(cpu, opcodeInfo, rotated, b7);
        applyResult(cpu, "A", rotated);
    }

    private void RR(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int toRotate;
        boolean toMem = false;
        if (xOpr.getName().equals("HL")) {
            toMem = true;
            toRotate = cpu.getMemory().readByte(cpu.getRegisters().getHL());
            cpu.getRegisters().incrementPC();
        } else {
            toRotate = cpu.getRegisters().getByName(xOpr.getName());
        }
        int b0 = toRotate & 0x1;
        int carryFlag = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
        int rotated = ((toRotate >> 1) | (carryFlag << 7)) & 0xFF;
        processFlags(cpu, opcodeInfo, rotated, b0);

        if (toMem) cpu.getMemory().writeByte(cpu.getRegisters().getHL(), rotated);
        else applyResult(cpu, xOpr.getName(), rotated);

    }

    private void RRA(CPU cpu, OpcodeInfo opcodeInfo) {
        int toRotate = cpu.getRegisters().getA();
        int b0 = toRotate & 0x1;
        int carryFlag = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
        int rotated = ((toRotate >> 1) | (carryFlag << 7)) & 0xFF;
        processFlags(cpu, opcodeInfo, rotated, b0);
        applyResult(cpu, "A", rotated);
    }

    private void RRC(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int toRotate;
        boolean toMem = false;
        if (xOpr.getName().equals("HL")) {
            toMem = true;
            toRotate = cpu.getMemory().readByte(cpu.getRegisters().getHL());
            cpu.getRegisters().incrementPC();
        } else {
            toRotate = cpu.getRegisters().getByName(xOpr.getName());
        }
        int b0 = toRotate & 0x1;
        int rotated = ((toRotate >> 1) | b0 << 7) & 0xFF;
        processFlags(cpu, opcodeInfo, rotated, b0);
        if (toMem) cpu.getMemory().writeByte(cpu.getRegisters().getHL(), rotated);
        else applyResult(cpu, xOpr.getName(), rotated);
    }

    private void RRCA(CPU cpu, OpcodeInfo opcodeInfo) {
        int toRotate = cpu.getRegisters().getA();
        int b0 = toRotate & 0x1;
        int rotated = ((toRotate >> 1) | b0 << 7) & 0xFF;
        processFlags(cpu, opcodeInfo, rotated, b0);
        applyResult(cpu, "A", rotated);
    }

    private void RST(CPU cpu, OpcodeInfo opcodeInfo) {
        int callAddress = ((opcodeInfo.getOpcode() & 0x38) >> 3) * 8;
        int returnAddress = cpu.getRegisters().getPC() + opcodeInfo.getBytes();
        cpu.getStack().push(returnAddress);
        cpu.getRegisters().setPC(callAddress);
    }

    private void SBC(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand yOpr = opcodeInfo.getOperands()[1];
        int xVal = cpu.getRegisters().getA();
        int yVal;
        int carry = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
        switch (yOpr.getName()) {
            case "n8":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC();
                break;
            case "HL":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
                break;
            default:
                yVal = cpu.getRegisters().getByName(yOpr.getName());
                break;
        }
        int result = xVal - (yVal + carry);
        processFlags(cpu, opcodeInfo, xVal, yVal);
        applyResult(cpu, "A", result);
    }

    private void SCF(CPU cpu, OpcodeInfo opcodeInfo) {
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY);
    }

    private void SET(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand yOpr = opcodeInfo.getOperands()[1];
        //Get u3 from opcode - treat as a bit index
        int xVal = (opcodeInfo.getOpcode() >> 3) & 0x07;
        //cpu.getRegisters().incrementPC();
        int yVal;
        if (yOpr.getName().equals("HL")) yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        else yVal = cpu.getRegisters().getByName(yOpr.getName());
        int mask = 1 << xVal;
        int result = yVal | mask;
        if (yOpr.getName().equals("HL")) cpu.getMemory().writeByte(cpu.getRegisters().getHL(), result);
        else applyResult(cpu, yOpr.getName(), result);
    }

    private void SLA(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int toShift;
        boolean isMem = false;
        if (xOpr.getName().equals("HL")) {
            isMem = true;
            toShift = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        } else toShift = cpu.getRegisters().getByName(xOpr.getName());
        processFlags(cpu, opcodeInfo, toShift, 0);
        int result = (toShift << 1);
        if (isMem) cpu.getMemory().writeByte(cpu.getRegisters().getHL(), result & 0xFF);
        else applyResult(cpu, xOpr.getName(), result);
    }

    private void SRA(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int toShift;
        boolean isMem = false;
        if (xOpr.getName().equals("HL")) {
            isMem = true;
            toShift = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        } else toShift = cpu.getRegisters().getByName(xOpr.getName());
        processFlags(cpu, opcodeInfo, toShift, 0);
        int result = ((toShift >> 1) | (toShift & 0x80));
        if (isMem) cpu.getMemory().writeByte(cpu.getRegisters().getHL(), result & 0xFF);
        else applyResult(cpu, xOpr.getName(), result);
    }

    private void SRL(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int toShift;
        boolean isMem = false;
        if (xOpr.getName().equals("HL")) {
            isMem = true;
            toShift = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        } else toShift = cpu.getRegisters().getByName(xOpr.getName());
        processFlags(cpu, opcodeInfo, toShift, 0);
        int result = (toShift >> 1);
        if (isMem) cpu.getMemory().writeByte(cpu.getRegisters().getHL(), result & 0xFF);
        else applyResult(cpu, xOpr.getName(), result);
    }

    private void STOP(CPU cpu, OpcodeInfo opcodeInfo) {
        cpu.setStopMode(true);
    }

    private void SUB(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand yOpr = opcodeInfo.getOperands()[1];
        int xVal = cpu.getRegisters().getA();
        int yVal;
        switch (yOpr.getName()) {
            case "n8":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC();
                break;
            case "HL":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
                break;
            default:
                yVal = cpu.getRegisters().getByName(yOpr.getName());
                break;
        }
        int result = xVal - yVal;
        processFlags(cpu, opcodeInfo, xVal, yVal);
        applyResult(cpu, "A", result);
    }

    private void SWAP(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int xVal;
        boolean isMem = false;
        if (xOpr.getName().equals("HL")) {
            isMem = true;
            xVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        } else {
            xVal = cpu.getRegisters().getByName(xOpr.getName());
        }

        int result = ((xVal >> 4) | (xVal << 4));
        processFlags(cpu, opcodeInfo, xVal, 0);
        if (isMem) cpu.getMemory().writeByte(cpu.getRegisters().getHL(), result & 0xFF);
        else applyResult(cpu, xOpr.getName(), result);
    }

    private void XOR(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand yOpr = opcodeInfo.getOperands()[1];
        int xVal = cpu.getRegisters().getA();
        int yVal;
        switch (yOpr.getName()) {
            case "n8":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC();
                break;
            case "HL":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
                break;
            default:
                yVal = cpu.getRegisters().getByName(yOpr.getName());
                break;
        }
        int result = xVal ^ yVal;
        processFlags(cpu, opcodeInfo, xVal, yVal);
        applyResult(cpu, "A", result);
    }


    private void processFlags(CPU cpu, OpcodeInfo opcodeInfo, int xVal, int yVal) {
        FlagConditions conditions = flagCalculator.calculateFlags(cpu, opcodeInfo.getMnemonic(), xVal, yVal, opcodeInfo.getOperands());
        applyFlags(cpu, opcodeInfo.getFlags(), conditions);
    }

    /**
     * Apply flags to CPU based on calculated conditions or opcode info.
     * Flag states are set to their expected values when Opcodes.json indicates "0", "1", or "-" for flag states.
     * Otherwise, FlagCalculator determines the set flag.
     *
     * @param flags
     * @param conditions
     */
    private void applyFlags(CPU cpu, Flags flags, FlagConditions conditions) {
        //Zero flag
        switch (flags.getZ()) {
            //OpcodeInfo indicates this flag should be set based on conditions that were set in the flag calculator
            case "Z":
                cpu.getRegisters().setFlag(Registers.FLAG_ZERO, conditions.isZero);
                break;
            //Opcode info indicates this flag should always be cleared
            case "0":
                cpu.getRegisters().clearFlag(Registers.FLAG_ZERO);
                break;
            //Opcode info indicates this flags should always be set
            case "1":
                cpu.getRegisters().setFlag(Registers.FLAG_ZERO);
                break;
            //Opcode info could give "-", indicating nothing should change with this flag
        }

        // Subtract flag
        switch (flags.getN()) {
            case "N":
                cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT, conditions.isSubtract);
            case "0":
                cpu.getRegisters().clearFlag(Registers.FLAG_SUBTRACT);
                break;
            case "1":
                cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT);
                break;
            // "-" and other cases - do nothing
        }

        // Half-carry flag
        switch (flags.getH()) {
            case "H":
                cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY, conditions.isHalfCarry);
                break;
            case "0":
                cpu.getRegisters().clearFlag(Registers.FLAG_HALF_CARRY);
                break;
            case "1":
                cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY);
                break;
            // "-" case - do nothing
        }

        // Carry flag
        switch (flags.getC()) {
            case "C":
                cpu.getRegisters().setFlag(Registers.FLAG_CARRY, conditions.isCarry);
                break;
            case "0":
                cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);
                break;
            case "1":
                cpu.getRegisters().setFlag(Registers.FLAG_CARRY);
                break;
            // "-" case - do nothing
        }
    }

    public static boolean is8BitRegister(String register) {
        return (register.length() == 1 || register.equals("n8"));
    }

    private void applyResult(CPU cpu, String register, int result) {
        //Mask result depending on bit size of register
        cpu.getRegisters().setByName(register, (is8BitRegister(register) ? result & 0xFF : result & 0xFFFF));
    }

    private enum incDec {
        NULL,
        INC_LEFT,
        INC_RIGHT,
        DEC_LEFT,
        DEC_RIGHT
    }

}

class FlagConditions {
    boolean isZero;
    boolean isHalfCarry;
    boolean isCarry;
    boolean isSubtract;
}

