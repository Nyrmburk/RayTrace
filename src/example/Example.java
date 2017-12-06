package example;

import graphics.*;
import graphics.renderdata.Renderable;
import graphics.renderdata.TriangleRenderable;
import graphics.scene.Light;
import graphics.scene.PerspectiveCamera;
import graphics.scene.PointLight;
import graphics.scene.Scene;
import graphics.value.NumberSource;
import matrix.Transform;
import matrix.Vec2;
import matrix.Vec3;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Nyrmburk on 8/18/2016.
 */
public class Example {

	public static void main(String[] args) {

		Rectangle window = new Rectangle(640, 480);

		Renderer renderer = new Renderer(window);
		renderer.setCamera(new PerspectiveCamera(75, 0.01f, 200));
//		Transform.translate(renderer.getCamera().getTransform(), new Vec3(0, -5, -15f));
		Transform.translate(renderer.getCamera().getTransform(), new Vec3(0, 0, -5f));
		Transform.rotate(renderer.getCamera().getTransform(), new Vec3(0, 1, 0), -0.4f);
//		Transform.rotate(renderer.getCamera().getTransform(), new Vec3(0, 1, 0), 0.9f);

		Material material = new Material();
		material.diffuseIntensity = new NumberSource(0.5f);
		material.refraction = uv -> 3;

		Scene scene = new Scene();

//		world.addVolume(new Triangle<>(
//				new Vec3(-5, -1, -10),
//				new Vec3(5, -1, -10),
//				new Vec3(0, 7, -10)));
//
//		Material checker = new Material();
//		checker.diffuse = generateChecker();

		Renderable floor1 = new TriangleRenderable(
				new Vec3(-10, -1.5f, 10), // close left
				new Vec3(10, -1.5f, 10), // close right
				new Vec3(-10, -1.5f, -10), // far left
				new Vec3(0, 1, 0),
				new Vec3(0, 1, 0),
				new Vec3(0, 1, 0),
				new Vec2(0, 0),
				new Vec2(1, 0),
				new Vec2(0, 1));
//		floor1.putData(checker);
		scene.addVolume(floor1);

		Renderable floor2 = new TriangleRenderable(
				new Vec3(10, -1.5f, 10), // close right
				new Vec3(10, -1.5f, -10), // far right
				new Vec3(-10, -1.5f, -10), // far left
				new Vec3(0, 1, 0),
				new Vec3(0, 1, 0),
				new Vec3(0, 1, 0),
				new Vec2(1, 0),
				new Vec2(1, 1),
				new Vec2(0, 1));
//		floor2.putData(checker);
		scene.addVolume(floor2);

		Light light = new PointLight(new Vec3(2, 3, 3), new Vec3(1, 1, 1), 10);
		scene.addLight(light);

		Model model = new Model(9);
//		model.setVertex(0, new Vec3(-5, -4, -10));
//		model.setVertex(1, new Vec3(5, -4, -10));
//		model.setVertex(2, new Vec3(0, 0, -10));
//		model.setVertex(3, new Vec3(-5, -3, -5));
//		model.setVertex(4, new Vec3(5, -3, -5));
//		model.setVertex(5, new Vec3(-5, -3, -15));
//		model.setVertex(6, new Vec3(5, -3, -5));
//		model.setVertex(7, new Vec3(5, -3, -15));
//		model.setVertex(8, new Vec3(-5, -3, -15));
		try {
			model.load(new File("resources\\monkey.obj").toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		long time = System.currentTimeMillis();
		scene.addModel(model);
		System.out.println("build:" + (System.currentTimeMillis() - time) + "ms");

		time = System.currentTimeMillis();
		renderer.render(scene);
		System.out.println("render:" + (System.currentTimeMillis() - time) + "ms");

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
