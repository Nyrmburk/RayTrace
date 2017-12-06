package graphics.value;

import matrix.Vec2;

public class NumberSource implements ValueSource {

	private float value;
	public NumberSource(float value) {
		this.value = value;
	}

	@Override
	public float get(Vec2 uv) {
		return value;
	}
}
