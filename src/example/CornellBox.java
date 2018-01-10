package example;

import graphics.Material;
import graphics.Renderer;
import graphics.renderdata.RenderData;
import graphics.renderdata.TriangleRenderData;
import graphics.scene.*;
import matrix.Transform;
import matrix.Vec2;
import matrix.Vec3;
import volume.Triangle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CornellBox {

	public static void main(String[] args) {

		Rectangle window = new Rectangle(512, 512);

		Camera camera = new PerspectiveCamera(64.5f, 1, 20);
		Transform.translate(camera.getTransform(), new Vec3(0, -2.744f, -8));
//		Transform.rotate(camera.getTransform(), new Vec3(0, 1, 0), -0.4f);

		Renderer renderer = new Renderer(window);
		renderer.setCamera(camera);

		Scene scene = new Scene();

		for (Triangle<RenderData> tri : getCornellBox())
			scene.addVolume(tri);

		renderer.render(scene, 64, 4);

		BufferedImage image = renderer.getFrameBuffer();

		try {
			ImageIO.write(image, "PNG", new File("cornell-box.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<Triangle<RenderData>> getCornellBox() {

		List<Triangle<RenderData>> tris = new ArrayList<>();

		float specular = 0.1f;
		float roughness = 0.9f;

		Material white = new Material();
		white.diffuse = uv -> new Vec3(0.7295f, 0.7355f, 0.729f);
		white.specular = uv -> specular;
		white.roughness = uv -> roughness;

		Material red = new Material();
		red.diffuse = uv -> new Vec3(0.611f, 0.0555f, 0.062f);
		red.specular = uv -> specular;
		red.roughness = uv -> roughness;

		Material green = new Material();
		green.diffuse = uv -> new Vec3(0.117f, 0.4125f, 0.115f);
		green.specular = uv -> specular;
		green.roughness = uv -> roughness;

		Material light = new Material();
		light.diffuse = uv -> new Vec3(0.78f, 0.78f, 0.78f);
		light.emmission = uv -> new Vec3(16.86f, 10.76f, 3.7f);

		Vec3[] boxVerts = new Vec3[]{
				new Vec3(2.764f, 0.000f, 0.0000f),
				new Vec3(2.764f, 5.488f, 0.0000f),
				new Vec3(2.764f, 0.000f, -5.592f),
				new Vec3(2.764f, 5.488f, -5.592f),
				new Vec3(-2.764f, 0f, 0f),
				new Vec3(-2.764f, 5.488f, 0f),
				new Vec3(-2.732f, 0f, -5.592f),
				new Vec3(-2.732f, 5.488f, -5.592f),
				new Vec3(0.644f, 5.488f, -3.32f),
				new Vec3(0.634f, 5.488f, -2.27f),
				new Vec3(-0.666f, 5.488f, -2.27f),
				new Vec3(-0.666f, 5.488f, -3.32f),
		};

		// floor
		tris.addAll(quad(
				boxVerts[4],
				boxVerts[0],
				boxVerts[2],
				boxVerts[6],
				white
		));

		// ceiling front
		tris.addAll(quad(
				boxVerts[5],
				boxVerts[10],
				boxVerts[9],
				boxVerts[1],
				white
		));

		// ceiling left
		tris.addAll(quad(
				boxVerts[7],
				boxVerts[11],
				boxVerts[10],
				boxVerts[5],
				white
		));

		// ceiling back
		tris.addAll(quad(
				boxVerts[3],
				boxVerts[8],
				boxVerts[11],
				boxVerts[7],
				white
		));

		// ceiling right
		tris.addAll(quad(
				boxVerts[1],
				boxVerts[9],
				boxVerts[8],
				boxVerts[3],
				white
		));

		// back wall
		tris.addAll(quad(
				boxVerts[7],
				boxVerts[6],
				boxVerts[2],
				boxVerts[3],
				white
		));

		// left wall
		tris.addAll(quad(
				boxVerts[4],
				boxVerts[6],
				boxVerts[7],
				boxVerts[5],
				red
		));

		// right wall
		tris.addAll(quad(
				boxVerts[3],
				boxVerts[2],
				boxVerts[0],
				boxVerts[1],
				green
		));

		// light
		tris.addAll(quad(
				boxVerts[9],
				boxVerts[10],
				boxVerts[11],
				boxVerts[8],
				light
		));

		Vec3[] shortBlockVerts = new Vec3[]{
				new Vec3(1.943792f, 0f, -2.25f),
				new Vec3(1.943792f, 1.65f, -2.25f),
				new Vec3(0.363389f, 0f, -2.72f),
				new Vec3(0.363389f, 1.65f, -2.72f),
				new Vec3(1.463669f, 0f, -0.65f),
				new Vec3(1.463669f, 1.65f, -0.65f),
				new Vec3(-0.136738f, 0f, -1.14f),
				new Vec3(-0.136738f, 1.65f, -1.14f),
		};

		Material shortBlockMaterial = white;

		tris.addAll(quad(
				shortBlockVerts[3],
				shortBlockVerts[1],
				shortBlockVerts[0],
				shortBlockVerts[2],
				shortBlockMaterial
		));

		tris.addAll(quad(
				shortBlockVerts[7],
				shortBlockVerts[3],
				shortBlockVerts[2],
				shortBlockVerts[6],
				shortBlockMaterial
		));

		tris.addAll(quad(
				shortBlockVerts[5],
				shortBlockVerts[7],
				shortBlockVerts[6],
				shortBlockVerts[4],
				shortBlockMaterial
		));

		tris.addAll(quad(
				shortBlockVerts[1],
				shortBlockVerts[5],
				shortBlockVerts[4],
				shortBlockVerts[0],
				shortBlockMaterial
		));

		tris.addAll(quad(
				shortBlockVerts[4],
				shortBlockVerts[6],
				shortBlockVerts[2],
				shortBlockVerts[0],
				shortBlockMaterial
		));

		tris.addAll(quad(
				shortBlockVerts[1],
				shortBlockVerts[3],
				shortBlockVerts[7],
				shortBlockVerts[5],
				shortBlockMaterial
		));
		
		Vec3[] tallBlockVerts = new Vec3[] {
				new Vec3(0.113326f, 0f, -2.96f),
				new Vec3(0.113326f, 3.3f, -2.96f),
				new Vec3(-0.3768f, 0f, -4.56f),
				new Vec3(-0.3768f, 3.3f, -4.56f),
				new Vec3(-1.467076f, 0f, -2.47f),
				new Vec3(-1.467076f, 3.3f, -2.47f),
				new Vec3(-1.957201f, 0f, -4.06f),
				new Vec3(-1.957201f, 3.3f, -4.06f),
		};

		Material tallBlockMaterial = white;

		tris.addAll(quad(
				tallBlockVerts[3],
				tallBlockVerts[1],
				tallBlockVerts[0],
				tallBlockVerts[2],
				tallBlockMaterial
		));

		tris.addAll(quad(
				tallBlockVerts[7],
				tallBlockVerts[3],
				tallBlockVerts[2],
				tallBlockVerts[6],
				tallBlockMaterial
		));

		tris.addAll(quad(
				tallBlockVerts[5],
				tallBlockVerts[7],
				tallBlockVerts[6],
				tallBlockVerts[4],
				tallBlockMaterial
		));

		tris.addAll(quad(
				tallBlockVerts[1],
				tallBlockVerts[5],
				tallBlockVerts[4],
				tallBlockVerts[0],
				tallBlockMaterial
		));

		tris.addAll(quad(
				tallBlockVerts[4],
				tallBlockVerts[6],
				tallBlockVerts[2],
				tallBlockVerts[0],
				tallBlockMaterial
		));

		tris.addAll(quad(
				tallBlockVerts[1],
				tallBlockVerts[3],
				tallBlockVerts[7],
				tallBlockVerts[5],
				tallBlockMaterial
		));

		return tris;
	}

	private static List<Triangle<RenderData>> quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, Material material) {

		List<Triangle<RenderData>> tris = new ArrayList<>(2);

		Vec3 p = b.subtract(a);
		Vec3 q = c.subtract(a);

		Vec3 normal = p.cross(q).normalized();

		tris.add(TriangleRenderData.renderableTriangle(
				a, b, c,
				normal, normal, normal,
				new Vec2(0, 0), new Vec2(1, 0), new Vec2(1, 1),
				material));

		tris.add(TriangleRenderData.renderableTriangle(
				c, d, a,
				normal, normal, normal,
				new Vec2(1, 1), new Vec2(0, 1), new Vec2(0, 0),
				material));

		return tris;
	}
}
