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
    private final GuiFxDisplay display;
    private int width;
    private int height;

    public GuiFxEmulator() {
        display = new GuiFxDisplay();
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
        stage.setWidth((display.getWidth()) * display.getScale());
        stage.setHeight((display.getHeight() * display.getScale()));
        stage.show();
    }


    public GuiFxDisplay getDisplay() {
        return display;
    }
}
