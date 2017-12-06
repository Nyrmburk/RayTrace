package world;

import matrix.Ray3;
import volume.AABB;
import volume.Volumetric;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public class World<T extends Volumetric> {

//	private List<T> volumes = new ArrayList<>();
	private BoundingVolumeHeirarchy<T> volumes = new BoundingVolumeHeirarchy<>();

	public void raytrace(RaycastQuery query, Ray3 ray) {

		volumes.query(new BoundingVolumeHeirarchy.bvhQuery<T>() {
			float fraction;
			@Override
			public boolean shouldContinue(AABB bounds) {
				return bounds.intersects(ray);
			}

			@Override
			public void foundValue(T volume) {
				IntersectionData intersection = volume.intersection(ray);
				if (intersection != null) {

					fraction = query.intersection(intersection);
					ray.length *= fraction;
				}
			}
		});
	}

	public void addVolume(T volume) {

		volumes.add(volume);
	}

	public void removeVolume(T volume) {

//		volumes.remove(volume);
	}

	public void clear() {

//		volumes.clear();
	}
}
