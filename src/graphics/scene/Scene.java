package graphics.scene;

import graphics.Model;
import graphics.renderdata.RenderData;
import graphics.renderdata.TriangleRenderData;
import matrix.Mat3;
import matrix.Mat4;
import matrix.Transform;
import volume.Triangle;
import volume.Volumetric;
import world.World;

import java.util.ArrayList;
import java.util.List;

public class Scene extends World<Volumetric<RenderData>> {

	public List<Light> lights = new ArrayList<>();
	private List<Camera> cameras = new ArrayList<>(); // technically not needed so I don't know what I'm doing

	public void addModel(Model model, Mat4 transform) {

		Mat3 rotation = Transform.getRotationMatrix(transform).transpose();

		for (int i = 0; i < model.getElementCapacity(); i += 3) {
			Triangle<RenderData> triangle = TriangleRenderData.renderableTriangle(
					transform.multiply(model.getVertex(i), 1),
					transform.multiply(model.getVertex(i + 1), 1),
					transform.multiply(model.getVertex(i + 2), 1),
					rotation.multiply(model.getNormal(i)),
					rotation.multiply(model.getNormal(i + 1)),
					rotation.multiply(model.getNormal(i + 2)),
					model.getTexCoord(i),
					model.getTexCoord(i + 1),
					model.getTexCoord(i + 2),
					model.getMaterial());

			this.addVolume(triangle);
		}
	}

	public void addModel(Model model) {
		addModel(model, Transform.identity());
	}

	public void addLight(Light light) {
		lights.add(light);
		if (light.getModel() != null)
			addModel(light.getModel());
	}

	public void removeLight(Light light) {
		lights.remove(light);
	}
}
