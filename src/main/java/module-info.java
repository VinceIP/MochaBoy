module org.mochaboy {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.desktop;

    requires com.google.gson;

    requires static org.junit.jupiter.api;
    requires static org.junit.jupiter.params;

    requires org.lwjgl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;

    opens org.mochaboy;
    opens org.mochaboy.opcode;
    opens org.mochaboy.gui.fx;
    exports org.mochaboy;
    exports org.mochaboy.gui.fx to javafx.fxml;
}
