package world;

import matrix.Ray3;
import matrix.Vec2;
import matrix.Vec3;
import volume.Volumetric;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public class IntersectionData<T extends Volumetric> {

	public T volumetric;

	// the depthFraction of the ray length where the intersection occurred
	public float depthFraction;

	// Normal vector of the intersection
	public Ray3 source;
	public Vec3 intersection;
	public Vec3 normal;

	// Model Material coordinates
	public Vec2 uv;

	public IntersectionData(float depthFraction, T volumetric, Ray3 source, Vec3 intersection, Vec3 normal, Vec2 uv) {

		this.depthFraction = depthFraction;
		this.volumetric = volumetric;
		this.source = new Ray3(source.position, source.direction, source.length * depthFraction);
		this.intersection = intersection;
		this.normal = normal;
		this.uv = uv;
	}
}
