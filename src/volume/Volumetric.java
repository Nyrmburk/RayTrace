package volume;

import matrix.Ray3;
import world.IntersectionData;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public interface Volumetric {

	IntersectionData intersection(final Ray3 ray);

	AABB getBounds();
}
