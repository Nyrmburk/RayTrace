package graphics;

import matrix.Mat4;
import matrix.Ray3;
import matrix.Vec3;
import world.*;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public class Renderer {

	private Camera camera;
	private Rectangle viewport;
	private Rectangle window;
	private BufferedImage frameBuffer;

	{
		camera = new PerspectiveCamera();
		setWindow(new Rectangle(640, 480));
		setViewport(getWindow());
	}

	ClosestQuery<Material> query = new ClosestQuery<>();

	public void render(World<Material> world) {

		Mat4 inverseMatrix = camera.getProjection(viewport.getSize()).multiply(camera.getTransform()).inverse();
		Vec3 near, far;

		for (int y = 0; y < viewport.height; y++) {
			for (int x = 0; x < viewport.width; x++) {

				float u = (float) (x - viewport.x) / viewport.width * 2 - 1;
				float v = (float) (y - viewport.y) / viewport.height * 2 - 1;
				near = inverseMatrix.multiply(new Vec3(u, v, -1), 1);
				far = inverseMatrix.multiply(new Vec3(u, v, 1), 1);

				query.closest = null;
				world.raytrace(query, new Ray3(near, far));
				Color color = calculateColor(query.closest);
				frameBuffer.setRGB(viewport.x + x, window.height - (viewport.y + y) - 1, color.getRGB());
			}
		}
	}

	private Color calculateColor(IntersectionData closest) {

		if (closest == null)
			return new Color(0, 0, 0, 0);

		return Color.getHSBColor(0, 0, 1 - query.closest.depth);
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public BufferedImage getFrameBuffer() {

		return frameBuffer;
	}

	public Rectangle getViewport() {
		return viewport;
	}

	public void setViewport(Rectangle viewport) {
		this.viewport = viewport;
	}

	public Rectangle getWindow() {
		return window;
	}

	public void setWindow(Rectangle window) {
		this.window = window;
		frameBuffer = new BufferedImage(window.width, window.height, BufferedImage.TYPE_INT_ARGB);
	}

	private static class ClosestQuery<E> implements RaycastQuery<E> {

		IntersectionData closest;

		@Override
		public float intersection(Object data, IntersectionData intersection) {

			closest = intersection;
			return intersection.depth;
		}
	}
}
