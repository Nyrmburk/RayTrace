package graphics;

import graphics.value.ValueSource;
import graphics.value.ValueSource3;

/**
 * Created by Nyrmburk on 8/18/2016.
 */
public class Material {

	public ValueSource3 normal;
	public ValueSource3 diffuse;
	public ValueSource diffuseIntensity;
	public ValueSource specularIntensity;
	public ValueSource opacity;
	public ValueSource refraction;
	public ValueSource scatter;
}
