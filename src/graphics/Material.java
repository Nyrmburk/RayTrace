package graphics;

import graphics.value.ValueSource;
import graphics.value.ValueSource3;
import matrix.Vec3;

/**
 * Created by Nyrmburk on 8/18/2016.
 */
public class Material {

	public ValueSource3 diffuse = uv -> new Vec3(0, 0, 0);
	public ValueSource3 emmission;
	public ValueSource3 normal;
	public ValueSource specular = uv -> 1;
	public ValueSource roughness = uv -> 1;
	public ValueSource opacity;
	public ValueSource refraction;
	public ValueSource scatter;
}
