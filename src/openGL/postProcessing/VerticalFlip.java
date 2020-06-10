package openGL.postProcessing;

import openGL.Quad;

/**
 * This class represents a filter that flip the screen vertically
 */
public class VerticalFlip extends PostProcessingStep {

    /**
     * Create a new Filter from specific shaders
     * filters created with this will be rendered directly to the screen
     *
     * @param quad the Quad where to render
     */
    public VerticalFlip(Quad quad) {
        super(quad, "shaders/v_flip_vertex.glsl", "shaders/filters/no_filter.glsl");
    }

    /**
     * Create a new Filter from specific shaders that will be rendered in an FBO of a specific size
     *
     * @param quad   the Quad where to render
     * @param width  the width of the FBO
     * @param height the height of the FBO
     */
    public VerticalFlip(Quad quad, int width, int height) {
        super(quad, "shaders/v_flip_vertex.glsl", "shaders/filters/no_filter.glsl", width, height);
    }

    /**
     * Create a copy of the current filter with it's own shader and FBO
     *
     * @return a copy of the filter
     */
    @Override
    public PostProcessingStep clone() {
        return new VerticalFlip(quad, fbo.getWidth(), fbo.getHeight());
    }

    @Override
    public String toString() {
        return "Vertical Flip";
    }
}
