package example;

import graphics.*;
import graphics.renderdata.RenderData;
import graphics.renderdata.TriangleRenderData;
import graphics.scene.Light;
import graphics.scene.PerspectiveCamera;
import graphics.scene.PointLight;
import graphics.scene.Scene;
import graphics.value.TextureSource;
import matrix.Transform;
import matrix.Vec2;
import matrix.Vec3;
import volume.Triangle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Nyrmburk on 8/18/2016.
 */
public class Example {

	public static void main(String[] args) {

//		Rectangle window = new Rectangle(320, 240);
		Rectangle window = new Rectangle(640, 480);
//		Rectangle window = new Rectangle(1920, 1080);
//		Rectangle window = new Rectangle(3840, 2160);
//		Rectangle window = new Rectangle(7680,4320);

		Renderer renderer = new Renderer(window);
		renderer.setCamera(new PerspectiveCamera(75, 0.01f, 200));
		Transform.translate(renderer.getCamera().getTransform(), new Vec3(0, -5, -15f));
//		Transform.translate(renderer.getCamera().getTransform(), new Vec3(0, 0, -5f));
		Transform.rotate(renderer.getCamera().getTransform(), new Vec3(0, 1, 0), -0.4f);
//		Transform.rotate(renderer.getCamera().getTransform(), new Vec3(0, 1, 0), 0.9f);

//		Transform.rotate(renderer.getCamera().getTransform(), new Vec3(1, 0, 0), 0.7f);


		BufferedImage texture = null;
		BufferedImage normals = null;
		try {
			texture = ImageIO.read(new File("resources\\chesterfield.png"));
//			normals = ImageIO.read(new File("resources\\sand normal.jpg"));
			normals = ImageIO.read(new File("resources\\chesterfield-normal.png"));
//			normals = ImageIO.read(new File("resources\\foil normal.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Material material = new Material();
		Vec3 diffuse = new Vec3(1f, 1f, 1f);
//		Vec3 diffuse = new Vec3(56/255f, 62/255f, 42/255f);
//		Vec3 diffuse = new Vec3(0.95f, 0.05f, 0.05f);
//		Vec3 diffuse = new Vec3(0.005f, 0.005f, 0.005f);
//		int scale = 10;
//		Vec3 colorA = diffuse;
//		Vec3 colorB = diffuse.multiply(0.5f);
//		material.diffuse = uv -> ((uv.x * scale) % 1 > 0.5f ^ (uv.y * scale) % 1 < 0.5f) ? colorA : colorB;
//		material.diffuse = uv -> new Vec3(1 - (uv.x + uv.y), uv.x, uv.y);
//		material.diffuse = new TextureSource(texture);
		material.diffuse = uv -> diffuse;

		material.specular = uv -> 0.8f;
		material.roughness = uv -> 0.1f;

//		material.normal = new TextureSource(normals);
//		material.normal = uv -> new Vec3(0.5f, 0.5f, 1f);

		Scene scene = new Scene();

//		world.addVolume(new Triangle<>(
//				new Vec3(-5, -1, -10),
//				new Vec3(5, -1, -10),
//				new Vec3(0, 7, -10)));
//
//		Material checker = new Material();
//		checker.diffuse = generateChecker();

		float radius = 10;
		float height = 0f;
		float secondHeight = 0;
		float uvScale = 1;
		Triangle<RenderData> floor1 = TriangleRenderData.renderableTriangle(
				new Vec3(-radius, height, radius), // close left
				new Vec3(radius, secondHeight+height, radius), // close right
				new Vec3(-radius, height, -radius), // far left
				new Vec3(0, 1, 0),
				new Vec3(0, 1, 0),
				new Vec3(0, 1, 0),
				new Vec2(0, 0),
				new Vec2(uvScale, 0),
				new Vec2(0, uvScale),
				material);
		scene.addVolume(floor1);

		Triangle<RenderData> floor2 = TriangleRenderData.renderableTriangle(
				new Vec3(radius, secondHeight+height, radius), // close right
				new Vec3(radius, secondHeight+height, -radius), // far right
				new Vec3(-radius, height, -radius), // far left
				new Vec3(0, 1, 0),
				new Vec3(0, 1, 0),
				new Vec3(0, 1, 0),
				new Vec2(uvScale, 0),
				new Vec2(uvScale, uvScale),
				new Vec2(0, uvScale),
				material);
		scene.addVolume(floor2);

		float heightVariance = 5;
		float minheight = 10;
		float brightnessVariance = 10;
		Random r = new Random();
		long seed = r.nextLong();
//		seed = -3484928076056650180L;
		seed = -6841690328182549244L;
		System.out.println(seed);
		r.setSeed(seed);
		for (int i = 0; i < 3; i++) {
			Vec3 point = new Vec3(
					(r.nextFloat() * 2 - 1) * radius,
					r.nextFloat() * heightVariance + height + minheight,
					(r.nextFloat() * 2 - 1) * radius);
			Vec3 color = new Vec3(r.nextFloat(), r.nextFloat(), r.nextFloat());
			System.out.println(point + ", " + color);
			Light light = new PointLight(point, color, r.nextFloat() * brightnessVariance);
			scene.addLight(light);
		}

		Light light = new PointLight(new Vec3(8, 7, 12), new Vec3(1, 1, 1), 2);
		scene.addLight(light);
//		light = new PointLight(new Vec3(-2, 2, 3), new Vec3(0, 0, 1), 8);
//		scene.addLight(light);

		Model model = new Model();
		try {
//			model.load(new File("resources\\bunnyUV.obj").toPath());
			model.load(new File("resources\\sphere.obj").toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		model.setMaterial(material);

		long time = System.currentTimeMillis();
		scene.addModel(model);
		System.out.println("build:" + (System.currentTimeMillis() - time) + "ms");

		time = System.currentTimeMillis();
		renderer.render(scene, 64, 3);
		System.out.println("render:" + (System.currentTimeMillis() - time) / 1000 + "s");

		BufferedImage image = renderer.getFrameBuffer();

		try {
			ImageIO.write(image, "PNG", new File("image.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static BufferedImage generateChecker() {

		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics2D g = image.createGraphics();
		g.setColor(java.awt.Color.MAGENTA);
		g.fillRect(0, 0, 16, 16);
		g.fillRect(16, 16, 32, 32);
		g.dispose();

		return image;
	}
}
