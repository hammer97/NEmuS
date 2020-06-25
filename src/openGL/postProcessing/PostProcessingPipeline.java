package openGL.postProcessing;

import core.ppu.PPU_2C02;
import gui.lwjgui.NEmuSUnified;
import openGL.Quad;
import org.lwjgl.opengl.GL11;
import utils.Dialogs;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11C.glEnable;

/**
 * This class represents a post processing pipeline that can process an input texture
 */
public class PostProcessingPipeline {

    private final List<PostProcessingStep> allSteps;
    private final List<PostProcessingStep> steps;

    //We need to duplicate the Filters when required, this need to be done by the OpenGL Thread
    //So we use a buffer variable to store the list of filters to apply
    private List<PostProcessingStep> requestedSteps;

    private PostProcessingStep default_filter;

    private volatile boolean locked = false;

    /**
     * Create a new pipeline
     *
     * @param quad the quad where we will render the textures
     */
    public PostProcessingPipeline(Quad quad) {
        steps = new ArrayList<>();
        allSteps = new ArrayList<>();
        try {
            allSteps.add(new GaussianHorizontal(quad, PPU_2C02.SCREEN_WIDTH, PPU_2C02.SCREEN_HEIGHT));
            allSteps.add(new GaussianVertical(quad, PPU_2C02.SCREEN_WIDTH, PPU_2C02.SCREEN_HEIGHT));
            allSteps.add(new VerticalFlip(quad, PPU_2C02.SCREEN_WIDTH, PPU_2C02.SCREEN_HEIGHT));
            allSteps.add(new HorizontalFlip(quad, PPU_2C02.SCREEN_WIDTH, PPU_2C02.SCREEN_HEIGHT));
            allSteps.add(new DiagonalFlipTR(quad, PPU_2C02.SCREEN_WIDTH, PPU_2C02.SCREEN_HEIGHT));
            allSteps.add(new DiagonalFlipTL(quad, PPU_2C02.SCREEN_WIDTH, PPU_2C02.SCREEN_HEIGHT));
            allSteps.add(new Fisheye(quad, PPU_2C02.SCREEN_WIDTH, PPU_2C02.SCREEN_HEIGHT));
            allSteps.add(new Toonify(quad, PPU_2C02.SCREEN_WIDTH, PPU_2C02.SCREEN_HEIGHT));

            //We need to apply a vertical flip because the PPU render from the top thus inverting the y component of the initial texture
            default_filter = new VerticalFlip(quad);
        } catch (Exception e) {
            Dialogs.showException("Filter initialization error", "An error has occurred when initializing filters", e);
        }
    }

    /**
     * Will apply the current set of filters to the input texture and render the result to the screen
     *
     * @param texture the texture we want to apply the filters to
     */
    public void applyFilters(int texture) {
        //If the pipeline has been modified, we lock the buffer and recompile the pipeline
        while (locked) Thread.onSpinWait();
        locked = true;
        if (requestedSteps != null) {
            for (PostProcessingStep step : steps)
                step.cleanUp();
            steps.clear();
            for (PostProcessingStep step : requestedSteps) {
                try {
                    steps.add(step.cloneFilter());
                } catch (Exception e) {
                    Dialogs.showException("Filter copy error", "An error has occurred when copying filter", e);
                }
            }
            requestedSteps = null;
        }
        locked = false;

        //We apply each step of the pipeline
        start();
        if (steps.size() > 0) {
            steps.get(0).render(texture, PPU_2C02.SCREEN_WIDTH, PPU_2C02.SCREEN_HEIGHT);
            for (int i = 1; i < steps.size(); i++)
                steps.get(i).render(steps.get(i - 1).getOutputTexture(), PPU_2C02.SCREEN_WIDTH, PPU_2C02.SCREEN_HEIGHT);
            default_filter.render(steps.get(steps.size() - 1).getOutputTexture(), NEmuSUnified.getInstance().getWidth() ,  NEmuSUnified.getInstance().getHeight());

        } else {
            if (NEmuSUnified.getInstance() != null)
                default_filter.render(texture, NEmuSUnified.getInstance().getWidth(), NEmuSUnified.getInstance().getHeight());
        }
        end();
    }

    /**
     * Clean up every filters of the pipeline
     */
    public void cleanUp() {
        default_filter.cleanUp();
        for (PostProcessingStep step : steps)
            step.cleanUp();
    }

    /**
     * Prepare the pipeline for rendering
     */
    private void start() {
        glDisable(GL11.GL_DEPTH_TEST);
    }

    /**
     * Reset the OpenGl state after rendering
     */
    private void end() {
        glEnable(GL11.GL_DEPTH_TEST);
    }

    /**
     * Get all existing filters
     *
     * @return a list of all existing filters
     */
    public List<PostProcessingStep> getAllSteps() {
        return allSteps;
    }

    /**
     * Set the list of filters to be applied
     *
     * @param steps the list of filters to apply
     */
    public void setSteps(List<PostProcessingStep> steps) {
        //We wait until the buffer is available before writing it from another Thread
        while (locked) Thread.onSpinWait();
        requestedSteps = steps;
    }

    public List<String> getSteps() {
        while (locked) Thread.onSpinWait();
        locked = true;
        List<String> l = new ArrayList<>();
        for (PostProcessingStep step : steps)
            l.add(step.toString());
        locked = false;
        return l;
    }
}