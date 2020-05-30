import core.cartridge.Cartridge;
import core.Bus;
import core.cpu.Flags;
import core.ppu.PPU_2C02;
import openGL.Texture;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import utils.NumberUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * This class is the entry point of the Emulator
 */
public class NEmuS {

    private static final int FRAME_DURATION = 1000000000/60;
    private static final String GAME = "smb.nes";
    private static final float DEAD_ZONE_RADIUS = .4f;

    private long game_window;
    private long info_window;

    private int game_width = PPU_2C02.SCREEN_WIDTH*4;
    private int game_height = PPU_2C02.SCREEN_HEIGHT*4;
    private int info_width = 1400;
    private int info_height = 1020;
    private float game_aspect = (float) game_width / game_height;
    private float info_aspect = (float) info_width / info_height;

    private Bus nes;

    private Texture screen_texture;

    // ==================== Debug Variables ==================== //
    private Thread info_thread;

    private boolean emulationRunning = true;
    private boolean redraw_game = false;
    private boolean redraw_info = false;

    private long frameCount = 0;

    private int selectedPalette = 0x00;
    private int ram_page = 0x00;

    private Map<Integer, String> decompiled;

    private Texture patternTable1_texture;
    private Texture patternTable2_texture;
    private Texture nametable1_texture;
    private Texture nametable2_texture;
    private Texture cpu_texture;
    private Texture oam_texture;
    // ========================================================= //


    /**
     * Launch the Emulator and the Debug Window
     */
    private void run() {
        //Initialize the NES and the Game Window
        initEmulator();
        initGameWindow();

        //Launch the Info/Debug Window on a dedicated Thread
        info_thread = new Thread(this::launchInfoWindow);
        info_thread.start();

        //Start the NES
        loopGameWindow();

        //Destroy the Game Window
        glfwFreeCallbacks(game_window);
        glfwDestroyWindow(game_window);

        //If the Debug Window is active, close it
        if (info_thread.isAlive())
            glfwSetWindowShouldClose(info_window, true);

        //Wait for the Debug Thread to terminate
        try {
            info_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Destroy the Debug Window
        glfwFreeCallbacks(info_window);
        glfwDestroyWindow(info_window);

        //Terminate the application
        glfwTerminate();
        glfwSetErrorCallback(null).free();

    }

    /**
     * Create an instance of a NES and load the game
     */
    private void initEmulator() {
        //Create the Bus
        nes = new Bus();
        //Load the game into the NES
        Cartridge cart = new Cartridge(GAME);
        nes.insertCartridge(cart);
        //Decompile the entire addressable range, for debug purposes
        decompiled = nes.getCpu().disassemble(0x0000, 0xFFFF);
        //Reset the CPU to its default state
        nes.getCpu().reset();
    }

    /**
    * Create and initialize the Game Window
    */
    private void initGameWindow() {
        //Initialize GLFW ont the current Thread
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("GLFW Init failed");

        //Set the window's properties
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        //Create the window
        game_window = glfwCreateWindow(game_width, game_height, "NES", NULL, NULL);
        if (game_window == NULL)
            throw new RuntimeException("Failed to create window");
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(game_window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(game_window, (vidmode.width() - pWidth.get(0))/2, (vidmode.height() - pHeight.get(0))/2);
        }

        //Set the window's resize event
        glfwSetWindowSizeCallback(game_window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long windows, int w, int h) {
                game_aspect = (float)w/h;
                game_width = w;
                game_height = h;
            }
        });
        //Show the window
        glfwMakeContextCurrent(game_window);
        glfwSwapInterval(0);
        glfwShowWindow(game_window);

        //Enable OpenGL on the window
        GL.createCapabilities();

        //Activate textures and create the Screen Texture target
        glEnable(GL_TEXTURE_2D);
        screen_texture = new Texture(256, 240, nes.getPpu().getScreenBuffer());
    }

    /**
     * Create and initialize the Info/Debug Window
     */
    private void initInfoWindow() {
        //Initialize GLFW ont the current Thread
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("GLFW Init failed");

        //Set the window's properties
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        //Create the window
        info_window = glfwCreateWindow(info_width, info_height, "NES", NULL, NULL);
        if (info_window == NULL)
            throw new RuntimeException("Failed to create window");
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(info_window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(info_window, (vidmode.width() - pWidth.get(0))/2, (vidmode.height() - pHeight.get(0))/2);
        }

        //Set the window's resize event
        glfwSetWindowSizeCallback(info_window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long windows, int w, int h) {
                info_aspect = (float)w/h;
                info_width = w;
                info_height = h;
            }
        });
        //Show the window
        glfwMakeContextCurrent(info_window);
        glfwSwapInterval(0);
        glfwShowWindow(info_window);

        //Enable OpenGL on the window
        GL.createCapabilities();

        //Activate textures and create a bunch of them
        glEnable(GL_TEXTURE_2D);
        patternTable1_texture = new Texture(128, 128, nes.getPpu().getPatternTable(0, selectedPalette));
        patternTable2_texture = new Texture(128, 128, nes.getPpu().getPatternTable(1, selectedPalette));
        nametable1_texture = new Texture(256, 240, nes.getPpu().getNametable(0));
        nametable2_texture = new Texture(256, 240, nes.getPpu().getNametable(1));
        cpu_texture = new Texture(new BufferedImage(258+258+3, 990-258-30, BufferedImage.TYPE_INT_RGB));
        oam_texture = new Texture(new BufferedImage(305, 990, BufferedImage.TYPE_INT_RGB));

        //Set the input handler of the window
        glfwSetKeyCallback(info_window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_SPACE && action == GLFW_PRESS)
                    emulationRunning = !emulationRunning;
                else if (key == GLFW_KEY_P && action == GLFW_PRESS) {
                    selectedPalette = (selectedPalette + 1) & 0x07;
                    redraw_info = true;
                } else if (key == GLFW_KEY_R && action == GLFW_PRESS) {
                    nes.reset();
                    redraw_info = true;
                    redraw_game = true;
                } else if (key == GLFW_KEY_3 && action == GLFW_PRESS) {
                    ram_page++;
                    redraw_info = true;
                } else if (key == GLFW_KEY_2 && action == GLFW_PRESS) {
                    ram_page--;
                    redraw_info = true;
                } else if (key == GLFW_KEY_4 && action == GLFW_PRESS) {
                    ram_page += 0x10;
                    redraw_info = true;
                } else if (key == GLFW_KEY_1 && action == GLFW_PRESS) {
                    ram_page -= 0x10;
                    redraw_info = true;
                } else if (key == GLFW_KEY_C && action == GLFW_PRESS) {
                    do { nes.debugClock(); } while (!nes.getCpu().complete());
                    do { nes.debugClock(); } while (nes.getCpu().complete());
                    if (nes.getPpu().frameComplete) {
                        frameCount++;
                        nes.getPpu().frameComplete = false;
                    }
                    redraw_info = true;
                    redraw_game = true;
                } else if (key == GLFW_KEY_V && action == GLFW_PRESS) {
                    for (int i = 0; i < 10; i++) {
                        do { nes.debugClock(); } while (!nes.getCpu().complete());
                        do { nes.debugClock(); } while (nes.getCpu().complete());
                        if (nes.getPpu().frameComplete) {
                            frameCount++;
                            nes.getPpu().frameComplete = false;
                        }
                    }
                    redraw_info = true;
                    redraw_game = true;
                } else if (key == GLFW_KEY_B && action == GLFW_PRESS) {
                    for (int i = 0; i < 50; i++) {
                        do { nes.debugClock(); } while (!nes.getCpu().complete());
                        do { nes.debugClock(); } while (nes.getCpu().complete());
                        if (nes.getPpu().frameComplete) {
                            frameCount++;
                            nes.getPpu().frameComplete = false;
                        }
                    }
                    redraw_info = true;
                    redraw_game = true;
                } else if (key == GLFW_KEY_F && action == GLFW_PRESS) {
                    do { nes.debugClock(); } while (!nes.getPpu().frameComplete);
                    do { nes.debugClock(); } while (nes.getCpu().complete());
                    nes.getPpu().frameComplete = false;
                    frameCount++;
                    redraw_info = true;
                    redraw_game = true;
                }
            }
        });
    }

    /**
     * Run the emulator
     * this is essentially where the emulation occur
     */
    private void loopGameWindow() {
        long next_frame = 0, last_frame = 0;
        while (!glfwWindowShouldClose(game_window)) {
            //Only redraw if the emulation is running or a debug step has been made
            if (emulationRunning || redraw_game) {
                //Set the current OpenGL context
                glfwMakeContextCurrent(game_window);
                glClearColor(.6f, .6f, .6f, 0f);
                GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                //We update the controller registers
                InputHandling();
                //If it's time to draw the next frame
                if (System.nanoTime() >= next_frame) {
                    //Set when the next frame should occur
                    next_frame = System.nanoTime() + FRAME_DURATION;
                    if (emulationRunning) {
                        //We compute an entire frame in one go and wait for the next one
                        //this isn't hardware accurate, but is close enough to have most game run properly
                        do { nes.clock(); } while (!nes.getPpu().frameComplete);
                        nes.getPpu().frameComplete = false;
                        frameCount++;
                    }
                    //Keep track of the FPS number
                    glfwSetWindowTitle(game_window, GAME + " - " + 1000000000 / (System.nanoTime() - last_frame) + " fps");
                    last_frame = System.nanoTime();
                }
                //We load the screen pixels into VRAM and display them
                screen_texture.load(nes.getPpu().getScreenBuffer());
                renderGameScreen();
                glfwSwapBuffers(game_window);
                //If it was a debug step, clear the flag
                if (!emulationRunning)
                    redraw_game = false;
            }
            //Get input events
            glfwPollEvents();
        }

        screen_texture.delete();
    }

    /**
     * Initialize and run the Debug Window
     * Usually launched on a separated Thread
     */
    private void launchInfoWindow() {
        initInfoWindow();
        long next_frame = 0, last_frame = 0;
        while (!glfwWindowShouldClose(info_window)) {
            //Only redraw if the emulation is running or a debug step has been made
            if (emulationRunning || redraw_info) {
                //Set the current OpenGL context
                glfwMakeContextCurrent(info_window);
                glClearColor(.6f, .6f, .6f, 0f);
                //Wrap the ram page to avoid out of bounds
                if (ram_page < 0x00) ram_page += 0x100;
                if (ram_page > 0xFF) ram_page -= 0x100;
                //If it's time to draw the next frame
                if (System.nanoTime() >= next_frame) {
                    GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                    //Set when the next frame should occur
                    next_frame = System.nanoTime() + FRAME_DURATION;
                    //Compute and update the debug textures (CPU, OAM, PatternTables and Nametables)
                    computeTextures();
                    //Keep track of the FPS number
                    glfwSetWindowTitle(info_window, "Info Window : " + 1000000000 / (System.nanoTime() - last_frame) + " fps");
                    last_frame = System.nanoTime();
                    //We actually draw the frame
                    renderInfoScreen();
                }
                glfwSwapBuffers(info_window);
                //If it was a debug step, clear the flag
                if (!emulationRunning)
                    redraw_info = false;
            }
            //Get input events
            glfwPollEvents();
        }

        //Delete the texture from memory
        patternTable2_texture.delete();
        patternTable1_texture.delete();
        cpu_texture.delete();
        oam_texture.delete();
    }

    /**
     * Get the current user inputs (Keyboard and Gamepad 1 and 2) and write it to NES
     */
    private void InputHandling() {
        //If Gamepad 1 is connected
        if (glfwJoystickPresent(GLFW_JOYSTICK_1)) {
            ByteBuffer buttons = glfwGetJoystickButtons(GLFW_JOYSTICK_1);
            FloatBuffer axes = glfwGetJoystickAxes(GLFW_JOYSTICK_1);
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_DPAD_UP - 1 && buttons.get(GLFW_GAMEPAD_BUTTON_DPAD_UP - 1) == GLFW_PRESS) || (axes != null && axes.capacity() > GLFW_GAMEPAD_AXIS_LEFT_Y && axes.get(GLFW_GAMEPAD_AXIS_LEFT_Y) < -DEAD_ZONE_RADIUS) || glfwGetKey(game_window, GLFW_KEY_UP) == GLFW_PRESS) nes.controller[0] |= 0x08;else nes.controller[0] &= ~0x08;       // Up
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_DPAD_DOWN - 1 && buttons.get(GLFW_GAMEPAD_BUTTON_DPAD_DOWN - 1) == GLFW_PRESS) || (axes != null && axes.capacity() > GLFW_GAMEPAD_AXIS_LEFT_Y && axes.get(GLFW_GAMEPAD_AXIS_LEFT_Y) > DEAD_ZONE_RADIUS) || glfwGetKey(game_window, GLFW_KEY_DOWN) == GLFW_PRESS) nes.controller[0] |= 0x04;else nes.controller[0] &= ~0x04;       // Down
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_DPAD_LEFT - 1 && buttons.get(GLFW_GAMEPAD_BUTTON_DPAD_LEFT - 1) == GLFW_PRESS) || (axes != null && axes.capacity() > GLFW_GAMEPAD_AXIS_LEFT_X && axes.get(GLFW_GAMEPAD_AXIS_LEFT_X) < -DEAD_ZONE_RADIUS) || glfwGetKey(game_window, GLFW_KEY_LEFT) == GLFW_PRESS) nes.controller[0] |= 0x02;else nes.controller[0] &= ~0x02;       // Left
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_DPAD_RIGHT - 1 && buttons.get(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT - 1) == GLFW_PRESS) || (axes != null && axes.capacity() > GLFW_GAMEPAD_AXIS_LEFT_X && axes.get(GLFW_GAMEPAD_AXIS_LEFT_X) > DEAD_ZONE_RADIUS) || glfwGetKey(game_window, GLFW_KEY_RIGHT) == GLFW_PRESS) nes.controller[0] |= 0x01;else nes.controller[0] &= ~0x01;       // Right
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_X && buttons.get(GLFW_GAMEPAD_BUTTON_X) == GLFW_PRESS) || glfwGetKey(game_window, GLFW_KEY_I) == GLFW_PRESS) nes.controller[0] |= 0x80;else nes.controller[0] &= ~0x80;       // A
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_A && buttons.get(GLFW_GAMEPAD_BUTTON_A) == GLFW_PRESS) || glfwGetKey(game_window, GLFW_KEY_U) == GLFW_PRESS) nes.controller[0] |= 0x40;else nes.controller[0] &= ~0x40;       // B
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_BACK && buttons.get(GLFW_GAMEPAD_BUTTON_BACK) == GLFW_PRESS) || glfwGetKey(game_window, GLFW_KEY_J) == GLFW_PRESS) nes.controller[0] |= 0x20;else nes.controller[0] &= ~0x20;       // Select
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_START && buttons.get(GLFW_GAMEPAD_BUTTON_START) == GLFW_PRESS) || glfwGetKey(game_window, GLFW_KEY_K) == GLFW_PRESS) nes.controller[0] |= 0x10;else nes.controller[0] &= ~0x10;       // Start
        //if not we ignore the Gamepad tests
        } else {
            if (glfwGetKey(game_window, GLFW_KEY_UP) == GLFW_PRESS) nes.controller[0] |= 0x08; else nes.controller[0] &= ~0x08;       // Up
            if (glfwGetKey(game_window, GLFW_KEY_DOWN) == GLFW_PRESS) nes.controller[0] |= 0x04; else nes.controller[0] &= ~0x04;       // Down
            if (glfwGetKey(game_window, GLFW_KEY_LEFT) == GLFW_PRESS) nes.controller[0] |= 0x02; else nes.controller[0] &= ~0x02;       // Left
            if (glfwGetKey(game_window, GLFW_KEY_RIGHT) == GLFW_PRESS) nes.controller[0] |= 0x01; else nes.controller[0] &= ~0x01;       // Right
            if (glfwGetKey(game_window, GLFW_KEY_I) == GLFW_PRESS) nes.controller[0] |= 0x80; else nes.controller[0] &= ~0x80;       // A
            if (glfwGetKey(game_window, GLFW_KEY_U) == GLFW_PRESS) nes.controller[0] |= 0x40; else nes.controller[0] &= ~0x40;       // B
            if (glfwGetKey(game_window, GLFW_KEY_J) == GLFW_PRESS) nes.controller[0] |= 0x20; else nes.controller[0] &= ~0x20;       // Select
            if (glfwGetKey(game_window, GLFW_KEY_K) == GLFW_PRESS) nes.controller[0] |= 0x10; else nes.controller[0] &= ~0x10;       // Start
        }
        //If Gamepad 2 is connected
        if (glfwJoystickPresent(GLFW_JOYSTICK_2)) {
            ByteBuffer buttons = glfwGetJoystickButtons(GLFW_JOYSTICK_2);
            FloatBuffer axes = glfwGetJoystickAxes(GLFW_JOYSTICK_2);
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_DPAD_UP - 1 && buttons.get(GLFW_GAMEPAD_BUTTON_DPAD_UP - 1) == GLFW_PRESS) || (axes != null && axes.capacity() > GLFW_GAMEPAD_AXIS_LEFT_Y && axes.get(GLFW_GAMEPAD_AXIS_LEFT_Y) < -DEAD_ZONE_RADIUS) || glfwGetKey(game_window, GLFW_KEY_W) == GLFW_PRESS) nes.controller[1] |= 0x08; else nes.controller[1] &= ~0x08;       // Up
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_DPAD_DOWN - 1 && buttons.get(GLFW_GAMEPAD_BUTTON_DPAD_DOWN - 1) == GLFW_PRESS) || (axes != null && axes.capacity() > GLFW_GAMEPAD_AXIS_LEFT_Y && axes.get(GLFW_GAMEPAD_AXIS_LEFT_Y) > DEAD_ZONE_RADIUS) || glfwGetKey(game_window, GLFW_KEY_S) == GLFW_PRESS) nes.controller[1] |= 0x04; else nes.controller[1] &= ~0x04;       // Down
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_DPAD_LEFT - 1 && buttons.get(GLFW_GAMEPAD_BUTTON_DPAD_LEFT - 1) == GLFW_PRESS) || (axes != null && axes.capacity() > GLFW_GAMEPAD_AXIS_LEFT_X && axes.get(GLFW_GAMEPAD_AXIS_LEFT_X) < -DEAD_ZONE_RADIUS) || glfwGetKey(game_window, GLFW_KEY_A) == GLFW_PRESS) nes.controller[1] |= 0x02; else nes.controller[1] &= ~0x02;       // Left
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_DPAD_RIGHT - 1 && buttons.get(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT - 1) == GLFW_PRESS) || (axes != null && axes.capacity() > GLFW_GAMEPAD_AXIS_LEFT_X && axes.get(GLFW_GAMEPAD_AXIS_LEFT_X) > DEAD_ZONE_RADIUS) || glfwGetKey(game_window, GLFW_KEY_D) == GLFW_PRESS) nes.controller[1] |= 0x01; else nes.controller[1] &= ~0x01;       // Right
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_X && buttons.get(GLFW_GAMEPAD_BUTTON_X) == GLFW_PRESS) || glfwGetKey(game_window, GLFW_KEY_Y) == GLFW_PRESS) nes.controller[1] |= 0x80; else nes.controller[1] &= ~0x80;       // A
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_A && buttons.get(GLFW_GAMEPAD_BUTTON_A) == GLFW_PRESS) || glfwGetKey(game_window, GLFW_KEY_R) == GLFW_PRESS) nes.controller[1] |= 0x40; else nes.controller[1] &= ~0x40;       // B
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_BACK && buttons.get(GLFW_GAMEPAD_BUTTON_BACK) == GLFW_PRESS) || glfwGetKey(game_window, GLFW_KEY_F) == GLFW_PRESS) nes.controller[1] |= 0x20; else nes.controller[1] &= ~0x20;       // Select
            if ((buttons != null && buttons.capacity() > GLFW_GAMEPAD_BUTTON_START && buttons.get(GLFW_GAMEPAD_BUTTON_START) == GLFW_PRESS) || glfwGetKey(game_window, GLFW_KEY_H) == GLFW_PRESS) nes.controller[1] |= 0x10; else nes.controller[1] &= ~0x10;       // Start
        //if not we ignore the Gamepad tests
        } else {
            if (glfwGetKey(game_window, GLFW_KEY_W) == GLFW_PRESS) nes.controller[1] |= 0x08; else nes.controller[1] &= ~0x08;       // Up
            if (glfwGetKey(game_window, GLFW_KEY_S) == GLFW_PRESS) nes.controller[1] |= 0x04; else nes.controller[1] &= ~0x04;       // Down
            if (glfwGetKey(game_window, GLFW_KEY_A) == GLFW_PRESS) nes.controller[1] |= 0x02; else nes.controller[1] &= ~0x02;       // Left
            if (glfwGetKey(game_window, GLFW_KEY_D) == GLFW_PRESS) nes.controller[1] |= 0x01; else nes.controller[1] &= ~0x01;       // Right
            if (glfwGetKey(game_window, GLFW_KEY_Y) == GLFW_PRESS) nes.controller[1] |= 0x80; else nes.controller[1] &= ~0x80;       // A
            if (glfwGetKey(game_window, GLFW_KEY_T) == GLFW_PRESS) nes.controller[1] |= 0x40; else nes.controller[1] &= ~0x40;       // B
            if (glfwGetKey(game_window, GLFW_KEY_G) == GLFW_PRESS) nes.controller[1] |= 0x20; else nes.controller[1] &= ~0x20;       // Select
            if (glfwGetKey(game_window, GLFW_KEY_H) == GLFW_PRESS) nes.controller[1] |= 0x10; else nes.controller[1] &= ~0x10;       // Start
        }
    }

    /**
     * Compute and load into VRAM the textures of the Debug Window
     * Must be called before renderInfoScreen() to insure accurate data
     */
    private void computeTextures() {
        // ================================= PPU Memory Visualization =================================
        patternTable1_texture.load(nes.getPpu().getPatternTable(0, selectedPalette));
        patternTable2_texture.load(nes.getPpu().getPatternTable(1, selectedPalette));
        nametable1_texture.load(nes.getPpu().getNametable(0));
        nametable2_texture.load(nes.getPpu().getNametable(1));

        // ================================= Status =================================
        Graphics g = cpu_texture.getImg().getGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0,0, cpu_texture.getImg().getWidth(), cpu_texture.getImg().getHeight());
        g.setFont(new Font("monospaced", Font.BOLD, 35));
        g.setColor(Color.GREEN);
        g.drawString("CPU - RAM", 150, 40);
        g.setFont(new Font("monospaced", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("STATUS:", 10 , 70);
        if (nes.getCpu().threadSafeGetState(Flags.N)) g.setColor(Color.GREEN); else g.setColor(Color.RED);
        g.drawString("N", 80, 70);
        if (nes.getCpu().threadSafeGetState(Flags.V)) g.setColor(Color.GREEN); else g.setColor(Color.RED);
        g.drawString("V", 96, 70);
        if (nes.getCpu().threadSafeGetState(Flags.U)) g.setColor(Color.GREEN); else g.setColor(Color.RED);
        g.drawString("-", 112, 70);
        if (nes.getCpu().threadSafeGetState(Flags.B)) g.setColor(Color.GREEN); else g.setColor(Color.RED);
        g.drawString("B", 128, 70);
        if (nes.getCpu().threadSafeGetState(Flags.D)) g.setColor(Color.GREEN); else g.setColor(Color.RED);
        g.drawString("D", 144, 70);
        if (nes.getCpu().threadSafeGetState(Flags.I)) g.setColor(Color.GREEN); else g.setColor(Color.RED);
        g.drawString("I", 160, 70);
        if (nes.getCpu().threadSafeGetState(Flags.Z)) g.setColor(Color.GREEN); else g.setColor(Color.RED);
        g.drawString("Z", 176, 70);
        if (nes.getCpu().threadSafeGetState(Flags.C)) g.setColor(Color.GREEN); else g.setColor(Color.RED);
        g.drawString("C", 194, 70);
        g.setColor(Color.WHITE);
        g.drawString("Program C  : $" + String.format("%02X", nes.getCpu().threadSafeGetPc()), 10 , 125 + 60);
        g.drawString("A Register : $" + String.format("%02X", nes.getCpu().threadSafeGetA()) + "[" + nes.getCpu().threadSafeGetA() + "]", 10 , 140 + 60);
        g.drawString("X Register : $" + String.format("%02X", nes.getCpu().threadSafeGetX()) + "[" + nes.getCpu().threadSafeGetX() + "]", 10 , 155 + 60);
        g.drawString("Y Register : $" + String.format("%02X", nes.getCpu().threadSafeGetY()) + "[" + nes.getCpu().threadSafeGetY() + "]", 10 , 170 + 60);
        g.drawString("Stack Ptr  : $" + String.format("%04X", nes.getCpu().threadSafeGetStkp()), 10 , 185 + 60);
        g.drawString("Ticks  : " + nes.getCpu().threadSafeGetCpuClock(), 10, 340);
        g.drawString("Frames : " + frameCount, 10 , 355);

        // ================================= RAM =================================
        int nRamX = 5, nRamY = 440;
        int nAddr = ram_page << 8;
        for (int row = 0; row < 16; row++)
        {
            String sOffset = String.format("$%04X:", nAddr);
            for (int col = 0; col < 16; col++)
            {
                sOffset += " " +  String.format("%02X", nes.threadSafeCpuRead(nAddr));
                nAddr += 1;
            }
            g.drawString(sOffset, nRamX, nRamY);
            nRamY += 17;
        }

        // ================================= Code =================================
        String currentLine = decompiled.get(nes.getCpu().threadSafeGetPc());
        if (currentLine != null) {
            Queue<String> before = new LinkedList<>();
            Queue<String> after = new LinkedList<>();
            boolean currentLineFound = false;
            for (Map.Entry<Integer, String> line : decompiled.entrySet()) {
                if (!currentLineFound) {
                    if (line.getKey() == nes.getCpu().threadSafeGetPc())
                        currentLineFound = true;
                    else
                        before.offer(line.getValue());
                    if (before.size() > 22 / 2)
                        before.poll();
                } else {
                    after.offer(line.getValue());
                    if (after.size() > 22 / 2)
                        break;
                }
            }
            int lineY = 70;
            g.setColor(Color.WHITE);
            for (String line : before) {
                g.drawString(line, 230, lineY);
                lineY += 15;
            }
            g.setColor(Color.CYAN);
            g.drawString(currentLine, 230, lineY);
            lineY += 15;
            g.setColor(Color.WHITE);
            for (String line : after) {
                g.drawString(line, 230, lineY);
                lineY += 15;
            }
            cpu_texture.update();
        }

        // ================================= Object Attribute Memory =================================
        Graphics g2 = oam_texture.getImg().getGraphics();
        g2.setColor(Color.BLUE);
        g2.fillRect(0,0, oam_texture.getImg().getWidth(), oam_texture.getImg().getHeight());
        g2.setFont(new Font("monospaced", Font.BOLD, 35));
        g2.setColor(Color.GREEN);
        g2.drawString("OAM Memory", 30, 40);
        g2.setFont(new Font("monospaced", Font.BOLD, 15));
        g2.setColor(Color.WHITE);
        synchronized (nes.getPpu()) {
            for (int i = 0; i < 64; i++) {
                String s = String.format("%02X:", i) + " (" + nes.getPpu().getOams()[i].getX() + ", " + nes.getPpu().getOams()[i].getY() + ") ID: " + String.format("%02X", nes.getPpu().getOams()[i].getId()) + " AT: " + String.format("%02X", nes.getPpu().getOams()[i].getAttribute());
                g2.drawString(s, 25, (int) (70 + 14.5 * i));
            }
        }
        oam_texture.update();
    }

    /**
     * Render the Game Window
     * the Quad is centered and scale to fit the window without stretching
     */
    private void renderGameScreen() {
        glViewport(0, 0, game_width, game_height);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-game_aspect, game_aspect, -1, 1, -1, 1);
        screen_texture.bind();

        float view_aspect = (float)PPU_2C02.SCREEN_WIDTH/ PPU_2C02.SCREEN_HEIGHT, quad_end_x = game_aspect, quad_end_y = -1;
        if (game_height / view_aspect > game_width)
            quad_end_y = NumberUtils.map(game_width * view_aspect, 0, game_height, 1, -1);
        else
            quad_end_x = NumberUtils.map(game_height * view_aspect, 0, game_width, -game_aspect, game_aspect);
        float quad_width = (2*game_aspect - (quad_end_x + game_aspect))/2;
        float quad_height = (2 - (1 - quad_end_y))/2;
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(-game_aspect + quad_width, 1 - quad_height);
        glTexCoord2f(1, 0);
        glVertex2f(quad_end_x + quad_width, 1 - quad_height);
        glTexCoord2f(1, 1);
        glVertex2f(quad_end_x + quad_width, quad_end_y - quad_height);
        glTexCoord2f(0, 1);
        glVertex2f(-game_aspect + quad_width, quad_end_y - quad_height);
        glEnd();
    }

    /**
     * Draw the elements of the Debug Window
     * CPU State, Object Attribute Memory, Pattern Tables and Nametables
     */
    private void renderInfoScreen() {
        glViewport(0, 0, info_width, info_height);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-info_aspect, info_aspect, -1, 1, -1, 1);

        // ============================================= CPU / Code / RAM ============================================= //
        cpu_texture.bind();
        float x_end_cpu = NumberUtils.map(cpu_texture.getWidth(), 0, info_width, -info_aspect, info_aspect);
        float y_end_cpu = NumberUtils.map(cpu_texture.getHeight(), 0, info_height, 1, -1);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(-info_aspect, 1);
        glTexCoord2f(1, 0);
        glVertex2f(x_end_cpu, 1);
        glTexCoord2f(1, 1);
        glVertex2f(x_end_cpu, y_end_cpu);
        glTexCoord2f(0, 1);
        glVertex2f(-info_aspect, y_end_cpu);
        glEnd();

        // ============================================= Object Attribute Memory ============================================= //
        oam_texture.bind();
        float x_end_oam = NumberUtils.map(oam_texture.getWidth() + cpu_texture.getWidth(), 0, info_width, -info_aspect, info_aspect);
        float y_end_oam = NumberUtils.map(oam_texture.getHeight(), 0, info_height, 1, -1);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(x_end_cpu, 1);
        glTexCoord2f(1, 0);
        glVertex2f(x_end_oam, 1);
        glTexCoord2f(1, 1);
        glVertex2f(x_end_oam, y_end_oam);
        glTexCoord2f(0, 1);
        glVertex2f(x_end_cpu, y_end_oam);
        glEnd();

        // ============================================= Palettes ============================================= //
        glBindTexture(GL_TEXTURE_2D, 0);
        int size = 14;
        glColor3f(1, 0, 0);
        float y_offset_from_cpu = - Math.abs(1 - y_end_cpu) - .025f;

        float x_start_select_ring = NumberUtils.map(selectedPalette * (size*4 + 9), 0, info_width, -info_aspect, info_aspect);
        float x_end_select_ring = NumberUtils.map(8 + selectedPalette * (size*4 + 9) + 4*size, 0, info_width, -info_aspect, info_aspect);
        float y_start_select_ring = NumberUtils.map(size + 5, 0, info_height, 1, -1) + y_offset_from_cpu;
        glColor3f(1, 0, 0);
        glBegin(GL_QUADS);
        glVertex2f(x_start_select_ring, y_start_select_ring);
        glVertex2f(x_end_select_ring, y_start_select_ring);
        glVertex2f(x_end_select_ring, 1 + y_offset_from_cpu + 0.007f);
        glVertex2f(x_start_select_ring, 1 + y_offset_from_cpu + 0.007f);
        glEnd();
        for (int p = 0; p < 8; p++) {
            for (int s = 0; s < 4; s++) {
                Color color = nes.getPpu().threadSafeGetColorFromPalette(p, s);
                glColor3f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
                float x_start_palette = NumberUtils.map(4 + p * (size*4 + 9) + s*size, 0, info_width, -info_aspect, info_aspect);
                float x_end_palette = NumberUtils.map(4 + p * (size*4 + 9) + s*size + size, 0, info_width, -info_aspect, info_aspect);
                float y = NumberUtils.map(size, 0, info_height, 1, -1) + y_offset_from_cpu;
                glBegin(GL_QUADS);
                glVertex2f(x_start_palette, y);
                glVertex2f(x_end_palette, y);
                glVertex2f(x_end_palette, 1 + y_offset_from_cpu);
                glVertex2f(x_start_palette, 1 + y_offset_from_cpu);
                glEnd();
            }
        }

        // ============================================= Pattern Table 1 ============================================= //
        y_offset_from_cpu -= .055f;
        glColor3f(1,1,1);
        patternTable1_texture.bind();
        float x_end_pattern1 = NumberUtils.map(2 * patternTable1_texture.getWidth(), 0, info_width, -info_aspect, info_aspect);
        float y_end_pattern = NumberUtils.map(2 * patternTable1_texture.getHeight(), 0, info_height, 1, -1) + y_offset_from_cpu;
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(-info_aspect, 1 + y_offset_from_cpu);
        glTexCoord2f(1, 0);
        glVertex2f(x_end_pattern1, 1 + y_offset_from_cpu);
        glTexCoord2f(1, 1);
        glVertex2f(x_end_pattern1, y_end_pattern);
        glTexCoord2f(0, 1);
        glVertex2f(-info_aspect, y_end_pattern);
        glEnd();

        // ============================================= Pattern Table 2 ============================================= //
        patternTable2_texture.bind();
        float x_offset_pattern2 = NumberUtils.map(8, 0, info_width, 0, 2*info_aspect);
        float x_end_pattern2 = NumberUtils.map(4 * patternTable2_texture.getWidth() + 8, 0, info_width, -info_aspect, info_aspect);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(x_end_pattern1 + x_offset_pattern2, 1 + y_offset_from_cpu);
        glTexCoord2f(1, 0);
        glVertex2f(x_end_pattern2, 1 + y_offset_from_cpu);
        glTexCoord2f(1, 1);
        glVertex2f(x_end_pattern2, y_end_pattern);
        glTexCoord2f(0, 1);
        glVertex2f(x_end_pattern1 + x_offset_pattern2, y_end_pattern);
        glEnd();

        // ============================================= Nametable 1 ============================================= //
        nametable1_texture.bind();
        float x_start_nametable = NumberUtils.map(cpu_texture.getWidth() + oam_texture.getWidth() + 20, 0, info_width, -info_aspect, info_aspect);
        float x_end_nametable = NumberUtils.map(2 * nametable1_texture.getWidth() + cpu_texture.getWidth() + oam_texture.getWidth() + 20, 0, info_width, -info_aspect, info_aspect);
        float y_start_nametable1 = NumberUtils.map(10, 0, info_height, 1, -1);
        float y_end_nametable1 = NumberUtils.map(10 + 2 * nametable1_texture.getHeight(), 0, info_height, 1, -1);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(x_start_nametable, y_start_nametable1);
        glTexCoord2f(1, 0);
        glVertex2f(x_end_nametable, y_start_nametable1);
        glTexCoord2f(1, 1);
        glVertex2f(x_end_nametable, y_end_nametable1);
        glTexCoord2f(0, 1);
        glVertex2f(x_start_nametable, y_end_nametable1);
        glEnd();

        // ============================================= Nametable 2 ============================================= //
        nametable2_texture.bind();
        float y_start_nametable2 = NumberUtils.map(20 + 2 * nametable1_texture.getHeight(), 0, info_height, 1, -1);
        float y_end_nametable2 = NumberUtils.map(20 + 2 * nametable1_texture.getHeight() + 20 + 2 * nametable1_texture.getHeight(), 0, info_height, 1, -1);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(x_start_nametable, y_start_nametable2);
        glTexCoord2f(1, 0);
        glVertex2f(x_end_nametable, y_start_nametable2);
        glTexCoord2f(1, 1);
        glVertex2f(x_end_nametable, y_end_nametable2);
        glTexCoord2f(0, 1);
        glVertex2f(x_start_nametable, y_end_nametable2);
        glEnd();
    }



    public static void main(String[] args) {
        new NEmuS().run();
    }
}