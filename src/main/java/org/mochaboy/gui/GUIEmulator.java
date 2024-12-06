package org.mochaboy.gui;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GUIEmulator {

    private long window;

    public GUIEmulator() {

    }

    public void run() {
        init();
        loop();

        //Free window callbacks, destroy window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        //Terminate, free error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
        //Error callback
        GLFWErrorCallback.createPrint(System.err).set();

        //Init GLFW
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        //Config window properties
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        //create window
        window = glfwCreateWindow(1024, 768, "MochaBoy", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create GLFW window");

        //key callback - called every time key is pressed, repeated, released
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); //Window should close on release ESC
        });

        //Get thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidMode.width() - pWidth.get(0)) / 2,
                    (vidMode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);


    }

    public void loop() {
        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //Clear framebuffer
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

    }
}
