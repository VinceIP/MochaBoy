module org.mochaboy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    requires org.lwjgl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;

    opens   org.mochaboy to javafx.fxml, com.google.gson, org.lwjgl, org.lwjgl.glfw, org.lwjgl.opengl;
    opens org.mochaboy.opcode to com.google.gson;
    opens org.mochaboy.gui.fx to javafx.fxml;
    exports org.mochaboy;
    exports org.mochaboy.gui.fx to javafx.fxml;
}
