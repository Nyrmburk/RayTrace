package volume;

import matrix.Mat4;
import matrix.Ray3;
import matrix.Transform;
import matrix.Vec3;
import world.IntersectionData;

public class Sphere<T> extends Volumetric<T> {

	private Vec3 center;
	private float radius;

	public Sphere(Vec3 center, float radius) {
		this.center = center;
		this.radius = radius;
	}

	public Vec3 getCenter() {
		return center;
	}
	public float getRadius() {
		return radius;
	}

	@Override
	public Volumetric<T> transform(Mat4 transform) {

		Sphere<T> sphere = new Sphere<>(transform.multiply(center, 1), radius * transform.m[15]);
		sphere.setData(this.getData());

		return sphere;
	}

	@Override
	public IntersectionData intersection(Ray3 ray) {
		return null;
	}

	@Override
	public AABB getBounds() {

		Vec3 lower = new Vec3();
		lower.x = center.x - radius;
		lower.y = center.y - radius;
		lower.z = center.z - radius;

		Vec3 upper = new Vec3();
		upper.x = center.x + radius;
		upper.y = center.y + radius;
		upper.z = center.z + radius;

		return new AABB(lower, upper);
	}
}
