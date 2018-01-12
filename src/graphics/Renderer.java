package graphics;

import graphics.renderdata.RenderData;
import graphics.scene.Camera;
import graphics.scene.Light;
import graphics.scene.PerspectiveCamera;
import graphics.scene.Scene;
import matrix.*;
import volume.Volumetric;
import world.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Period;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public class Renderer {

	private Camera camera;
	private Rectangle viewport;
	private Rectangle window;
	private BufferedImage frameBuffer;

	private AtomicInteger pixelsComplete = new AtomicInteger(0);

	public Renderer(Rectangle window) {

		camera = new PerspectiveCamera();
		setWindow(window);
		setViewport(getWindow());
	}

	public void render(Scene scene, int samples, int depth) {

		pixelsComplete.set(0);

		Mat4 projection = camera.getProjection(viewport.getSize()).multiply(camera.getTransform());
		Mat4 inverseMatrix = projection.inverse();
		Vec3 near, far;

		Vec3[][] frame = new Vec3[viewport.height][viewport.width];
		float maxBrightness = 0;

		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newWorkStealingPool(threads);
		Dimension size = new Dimension(40, 30);
		for (int y = 0; y < viewport.height; y += size.height) {
			for (int x = 0; x < viewport.width; x += size.width) {

				int width = Math.min(viewport.width - x, size.width);
				int height = Math.min(viewport.height - y, size.height);
				Rectangle area = new Rectangle(x, y, width, height);

				executor.execute(() -> {
//					System.out.println(area.x + ", " + area.y);
					renderKernel(scene, samples, depth, inverseMatrix, frame, area);
				});
			}
		}

		executor.shutdown();
		try {
			long time = System.currentTimeMillis();
			while(!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				int pixels = viewport.width * viewport.height;
				float complete = (float) pixelsComplete.get() / pixels;
				long elapsed = (System.currentTimeMillis() - time);
				long left = (long) (elapsed / complete) - elapsed;

				System.out.printf("%.2f%% complete, %s elapsed, %s left\n",
						complete * 100, humanReadableFormat(elapsed), humanReadableFormat(left));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int y = 0; y < viewport.height; y++) {
			for (int x = 0; x < viewport.width; x++) {

				if (frame[y][x] == null)
					frame[y][x] = new Vec3(1, 0, 1);

				if (!Float.isFinite(frame[y][x].x) || !Float.isFinite(frame[y][x].y) || !Float.isFinite(frame[y][x].z))
					frame[y][x] = new Vec3(1, 1, 0);

				maxBrightness = Math.max(maxBrightness, frame[y][x].x);
				maxBrightness = Math.max(maxBrightness, frame[y][x].y);
				maxBrightness = Math.max(maxBrightness, frame[y][x].z);
			}
		}

		System.out.println(maxBrightness);
		maxBrightness = 1f;

		for (int y = 0; y < viewport.height; y++) {
			for (int x = 0; x < viewport.width; x++) {
//				System.out.println(frame[y][x]);
//				Vec3 pixel = frame[y][x];
				Vec3 pixel = frame[y][x].divide(maxBrightness);
				pixel.x = (float) Math.sqrt(clamp(pixel.x));
				pixel.y = (float) Math.sqrt(clamp(pixel.y));
				pixel.z = (float) Math.sqrt(clamp(pixel.z));

				Color color = new Color(pixel.x, pixel.y, pixel.z);
				frameBuffer.setRGB(viewport.x + x, window.height - (viewport.y + y) - 1, color.getRGB());
			}
		}
	}

	public static String humanReadableFormat(long milliseconds) {
		return Duration.ofMillis(milliseconds).toString()
				.substring(2)
				.replaceAll("(\\d[HMS])(?!$)", "$1 ")
				.toLowerCase();
	}

	private float clamp(float a) {

		if (Float.isNaN(a) || a < 0) {
			a = 0;
		} else if (a > 1) {
			a = 1;
		}

		return a;
	}

	private void renderKernel(Scene scene, int samples, int depth, Mat4 inverseMatrix, Vec3[][] frame, Rectangle area) {

		Vec3 near, far;

		for (int y = area.y; y < area.y + area.height; y++) {
//			System.out.println(String.format("%.2f%%", 100f * y / viewport.height));
			for (int x = area.x; x < area.x + area.width; x++) {

				float u = (float) (x - viewport.x) / viewport.width * 2 - 1;
				float v = (float) (y - viewport.y) / viewport.height * 2 - 1;
				near = inverseMatrix.multiply(new Vec3(u, v, -1), 1);
				far = inverseMatrix.multiply(new Vec3(u, v, 1), 1);

				frame[y][x] = raycast(scene, new Ray3(near, far), samples, depth);
				pixelsComplete.incrementAndGet();
			}
		}
	}

	private Vec3 raycast(Scene scene, Ray3 ray, int samples, int depth, Object... ignore) {

		ClosestQuery<Volumetric<RenderData>> query = new ClosestQuery<>();

		scene.raytrace(query, ray, ignore);

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

			Vec3 incoming = intersectionData.source.direction.negate();
			float incomingAngle = normal.dot(incoming);

			if (depth --> 0) {
				for (int i = 0; i < samples; i++) {
					Mat3 rotation = Transform.createCoordinateSystem(normal);
					Random random = ThreadLocalRandom.current();
					Vec3 perturbation = sampleHemisphere(random.nextFloat(), random.nextFloat());
					Vec3 perturbed = rotation.multiply(perturbation).normalized();
					Ray3 bounceRay = new Ray3(intersectionData.intersection, perturbed, 100000);
					Vec3 resultColor = raycast(scene, bounceRay, (int) Math.sqrt(samples * 10), depth, volumetric);
					resultColor = shade(
							incoming, bounceRay.direction, normal,
							incomingAngle, normal.dot(bounceRay.direction),
							resultColor, material, textureUV);
					color.set(color.add(resultColor));
				}
				color.set(color.divide(samples));
			}

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

					Vec3 shaded = shade(
							incoming, lightRay.direction, normal,
							incomingAngle, outgoingAngle,
							photon, material, textureUV);
					color.set(color.add(shaded));
				}
			}
		}

		return color;
	}

	Vec3 shade(Vec3 incoming, Vec3 outgoing, Vec3 normal,
			   float incomingAngle, float outgoingAngle,
			   Vec3 light, Material material, Vec2 uv) {

		// specular reflection
		Vec3 halfAngle = incoming.add(outgoing).normalized();

		float roughness = material.roughness.get(uv);

		float brdf = brdf(incomingAngle, outgoingAngle, incoming, normal, halfAngle, roughness);
		if (!Float.isFinite(brdf))
			brdf = 0;

		float specular = brdf * material.specular.get(uv);

		Vec3 diffuse = material.diffuse.get(uv);

		Vec3 emission = new Vec3();
		if (material.emmission != null)
			emission = material.emmission.get(uv);

		return diffuse(outgoingAngle, diffuse, light).add(light.multiply(specular)).add(emission);
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
		float angle = normal.dot(halfAngle);
//		float alpha = (float) Math.acos(angle);
		float alpha = fastAcos(angle);

		float tanAlpha2 = (float) Math.tan(alpha);
		tanAlpha2 = tanAlpha2 * tanAlpha2;
		float roughness2 = roughness * roughness;

		float cosAlpha4 = (float) Math.cos(alpha); // cos(alpha)
		cosAlpha4 *= cosAlpha4; // cos^2(alpha)
		cosAlpha4 *= cosAlpha4; // cos^4(alpha)

		return ((float) Math.exp(-tanAlpha2 / roughness2)) / (((float) Math.PI) * roughness2 * cosAlpha4);
	}

	private final float PI_4 = (float) (Math.PI * Math.PI * Math.PI * Math.PI);
	float fastAtan(float x) {
		return PI_4*x - x*(Math.abs(x) - 1)*(0.2447f + 0.0663f*Math.abs(x));
	}

	float fastAcos(float x) {
		return fastAtan((float) Math.sqrt(1 - x * x) / x);
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

	private Vec3 diffuse(float outgoingAngle, Vec3 diffuse, Vec3 light) {
		return diffuse.multiply(light).multiply(Math.max(0, outgoingAngle));
	}

	private Vec3 sampleHemisphere(float a, float b) {

		float sinTheta = (float) Math.sqrt(1 - a * a);
		float phi = 2 * (float) Math.PI * b;
		float x = sinTheta * (float) Math.cos(phi);
		float z = sinTheta * (float) Math.sin(phi);
		return new Vec3(x, z, a);
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
