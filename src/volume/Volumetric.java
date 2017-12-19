package volume;

import matrix.Mat4;
import matrix.Ray3;
import world.IntersectionData;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public abstract class Volumetric<T> {

	private T data;

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public abstract Volumetric<T> transform(Mat4 transform);

	public abstract IntersectionData intersection(final Ray3 ray);

	public abstract AABB getBounds();
}
