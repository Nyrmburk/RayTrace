package graphics.value;

import matrix.Vec2;
import matrix.Vec3;

import java.awt.image.BufferedImage;

public class TextureSource implements ValueSource3 {

	private BufferedImage texture;

	public TextureSource(BufferedImage texture) {
		this.texture = texture;
	}

	@Override
	public Vec3 get(Vec2 uv) {
		int x = (int) (uv.x * texture.getWidth());
		int y = (int) (uv.y * texture.getHeight());

		int argb = texture.getRGB(x, y);
		Vec3 color = new Vec3();
		color.x = ((argb & 0x00FF0000) >>> 16) / 255; // red
		color.y = ((argb & 0x0000FF00) >>>  8) / 255; // green
		color.z = ((argb & 0x000000FF)       ) / 255; // blue
		return color;
	}
}
