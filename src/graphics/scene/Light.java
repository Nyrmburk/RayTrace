package graphics.scene;

import graphics.Model;
import matrix.Vec3;

public interface Light {

	// used so that the scene can add the model to the world
	Model getModel();

	// used so that the renderer has a target to trace to
	Vec3 getSource();

	// used for shading
	Vec3 getColor();
	float getBrightness(Vec3 direction);
}
