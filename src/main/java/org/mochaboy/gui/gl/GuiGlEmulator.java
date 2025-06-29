package org.mochaboy.gui.gl;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.mochaboy.FrameBuffer;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GuiGlEmulator {

    private long window;
    private final FrameBuffer frameBuffer;
    private int textureId; // Store the texture ID

    public GuiGlEmulator(FrameBuffer frameBuffer) {
        this.frameBuffer = frameBuffer;
    }

    public void run() {
        init();
        loop();

        // Free window callbacks, destroy window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate, free error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
        // Error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Init GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Config window properties
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        // create window
        window = glfwCreateWindow(1024, 768, "MochaBoy", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        // key callback - called every time key is pressed, repeated, released
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true); // Window should close on release ESC
            }
        });

        // Get thread stack and push a new frame
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

        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Create and bind the texture
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Allocate texture memory (only needed once, unless resizing)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, frameBuffer.getWidth(), frameBuffer.getHeight(),
                0, GL_RGBA, GL_UNSIGNED_BYTE, (IntBuffer) null); // Pass null for initial data
    }

    public void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear framebuffer

            int[] pixels = frameBuffer.getPixels();

            // Update the texture data using glTexSubImage2D()
            glBindTexture(GL_TEXTURE_2D, textureId); // You might not even need this here if the texture is already bound
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, frameBuffer.getWidth(), frameBuffer.getHeight(),
                    GL_RGBA, GL_UNSIGNED_BYTE, pixels);

            // Render a quad (using immediate mode for now)
            glBegin(GL_QUADS);
            glTexCoord2f(0, 0);
            glVertex2f(-1, 1);
            glTexCoord2f(1, 0);
            glVertex2f(1, 1);
            glTexCoord2f(1, 1);
            glVertex2f(1, -1);
            glTexCoord2f(0, 1);
            glVertex2f(-1, -1);
            glEnd();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
}