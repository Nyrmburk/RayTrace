package graphics.renderdata;

import matrix.Vec2;
import matrix.Vec3;
import volume.Triangle;

public class TriangleRenderable extends Triangle implements Renderable {

	Vec3 normalA;
	Vec3 normalB;
	Vec3 normalC;

	Vec2 textureCoordA;
	Vec2 textureCoordB;
	Vec2 textureCoordC;

	public TriangleRenderable(Vec3 vertexA,
							  Vec3 vertexB,
							  Vec3 vertexC,
							  Vec3 normalA,
							  Vec3 normalB,
							  Vec3 normalC,
							  Vec2 textureCoordA,
							  Vec2 textureCoordB,
							  Vec2 textureCoordC) {

		super(vertexA, vertexB, vertexC);

		this.normalA = normalA;
		this.normalB = normalB;
		this.normalC = normalC;

		this.textureCoordA = textureCoordA;
		this.textureCoordB = textureCoordB;
		this.textureCoordC = textureCoordC;
	}

	@Override
	public Vec2 normalizeTextureUV(Vec2 uv) {

		return textureCoordA.multiply(uv.x)
				.add(textureCoordB.multiply(uv.y))
				.add(textureCoordC.multiply(1 - (uv.x + uv.y)));
	}

	@Override
	public Vec3 calculateNormal(Vec2 uv) {
		return normalA.multiply(1 - (uv.x + uv.y))
				.add(normalB.multiply(uv.x))
				.add(normalC.multiply(uv.y));
	}
}
