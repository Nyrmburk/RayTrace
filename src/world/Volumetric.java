package world;

import matrix.Ray3;
import matrix.Vec3;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public abstract class Volumetric<E> {

	private E data;
	private int index;

	public abstract IntersectionData intersection(Ray3 ray);

	public abstract Vec3 getRadius();

	public abstract Vec3 getCenter();

	public E getData() {
		return data;
	}

	public void putData(E data) {
		this.data = data;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
