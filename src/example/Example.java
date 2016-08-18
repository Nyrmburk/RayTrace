package example;

import graphics.Material;
import graphics.PerspectiveCamera;
import graphics.Renderer;
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

		Dimension resolution = new Dimension(640, 480);

		Renderer renderer = new Renderer();
		renderer.setWindow(new Rectangle(renderer.getWindow().getLocation(), resolution));
		renderer.setCamera(new PerspectiveCamera(90, 0.01f, 20));

		World<Material> world = new World<>();

		world.addVolume(new Triangle<>(
				new Vec3(-5, -1, -10),
				new Vec3(5, -1, -10),
				new Vec3(0, 7, -10)));

		Material checker = new Material();
		checker.diffuse = generateChecker();

		Triangle<Material> floor1 = new Triangle<>(
				new Vec3(-5, -3, -5),
				new Vec3(5, -3, -5),
				new Vec3(-5, -3, -15));
		floor1.putData(checker);
		world.addVolume(floor1);

		Triangle<Material> floor2 = new Triangle<>(
				new Vec3(5, -3, -5),
				new Vec3(5, -3, -15),
				new Vec3(-5, -3, -15));
		floor2.putData(checker);
		world.addVolume(floor2);

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
