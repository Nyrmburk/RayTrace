package graphics.scene;

import graphics.Model;
import matrix.Vec3;

public class PointLight implements Light {

	private Vec3 point;

	private Vec3 color = new Vec3();
	float brightness;

	public PointLight(Vec3 point, Vec3 color, float brightness) {
		this.setPoint(point);
		this.setColor(color);
		this.setBrightness(brightness);
	}

	@Override
	public Model getModel() {
		return null;
	}

	@Override
	public Vec3 getSource() {
		return point;
	}

	public void setPoint(Vec3 point) {
		this.point = point;
	}

	@Override
	public Vec3 getLight() {
		return color.multiply(brightness);
	}

	public void setColor(Vec3 color) {
		this.color.set(color);
	}

	public float getBrightness(Vec3 direction) {
		return brightness;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}
}
