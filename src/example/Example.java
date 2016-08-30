package example;

import graphics.Material;
import graphics.Model;
import graphics.PerspectiveCamera;
import graphics.Renderer;
import matrix.Ray3;
import matrix.Transform;
import matrix.Vec3;
import volume.Triangle;
import world.World;

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
		renderer.setCamera(new PerspectiveCamera(75, 0.01f, 20));
		Transform.translate(renderer.getCamera().getTransform(), new Vec3(0, 0, -2.5f));
		Transform.rotate(renderer.getCamera().getTransform(), new Vec3(0, 1, 0), 0.4f);

		World<Material> world = new World<>();

//		world.addVolume(new Triangle<>(
//				new Vec3(-5, -1, -10),
//				new Vec3(5, -1, -10),
//				new Vec3(0, 7, -10)));
//
//		Material checker = new Material();
//		checker.diffuse = generateChecker();
//
//		Triangle<Material> floor1 = new Triangle<>(
//				new Vec3(-5, -3, -5),
//				new Vec3(5, -3, -5),
//				new Vec3(-5, -3, -15));
//		floor1.putData(checker);
//		world.addVolume(floor1);
//
//		Triangle<Material> floor2 = new Triangle<>(
//				new Vec3(5, -3, -5),
//				new Vec3(5, -3, -15),
//				new Vec3(-5, -3, -15));
//		floor2.putData(checker);
//		world.addVolume(floor2);

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

		for (int i = 0; i < model.getElementCapacity();)
			world.addVolume(new Triangle<>(model.getVertex(i++), model.getVertex(i++), model.getVertex(i++)));

		renderer.render(world);

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
