package graphics;

import graphics.renderdata.RenderData;
import graphics.scene.Camera;
import graphics.scene.Light;
import graphics.scene.PerspectiveCamera;
import graphics.scene.Scene;
import matrix.Mat4;
import matrix.Ray3;
import matrix.Vec2;
import matrix.Vec3;
import volume.Volumetric;
import world.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public class Renderer {

	private Camera camera;
	private Rectangle viewport;
	private Rectangle window;
	private BufferedImage frameBuffer;

	public Renderer(Rectangle window) {

		camera = new PerspectiveCamera();
		setWindow(window);
		setViewport(getWindow());
	}

	public void render(Scene scene) {

		Mat4 projection = camera.getProjection(viewport.getSize()).multiply(camera.getTransform());
		Mat4 inverseMatrix = projection.inverse();
		Vec3 near, far;

		Vec3[][] frame = new Vec3[viewport.height][viewport.width];
		float maxBrightness = 0;

		int threads = 7;
		ExecutorService executor = Executors.newWorkStealingPool(threads);
		Dimension size = new Dimension(40, 30);
		for (int y = 0; y < viewport.height; y += size.height) {
			for (int x = 0; x < viewport.width; x += size.width) {

				int width = Math.min(viewport.width - x, size.width);
				int height = Math.min(viewport.height - y, size.height);
				Rectangle area = new Rectangle(x, y, width, height);

				executor.execute(() -> {
//					System.out.println(area.x + ", " + area.y);
					renderKernel(scene, inverseMatrix, frame, area);
				});
			}
		}

		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int y = 0; y < viewport.height; y++) {
			for (int x = 0; x < viewport.width; x++) {
				if (!Float.isFinite(frame[y][x].x) || !Float.isFinite(frame[y][x].y) || !Float.isFinite(frame[y][x].z))
					continue;

				maxBrightness = Math.max(maxBrightness, frame[y][x].x);
				maxBrightness = Math.max(maxBrightness, frame[y][x].y);
				maxBrightness = Math.max(maxBrightness, frame[y][x].z);
			}
		}

		System.out.println(maxBrightness);

		for (int y = 0; y < viewport.height; y++) {
			for (int x = 0; x < viewport.width; x++) {
//				System.out.println(frame[y][x]);
				Color color = null;
				if (!Float.isFinite(frame[y][x].x) || !Float.isFinite(frame[y][x].y) || !Float.isFinite(frame[y][x].z)) {
					color = new Color(0, 0, 0, 0);
				} else {
//					Vec3 pixel = frame[y][x];
					Vec3 pixel = frame[y][x].divide(maxBrightness);
					pixel.x = (float) Math.sqrt(pixel.x);
					pixel.y = (float) Math.sqrt(pixel.y);
					pixel.z = (float) Math.sqrt(pixel.z);
//				System.out.println(pixel);
					color = new Color(pixel.x, pixel.y, pixel.z);
				}
				frameBuffer.setRGB(viewport.x + x, window.height - (viewport.y + y) - 1, color.getRGB());
			}
		}
	}

	private void renderKernel(Scene scene, Mat4 inverseMatrix, Vec3[][] frame, Rectangle area) {

		ClosestQuery<Volumetric<RenderData>> query = new ClosestQuery<>();
		Vec3 near, far;

		for (int y = area.y; y < area.y + area.height; y++) {
//			System.out.println(String.format("%.2f%%", 100f * y / viewport.height));
			for (int x = area.x; x < area.x + area.width; x++) {

				float u = (float) (x - viewport.x) / viewport.width * 2 - 1;
				float v = (float) (y - viewport.y) / viewport.height * 2 - 1;
				near = inverseMatrix.multiply(new Vec3(u, v, -1), 1);
				far = inverseMatrix.multiply(new Vec3(u, v, 1), 1);

				query.closest = null;
				scene.raytrace(query, new Ray3(near, far));

				Vec3 color = new Vec3();

				IntersectionData<Volumetric<RenderData>> intersectionData = query.closest;

				if (intersectionData != null) {

					Volumetric<RenderData> volumetric = intersectionData.volumetric;
					RenderData data = volumetric.getData();
					Material material = data.getMaterial();
					if (material == null) {
						material = new Material();
						material.diffuse = uv -> new Vec3(1, 1, 1);
					}
					Vec2 uv = intersectionData.uv;
					Vec2 textureUV = data.normalizeTextureUV(volumetric, uv);

					Vec3 normal = data.calculateNormal(volumetric, uv);
					if (material.normal != null) {
						Vec3 materialNormal = material.normal.get(textureUV);
						materialNormal.set(materialNormal.add(materialNormal));
						materialNormal.set(materialNormal.subtract(new Vec3(1, 1, 1)).normalized());
						normal = data.perturbNormal(volumetric, normal, materialNormal);
					}

					// lighting querys
					for (Light light : scene.lights) {
						AnyQuery<Volumetric<RenderData>> lightQuery = new AnyQuery<>();
						Vec3 photon = light.getLight(); // light color and brightness
						Ray3 lightRay = new Ray3(intersectionData.intersection, light.getSource());

						// skip backfacing lights
						// cuts render time in half basically
						if (normal.dot(lightRay.direction) < 0)
							continue;

						// make sure to exclude the source volumetric so as to avoid self-intersection
						scene.raytrace(lightQuery, lightRay, volumetric);
						if (!lightQuery.isIntersection) {
							// clear path

							// surface area of a sphere
							float falloff = 4 * ((float) Math.PI) * lightRay.length * lightRay.length;
							photon = photon.divide(falloff);
							photon.set(photon.multiply(facingRatio(lightRay.direction.negate(), normal)));

							// specular reflection
							Vec3 incoming = intersectionData.source.direction;

							float glossiness = material.glossiness.get(textureUV);
							float phong = phong(incoming, normal, lightRay.direction, glossiness);

							phong *= material.specular.get(textureUV);

							Vec3 diffuse = material.diffuse.get(textureUV);
//							Vec3 diffuse = new Vec3(0.36f, 0.2f, 0.09f);
							color.set(color.add(diffuse(diffuse, photon).add(photon.multiply(phong))));
						}
					}
				}
				frame[y][x] = color;
			}
		}
	}

	private float phong(Vec3 incomingDirection, Vec3 normal, Vec3 lightDirection, float n) {

		Vec3 hmm = normal.multiply(incomingDirection.dot(normal));
		Vec3 reflection =  incomingDirection.subtract(hmm.add(hmm));
		float specular = Math.max(0, reflection.dot(lightDirection));
		return (float) Math.pow(specular, n);
	}

	float brdf(float incomingAngle, float outgoingAngle) {
		return 1;
	}

	private Vec3 diffuse(Vec3 diffuse, Vec3 light) {
		return diffuse.multiply(light);
	}

	private float facingRatio(Vec3 view, Vec3 normal) {
		float facingRatio = normal.dot(view.negate());
		facingRatio = Math.max(0, facingRatio);
		return facingRatio;
	}

	private Color calculateColor(IntersectionData<Volumetric<RenderData>> closest) {

		if (closest == null)
			return new Color(0, 0, 0, 0);

//		Vec3 triangleUVW = new Vec3(1 - (closest.uv.x + closest.uv.y), closest.uv.x, closest.uv.y);
//		return new Color(triangleUVW.x, triangleUVW.y, triangleUVW.z);

		Vec3 normal = closest.volumetric.getData().calculateNormal(closest.volumetric, closest.uv);
		float facingRatio = normal.dot(closest.source.direction.negate());
		facingRatio = Math.max(0, facingRatio);
		return new Color(facingRatio, facingRatio, facingRatio);
//		return new Color((normal.x+1)/2, (normal.y+1)/2, (normal.z+1)/2);

//		return Color.getHSBColor(0, 0, closest.source.length / 20f);
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

	private static class ClosestQuery<T extends Volumetric> implements RaycastQuery<T> {

		public IntersectionData<T> closest;

		@Override
		public float intersection(IntersectionData<T> intersection) {

			closest = intersection;
			return intersection.depthFraction;
		}
	}

	private static class AnyQuery<T extends Volumetric> implements RaycastQuery<T> {

		public boolean isIntersection = false;

		@Override
		public float intersection(IntersectionData<T> intersection) {
			isIntersection = true;
			return 0;
		}
	}
}
