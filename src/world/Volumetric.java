package world;

import matrix.Ray3;
import matrix.Vec3;
import matrix.Vec4;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public abstract class Volumetric<E> {

	E data;

	public abstract IntersectionData intersection(Ray3 ray);

	public abstract Vec3 getRadius();

	public abstract Vec3 getCenter();

	public E getData() {
		return data;
	}

	public void putData(E data) {
		this.data = data;
	}
}
