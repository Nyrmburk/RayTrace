package world;

/**
 * Created by Nyrmburk on 8/17/2016.
 */
public interface RaycastQuery<E> {

	float intersection(Object data, IntersectionData intersection);
}
