package org.mochaboy.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.mochaboy.FrameBuffer;

import java.io.IOException;

public class GuiFxEmulator extends Application {
    private final FrameBuffer frameBuffer;
    private final GuiFxDisplay display;
    private int width;
    private int height;

    public GuiFxEmulator(FrameBuffer frameBuffer){
        this.frameBuffer = frameBuffer;
        display = new GuiFxDisplay(frameBuffer);
        init();
    }

    public void init(){
        width = display.getWidth() * display.getScale();
        height = display.getHeight() * display.getScale();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(GuiFxEmulator.class.getResource("views/GuiFxEmulatorView.fxml"));

        Group root = new Group();
        Scene scene = new Scene(root, width, height, Color.rgb(40, 40, 40));

        Image icon = new Image("icon.png");
        Image background = new Image("icon.png");

        ImageView backgroundView = new ImageView(background);
        ImageView lcdView = new ImageView(display.getWritableImage());

        backgroundView.setSmooth(true);
        backgroundView.setFitWidth(scene.getWidth());
        backgroundView.setFitHeight(scene.getHeight());
        backgroundView.setPreserveRatio(true);
        backgroundView.setX((scene.getWidth() / 2) - (background.getWidth()*0.215));
        backgroundView.setOpacity(0.10);

        lcdView.setSmooth(false);
        lcdView.setFitWidth(160*display.getScale());
        lcdView.setPreserveRatio(true);

        root.getChildren().addAll(backgroundView, lcdView);

        stage.getIcons().add(icon);
        stage.setTitle("MochaBoy");
        stage.setResizable(false);

        stage.setScene(scene);
        stage.show();
    }

    public void run(){
        Platform.runLater(display::updateFrame);
    }

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public GuiFxDisplay getDisplay() {
        return display;
    }
}
