Old Opcode System Archive
=========================

This folder contains the pre-rewrite CPU/opcode implementation for reference only.

It is intentionally outside `src/main` and `src/test`, so it is not part of the active Gradle build and may not compile.

Useful reference areas:

- `src/main/java/org/mochaboy/CPU.java`: old execution loop and opcode stepping model
- `src/main/java/org/mochaboy/opcode/OpcodeBuilder.java`: old JSON-driven opcode-to-micro-op builder
- `src/main/java/org/mochaboy/opcode/operations/`: old micro-operation classes
- `src/main/java/org/mochaboy/opcode/FlagCalculator.java`: old flag formulas
- `src/main/resources/system/Opcodes.json`: old external opcode metadata
- `src/test/java/`: old opcode behavior tests
