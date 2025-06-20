package org.mochaboy.gui.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.mochaboy.MochaBoy;

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
        } else {
            MochaBoy.setupEmulator(rom.getPath());
        }
    }

    @FXML
    public void onExit() {
        System.exit(0);
    }
}
