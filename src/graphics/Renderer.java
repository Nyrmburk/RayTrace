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

				frame[y][x] = raycast(scene, new Ray3(near, far), 1, 1);
			}
		}
	}

	private Vec3 raycast(Scene scene, Ray3 ray, int samples, int depth) {

		ClosestQuery<Volumetric<RenderData>> query = new ClosestQuery<>();

		scene.raytrace(query, ray);

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

			float incomingAngle = normal.dot(intersectionData.source.direction.negate());

			// lighting querys
			for (Light light : scene.lights) {
				AnyQuery<Volumetric<RenderData>> lightQuery = new AnyQuery<>();
				Vec3 photon = light.getLight(); // light color and brightness
				Ray3 lightRay = new Ray3(intersectionData.intersection, light.getSource());

				float outgoingAngle = normal.dot(lightRay.direction);

				// skip backfacing lights
				// cuts render time in half basically
				if (outgoingAngle < 0)
					continue;

				// make sure to exclude the source volumetric so as to avoid self-intersection
				scene.raytrace(lightQuery, lightRay, volumetric);
				if (!lightQuery.isIntersection) {
					// clear path

					// surface area of a sphere
					float falloff = 4 * ((float) Math.PI) * lightRay.length * lightRay.length;
					photon = photon.divide(falloff);
					photon.set(photon.multiply(Math.max(0, outgoingAngle)));

					// specular reflection
					Vec3 incoming = intersectionData.source.direction.negate();

					Vec3 halfAngle = incoming.add(lightRay.direction).normalized();

					float roughness = material.roughness.get(textureUV);

					float brdf = brdf(incomingAngle, outgoingAngle, incoming, normal, halfAngle, roughness);
					if (!Float.isFinite(brdf))
						brdf = 0;

					float specular = brdf * material.specular.get(textureUV);

					Vec3 diffuse = material.diffuse.get(textureUV);
					color.set(color.add(diffuse(diffuse, photon).add(photon.multiply(specular))));
				}
			}
		}

		return color;
	}

	float brdf(float incomingAngle, float outgoingAngle,
			   Vec3 source, Vec3 normal, Vec3 halfAngle, float roughness) {
		return cookTorrance(incomingAngle, outgoingAngle, source, normal, halfAngle, roughness);
	}

	float cookTorrance(float incomingAngle, float outgoingAngle,
					   Vec3 source, Vec3 normal, Vec3 halfAngle, float roughness) {
		float distribution = beckmann(normal, halfAngle, roughness);
		float fresnel = fresnel(incomingAngle, roughness);
		float g = attenuation(incomingAngle, outgoingAngle, source, normal, halfAngle);

		return (distribution * fresnel * g) / (4 * incomingAngle * outgoingAngle);
	}

	float beckmann(Vec3 normal, Vec3 halfAngle, float roughness) {
		float alpha = (float) Math.acos(normal.dot(halfAngle));

		float tanAlpha2 = (float) Math.tan(alpha);
		tanAlpha2 = tanAlpha2 * tanAlpha2;
		float roughness2 = roughness * roughness;

		float cosAlpha4 = (float) Math.cos(alpha); // cos(alpha)
		cosAlpha4 *= cosAlpha4; // cos^2(alpha)
		cosAlpha4 *= cosAlpha4; // cos^4(alpha)

		return ((float) Math.exp(-tanAlpha2 / roughness2)) / (((float) Math.PI) * roughness2 * cosAlpha4);
	}

	float fresnel(float incomingAngle, float roughness) {
		float oneMinusAngle = (1 - incomingAngle);
		return roughness + (1 - roughness) *
				(oneMinusAngle * oneMinusAngle * oneMinusAngle * oneMinusAngle * oneMinusAngle);
	}

	float attenuation(float incomingAngle, float outgoingAngle, Vec3 source, Vec3 normal, Vec3 halfAngle) {

		float attenuation = 2 * halfAngle.dot(normal) * Math.min(incomingAngle, outgoingAngle) / source.dot(halfAngle);

		return Math.min(1, attenuation);
	}

	private Vec3 diffuse(Vec3 diffuse, Vec3 light) {
		return diffuse.multiply(light);
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
