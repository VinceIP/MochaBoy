package org.mochaboy.gui.fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mochaboy.FrameBuffer;

public class GuiFxEmulator extends Application {
    private final FrameBuffer frameBuffer;
    private final GuiFxDisplay display;
    private int width;
    private int height;

    public GuiFxEmulator(FrameBuffer frameBuffer) {
        this.frameBuffer = frameBuffer;
        display = new GuiFxDisplay(frameBuffer);
        init();
    }

    public void init() {
        width = display.getWidth() * display.getScale();
        height = display.getHeight() * display.getScale();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                GuiFxEmulator.class.getResource("/org/mochaboy/gui/views/MochaBoy.fxml")
        );
        BorderPane rootPane = loader.load();
        GuiFxController controller = loader.getController();
        controller.setDisplay(display);

        Scene scene = new Scene(rootPane);
        stage.setScene(scene);


        stage.setResizable(false);
        stage.show();
    }

    public void run() {
        Platform.runLater(display::updateFrame);
    }

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public GuiFxDisplay getDisplay() {
        return display;
    }
}
