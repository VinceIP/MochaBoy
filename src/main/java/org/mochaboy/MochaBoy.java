package org.mochaboy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mochaboy.gui.GuiFxEmulator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MochaBoy extends Application {
    public static void main(String[] args) {

        String romFile = "roms/cpu_instrs.gb";

        Path path = Paths.get(romFile);
        try {
            Cartridge cartridge = new Cartridge(path);
            //Memory memory = new Memory(cartridge);
            //FrameBuffer frameBuffer = new FrameBuffer(160, 144);
            //GuiFxEmulator gui = new GuiFxEmulator();
            //GuiSwingEmulator gui = new GuiSwingEmulator(frameBuffer);
            //GuiGlEmulator gui = new GuiGlEmulator(frameBuffer);
            //PPU ppu = new PPU(memory, frameBuffer, gui.getDisplay());
            //CPU cpu = new CPU(ppu, memory);
            //memory.setCpu(cpu);
            //ppu.setCPU(cpu);
            //cpu.start();
            //gui.run();
            //cpu.stopCPU();
        } catch (IOException e) {
            System.out.println("IOException reading cart.");
        }

        launch();

    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GuiFxEmulator.class.getResource("views/GuiFxEmulatorView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        stage.setTitle("MochaBoy");
        stage.setScene(scene);
        stage.show();
    }
}