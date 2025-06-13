package org.mochaboy.gui.fx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class GuiFxController {

    @FXML
    private ImageView lcdView;
    @FXML
    private BorderPane rootPane;
    @FXML
    private MenuBar menuBar;

    public void setDisplay(GuiFxDisplay display) {

        lcdView.setImage(display.getWritableImage());
        lcdView.setPreserveRatio(true);
        lcdView.setSmooth(false);
        lcdView.fitWidthProperty().bind(display.scaledWidthProperty());
        lcdView.fitHeightProperty().bind(display.scaledHeightProperty());

        DoubleProperty lcdW = lcdView.fitWidthProperty();
        DoubleProperty lcdH = lcdView.fitHeightProperty();
        ReadOnlyDoubleProperty menuH = menuBar.heightProperty();

        rootPane.prefWidthProperty().bind(lcdW);
        rootPane.minWidthProperty().bind(lcdW);
        rootPane.maxWidthProperty().bind(lcdW);

        rootPane.prefHeightProperty().bind(lcdH.add(menuH));
        rootPane.minHeightProperty().bind(lcdH.add(menuH));
        rootPane.maxHeightProperty().bind(lcdH.add(menuH));

    }

    @FXML
    public void onLoadRom(ActionEvent evt) {
        Window owner = ((MenuItem) evt.getSource()).getParentPopup().getOwnerWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a Game Boy ROM");
        chooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Game Boy / Game Boy Color *.gb, *.gbc", "*.gb", "*.gbc")
        );
        File rom = chooser.showOpenDialog(owner);
        if (rom == null) {
            return;
        }
    }

    @FXML
    public void onExit() {
        System.exit(1);
    }
}
