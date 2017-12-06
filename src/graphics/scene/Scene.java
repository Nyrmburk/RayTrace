package graphics.scene;

import graphics.Model;
import graphics.renderdata.Renderable;
import graphics.renderdata.TriangleRenderable;
import world.World;

import java.util.ArrayList;
import java.util.List;

public class Scene extends World<Renderable> {

	private List<Light> lights = new ArrayList<>();
	private List<Camera> cameras = new ArrayList<>(); // technically not needed so I don't know what I'm doing

	public void addModel(Model model) {

		for (int i = 0; i < model.getElementCapacity(); i += 3) {
			Renderable triangle = new TriangleRenderable(
					model.getVertex(i),
					model.getVertex(i+1),
					model.getVertex(i+2),
					model.getNormal(i),
					model.getNormal(i+1),
					model.getNormal(i+2),
					model.getTexCoord(i),
					model.getTexCoord(i+1),
					model.getTexCoord(i+2)
			);
			this.addVolume(triangle);
		}
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
