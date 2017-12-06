package graphics;

import graphics.renderdata.Renderable;
import graphics.scene.Camera;
import graphics.scene.PerspectiveCamera;
import matrix.Mat4;
import matrix.Ray3;
import matrix.Vec3;
import world.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public class Renderer {

	private Camera camera;
	private Rectangle viewport;
	private Rectangle window;
	private BufferedImage frameBuffer;
	private Mat4 projection;

	private ClosestQuery query = new ClosestQuery();

	public Renderer(Rectangle window) {

		camera = new PerspectiveCamera();
		setWindow(window);
		setViewport(getWindow());
	}

	public void render(World<Renderable> world) {

		projection = camera.getProjection(viewport.getSize()).multiply(camera.getTransform());
		Mat4 inverseMatrix = projection.inverse();
		Vec3 near, far;

//		Random random = new Random();
//		for (int i = 0; i < 1_000_000; i++) {
//
//			float x = random.nextFloat();
//			float u = x + x - 1;
//			float y = random.nextFloat();
//			float v = y + y - 1;
//
//			near = inverseMatrix.multiply(new Vec3(u, v, -1), 1);
//			far = inverseMatrix.multiply(new Vec3(u, v, 1), 1);
//
//			query.closest = null;
//			world.raytrace(query, new Ray3(near, far));
//			Color color = calculateColor(query.closest);
//			x *= viewport.width;
//			y *= viewport.height;
//			frameBuffer.setRGB((int) x, window.height - 1 - (int) y, color.getRGB());
//		}

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

	private Color calculateColor(IntersectionData<Renderable> closest) {

		if (closest == null)
			return new Color(0, 0, 0, 0);

//		Vec3 triangleUVW = new Vec3(closest.uv.x, closest.uv.y, 1 - (closest.uv.x + closest.uv.y));
//		return new Color(triangleUVW.x, triangleUVW.y, triangleUVW.z);

		Vec3 normal = closest.volumetric.calculateNormal(closest.uv);
		float facingRatio = normal.dot(closest.source.direction.negate());
		facingRatio = Math.max(0, facingRatio);
		return new Color(facingRatio, facingRatio, facingRatio);
//		return new Color((normal.x+1)/2, (normal.y+1)/2, (normal.z+1)/2);

//		return Color.getHSBColor(0, 0, (2 - closest.normal.position.length()) / 2f);
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

	private static class ClosestQuery implements RaycastQuery {

		public IntersectionData closest;

		@Override
		public float intersection(IntersectionData intersection) {

			closest = intersection;
			return intersection.depthFraction;
		}
	}
}
