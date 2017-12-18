package world;

import volume.Volumetric;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public interface RaycastQuery<T extends Volumetric> {

	float intersection(IntersectionData<T> intersection);
}
