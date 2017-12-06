package graphics.renderdata;

import matrix.Vec2;
import matrix.Vec3;
import volume.Volumetric;

public interface Renderable extends Volumetric {

	Vec2 normalizeTextureUV(Vec2 uv);
	Vec3 calculateNormal(Vec2 uv);
}
