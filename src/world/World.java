package world;

import matrix.Ray3;
import matrix.Vec3;
import matrix.Vec4;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public class World<E> {

	private List<Volumetric<E>> volumes = new ArrayList<>();

	public void raytrace(RaycastQuery<E> query, Ray3 ray) {

		float fraction;
		float length = ray.length;
		Iterator<Volumetric<E>> it = volumes.iterator();
		while (it.hasNext()) {

			Volumetric<E> volume = it.next();

			IntersectionData intersection = volume.intersection(ray);
			if (intersection != null) {

				fraction = query.intersection(volume.getData(), intersection);
				ray.length = length * fraction;
			}
		}
	}

	public void addVolume(Volumetric<E> volume) {

		volumes.add(volume);
	}

	public void removeVolume(Volumetric<E> volume) {

		volumes.remove(volume);
	}

	public void clear() {

		volumes.clear();
	}
}
