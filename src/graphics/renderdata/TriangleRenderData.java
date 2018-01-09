package graphics.renderdata;

import graphics.Material;
import matrix.Mat3;
import matrix.Vec2;
import matrix.Vec3;
import volume.Triangle;
import volume.Volumetric;

public class TriangleRenderData extends RenderData {

	public Vec3 normalA;
	public Vec3 normalB;
	public Vec3 normalC;

	public Vec2 textureCoordA;
	public Vec2 textureCoordB;
	public Vec2 textureCoordC;

	public TriangleRenderData(Vec3 normalA,
							  Vec3 normalB,
							  Vec3 normalC,
							  Vec2 textureCoordA,
							  Vec2 textureCoordB,
							  Vec2 textureCoordC,
							  Material material) {
		super(material);

		this.normalA = normalA;
		this.normalB = normalB;
		this.normalC = normalC;

		this.textureCoordA = textureCoordA;
		this.textureCoordB = textureCoordB;
		this.textureCoordC = textureCoordC;
	}

	public static Triangle<RenderData> renderableTriangle(
			Vec3 vertexA,
			Vec3 vertexB,
			Vec3 vertexC,
			Vec3 normalA,
			Vec3 normalB,
			Vec3 normalC,
			Vec2 textureCoordA,
			Vec2 textureCoordB,
			Vec2 textureCoordC,
			Material material) {

		Triangle<RenderData> triangle = new Triangle<>(vertexA, vertexB, vertexC);
		triangle.setData(new TriangleRenderData(
				normalA, normalB, normalC, textureCoordA, textureCoordB, textureCoordC, material));

		return triangle;
	}

	@Override
	public Vec2 normalizeTextureUV(Volumetric volumetric, Vec2 uv) {

		return textureCoordA.multiply(1 - (uv.x + uv.y))
				.add(textureCoordB.multiply(uv.x))
				.add(textureCoordC.multiply(uv.y));
	}

	@Override
	public Vec3 calculateNormal(Volumetric volumetric, Vec2 uv) {
		return normalA.multiply(1 - (uv.x + uv.y))
				.add(normalB.multiply(uv.x))
				.add(normalC.multiply(uv.y));
	}

	@Override
	public Vec3 perturbNormal(Volumetric<RenderData> volumetric, Vec3 normal, Vec3 perturbation) {

		Triangle<RenderData> triangle = (Triangle<RenderData>) volumetric;

		Vec3 deltaPos1 = triangle.b.subtract(triangle.a);
		Vec3 deltaPos2 = triangle.c.subtract(triangle.a);

		Vec2 deltaUV1 = textureCoordB.subtract(textureCoordA);
		Vec2 deltaUV2 = textureCoordC.subtract(textureCoordA);

		Vec3 tangent = deltaPos1.multiply(deltaUV2.y).subtract(deltaPos2.multiply(deltaUV1.y)).normalized();
		Vec3 bitangent = normal.cross(tangent);

		Mat3 rotation = new Mat3(
				tangent.x,
				tangent.y,
				tangent.z,
				bitangent.x,
				bitangent.y,
				bitangent.z,
				normal.x,
				normal.y,
				normal.z);

		return rotation.multiply(perturbation).normalized();
	}
}
