module org.mochaboy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    requires org.lwjgl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;

    opens   org.mochaboy to javafx.fxml, com.google.gson, org.lwjgl, org.lwjgl.glfw, org.lwjgl.opengl;
    exports org.mochaboy;
}
