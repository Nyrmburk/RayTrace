package volume;

import matrix.Ray3;
import matrix.Vec3;

public class AABB {

	private Vec3 lower;
	private Vec3 upper;

	public AABB(Vec3... vecs) {

		lower = vecs[0];
		upper = vecs[0];
		for (int i = 1; i < vecs.length; i++) {
			lower = min(lower, vecs[i]);
			upper = max(upper, vecs[i]);
		}
	}
	
	public static AABB combine(AABB a, AABB b) {

		Vec3 lower = min(a.lower, b.lower);
		Vec3 upper = max(a.upper, b.upper);
		
		return new AABB(lower, upper);
	}
	
	private static Vec3 min(Vec3 a, Vec3 b) {
		Vec3 lower = new Vec3();
		lower.x = Math.min(a.x, b.x);
		lower.y = Math.min(a.y, b.y);
		lower.z = Math.min(a.z, b.z);
		return lower;
	}
	
	private static Vec3 max(Vec3 a, Vec3 b) {
		Vec3 upper = new Vec3();
		upper.x = Math.max(a.x, b.x);
		upper.y = Math.max(a.y, b.y);
		upper.z = Math.max(a.z, b.z);
		return upper;
	}

	public float size() {
		return upper.subtract(lower).length();
	}
	
	public boolean intersects(Ray3 ray) {

		float temp;

		float tmin = (lower.x - ray.position.x) / ray.direction.x;
		float tmax = (upper.x - ray.position.x) / ray.direction.x;

		if (tmin > tmax) {
			temp = tmin;
			tmin = tmax;
			tmax = temp;
		}

		float tymin = (lower.y - ray.position.y) / ray.direction.y;
		float tymax = (upper.y - ray.position.y) / ray.direction.y;

		if (tymin > tymax) {
			temp = tymin;
			tymin = tymax;
			tymax = temp;
		}

		if ((tmin > tymax) || (tymin > tmax))
			return false;

		if (tymin > tmin)
			tmin = tymin;

		if (tymax < tmax)
			tmax = tymax;

		float tzmin = (lower.z - ray.position.z) / ray.direction.z;
		float tzmax = (upper.z - ray.position.z) / ray.direction.z;

		if (tzmin > tzmax) {
			temp = tzmin;
			tzmin = tzmax;
			tzmax = temp;
		}

		if ((tmin > tzmax) || (tzmin > tmax))
			return false;

		if (tzmin > tmin)
			tmin = tzmin;

		if (tzmax < tmax)
			tmax = tzmax;

		return true;
	}
}
