package org.mochaboy;

import javafx.application.Application;
import javafx.stage.Stage;
import org.mochaboy.gui.fx.GuiFxEmulator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MochaBoy extends Application {

    static GuiFxEmulator gui;
    public static void main(String[] args) {

        String romFile = "roms/tetris.gb";

        Path path = Paths.get(romFile);
        try {
            Cartridge cartridge = new Cartridge(path);
            Memory memory = new Memory(cartridge);
            FrameBuffer frameBuffer = new FrameBuffer(160, 144);
            gui = new GuiFxEmulator(frameBuffer);
            //GuiSwingEmulator gui = new GuiSwingEmulator(frameBuffer);
            //GuiGlEmulator gui = new GuiGlEmulator(frameBuffer);
            PPU ppu = new PPU(memory, frameBuffer, gui.getDisplay());
            CPU cpu = new CPU(ppu, memory);
            memory.setCpu(cpu);
            ppu.setCPU(cpu);
            cpu.start();
            //gui.run();
            //cpu.stopCPU();
        } catch (IOException e) {
            System.out.println("IOException reading cart.");
        }

        launch();

    }

    @Override
    public void start(Stage stage) throws Exception {
        gui.start(stage);
    }
}