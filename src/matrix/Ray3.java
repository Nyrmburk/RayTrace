package matrix;

/**
 * Created by Nyrmburk on 6/2/2016.
 */
public class Ray3 {

	public Vec3 position;
	public Vec3 direction;
	public float length;

	// position, direction, length
	public Ray3(Vec3 position, Vec3 direction, float length) {

		this.position = position;
		this.direction = direction;
		this.length = length;
	}

	// start position, end position
	public Ray3(Vec3 startPosition, Vec3 endPosition) {

		this.position = startPosition;
		this.direction = endPosition.subtract(startPosition);
		this.length = direction.length();
		this.direction = this.direction.normalized();
	}

	public Ray3(float... pn) {

		this(new Vec3(pn[0], pn[1], pn[2]), new Vec3(pn[3], pn[4], pn[5]));
	}

	public Vec3 point(float t) {

		return position.add(direction.multiply(t * length));
	}
}
