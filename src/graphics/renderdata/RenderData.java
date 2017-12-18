package graphics.renderdata;

import graphics.Material;
import matrix.Vec2;
import matrix.Vec3;
import volume.Volumetric;

public abstract class RenderData {

	private Material material;

	public RenderData(Material material) {
		setMaterial(material);
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public abstract Vec2 normalizeTextureUV(Volumetric volumetric, Vec2 uv);
	public abstract Vec3 calculateNormal(Volumetric volumetric, Vec2 uv);
	public abstract Vec3 perturbNormal(Volumetric<RenderData> volumetric, Vec3 normal, Vec3 tangentNormal);
}
