package volume;

import matrix.Ray3;
import matrix.Vec2;
import matrix.Vec3;
import matrix.Vec4;
import world.IntersectionData;
import world.Volumetric;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public class Triangle<E> extends Volumetric<E> {

	private static final float EPSILON = 0.00001f;

	public Vec3 a, b, c;

	public Triangle(Vec3 a, Vec3 b, Vec3 c) {

		this.a = a;
		this.b = b;
		this.c = c;
	}

	@Override
	public IntersectionData intersection(Ray3 ray) {

		Vec3 edgeAB, edgeAC;

		edgeAB = b.subtract(a);
		edgeAC = c.subtract(a);

		Vec3 p = ray.direction.cross(edgeAC);
		float s = edgeAB.dot(p);

		if (s > -EPSILON && s < EPSILON)
			return null;

		float sInverse = 1/s;

		Vec3 q = ray.position.subtract(a);
		float u = sInverse * q.dot(p);

		if (u < 0 || u > 1)
			return null;

		Vec3 r = q.cross(edgeAB);
		float v = sInverse * ray.direction.dot(r);

		if (v < 0 || u + v > 1)
			return null;

		float depth = sInverse * edgeAC.dot(r) / ray.length;

		if (depth < 0 || depth > 1)
			return null;

		Vec3 normal = edgeAB.cross(edgeAC);

		return new IntersectionData(depth, new Ray3(ray.point(depth), normal), new Vec2(u, v));
	}

	@Override
	public Vec3 getRadius() {
		return null;
	}

	@Override
	public Vec3 getCenter() {
		return null;
	}
}
