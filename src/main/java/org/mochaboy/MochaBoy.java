package org.mochaboy;

import org.mochaboy.gui.GuiSwingEmulator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MochaBoy {
    public static void main(String[] args) {

//        if (args.length == 0) {
//            System.out.println("MochaBoy usage: 'MochaBoy *.gb");
//            return;
//        } else {
        //String romFile = args[0];
        String romFile = "roms/moon/utils/bootrom_dumper.gb";
        Path path = Paths.get(romFile);
        try {
            Cartridge cartridge = new Cartridge(path);
            Memory memory = new Memory(cartridge);
            FrameBuffer frameBuffer = new FrameBuffer(160, 144);
            GuiSwingEmulator gui = new GuiSwingEmulator(frameBuffer);
            //GuiGlEmulator gui = new GuiGlEmulator(frameBuffer);
            PPU ppu = new PPU(memory, frameBuffer, gui.getDisplay());
            CPU cpu = new CPU(ppu, memory);
            memory.setCpu(cpu);
            ppu.setCPU(cpu);
            cpu.start();
            gui.run();
            //cpu.stopCPU();
        } catch (IOException e) {
            System.out.println("IOException reading cart.");
        }

        //}

        //new GUIEmulator().run();
    }
}