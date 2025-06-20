package org.mochaboy;

import javafx.application.Application;
import javafx.stage.Stage;
import org.mochaboy.gui.fx.GuiFxEmulator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MochaBoy extends Application {

    static GuiFxEmulator gui;
    private static CPU currentCpu;

    public static void main(String[] args) {

        String romFile = "";
        gui = new GuiFxEmulator();
        setupEmulator(romFile);
        launch();

    }

    public static synchronized void setupEmulator(String romFile) {
        if (currentCpu != null && currentCpu.isAlive()) {
            currentCpu.stopCPU();
            try {
                currentCpu.join();
            } catch (InterruptedException ignored) {
            }
        }
        Path path = Paths.get(romFile);

        try {

            Cartridge cartridge = new Cartridge(path);
            Memory memory = new Memory(cartridge);
            FrameBuffer fb = gui.getDisplay().getFrameBuffer();
            PPU ppu = new PPU(memory, fb, gui.getDisplay());
            CPU cpu = new CPU(ppu, memory);

            memory.setCpu(cpu);
            ppu.setCPU(cpu);

            cpu.start();
            currentCpu = cpu;

        } catch (IOException e) {
            System.out.println("IOException reading cart.");
        }

    }

    @Override
    public void start(Stage stage) throws Exception {
        gui.start(stage);
    }
}