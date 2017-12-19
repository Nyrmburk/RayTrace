package world;

import matrix.Mat4;
import volume.AABB;
import volume.Volumetric;

public class BoundingVolumeHeirarchy<E extends Volumetric> {

	private BoundingVolumeHeirarchy<E> left;
	private BoundingVolumeHeirarchy<E> right;

	private E value;
	private AABB bounds;

	public BoundingVolumeHeirarchy() {
		this(null, null);
	}

	private BoundingVolumeHeirarchy(E value, AABB bounds) {
		this.value = value;
		this.bounds = bounds;
	}

	public void query(bvhQuery<E> query) {

		if (query.shouldContinue(bounds)) {

			if (value != null) {
				query.foundValue(value);
			} else {
				left.query(query);
				right.query(query);
			}
		}
	}

	public void add(E value) {

		if (this.value == null && left == null) {

			// no value has been set for this node
			this.value = value;
			this.bounds = value.getBounds();
		} else if (left == null) {

			// filling out the child nodes
			// there are two types, nodes and leafs
			// in this case the leaf becomes a node and the value is nullified
			// the current value becomes the left node and the new value is the right
			left = new BoundingVolumeHeirarchy<>(this.value, this.bounds);
			right = new BoundingVolumeHeirarchy<>(value, value.getBounds());
			this.value = null;
			this.bounds = AABB.combine(left.bounds, right.bounds);
		} else {

			// the child nodes are full, so this node needs to grow
			// the value needs to be passed down into the children
			// the smaller child is going to be the recipient to help grow equally
			if (left.bounds.size() < right.bounds.size()) {
				left.add(value);
			} else {
				right.add(value);
			}
			this.bounds = AABB.combine(left.bounds, right.bounds);
		}
	}

	interface bvhQuery<E extends Volumetric> {

		boolean shouldContinue(AABB bounds);
		void foundValue(E value);
	}
}
