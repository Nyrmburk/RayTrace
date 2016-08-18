package world;

import matrix.Ray3;
import matrix.Vec2;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public class IntersectionData {

	public float depth;
	public Ray3 intersection;
	public Vec2 uv;

	public IntersectionData(float depth, Ray3 intersection, Vec2 uv) {

		this.depth = depth;
		this.intersection = intersection;
		this.uv = uv;
	}
}
