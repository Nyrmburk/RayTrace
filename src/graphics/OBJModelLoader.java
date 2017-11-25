package graphics;

import matrix.Vec2;
import matrix.Vec3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nyrmburk on 5/13/2016.
 */
public class OBJModelLoader {

	// model information
	private List<Vec3> vertices = new ArrayList<>();
	private List<Vec3> normals = new ArrayList<>();
	private List<Vec2> textureCoords = new ArrayList<>();
	private List<Integer> vertexIndices = new ArrayList<>();
	private List<Integer> normalIndices = new ArrayList<>();
	private List<Integer> textureIndices = new ArrayList<>();

	public Model load(Path path) throws IOException {

		// read file line by line
		BufferedReader read = null;

		Model model = null;

		try {
			String currentLine;
			FileReader reader = new FileReader(path.toFile());
			read = new BufferedReader(reader);

			while ((currentLine = read.readLine()) != null) {
				if (currentLine.isEmpty())
					continue;
				parseLine(currentLine);
			}

			model = new Model(vertexIndices.size());

			for (int i = 0; i < vertexIndices.size(); i++) {
				model.setVertex(i, vertices.get(vertexIndices.get(i)));
			}

			for (int i = 0; i < normals.size(); i++)
				model.setNormal(i, normals.get(normalIndices.get(i)));

			for (int i = 0; i < textureCoords.size(); i++)
				model.setTexCoord(i, textureCoords.get(textureIndices.get(i)));

			if (normals.isEmpty())
				model.generateNormals();

			model.rewindBuffers();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (read != null)
				read.close();
		}

		return model;
	}

	public static void save(Path path, Model model) {

	}

	public void parseLine(String line) {
		// http://www.martinreddy.net/gfx/3d/OBJ.spec

		String[] arguments = line.split("\\s+");

		// find out what the type definition is
		switch (arguments[0]) {

			case "#":// comment
				break;
			// vertex data
			case "v":// vertex
				putVec3(vertices, arguments, 1);
				break;
			case "vt":// texture vertex
				putVec2(textureCoords, arguments, 1);
				break;
			case "vn":// vertex normal
				putVec3(normals, arguments, 1);
				break;
			// elements
			case "f":// face

				// line formatted as:
				// vertex_index/texture_index/normal_index
				// where texture_index and normal_index can be missing
				// and that pattern is repeated in the line for every vertex in the face

				int[] vertexIndices = new int[arguments.length - 1];
				int[] textureIndices = new int[arguments.length - 1];
				int[] normalIndices = new int[arguments.length - 1];

				@SuppressWarnings("unused")
				boolean useTextures = false;
				@SuppressWarnings("unused")
				boolean useNormals = false;

				for (int i = 0; i < arguments.length - 1; i++) {

					String[] face = arguments[i + 1].split("/");
					if (!face[0].isEmpty())
						vertexIndices[i] = Integer.parseInt(face[0]) - 1;

					if (face.length < 2)
						continue;
					if (!face[1].isEmpty()) {

						textureIndices[i] = Integer.parseInt(face[1]) - 1;
						useTextures = true;
					}

					if (face.length < 3)
						continue;
					if (!face[2].isEmpty()) {

						normalIndices[i] = Integer.parseInt(face[2]) - 1;
						useNormals = true;
					}
				}

				if (!useTextures)
					textureIndices = null;
				if (!useNormals)
					normalIndices = null;
				defineFace(vertexIndices, textureIndices, normalIndices);
				break;
			case "lod":// level of detail
				break;
			default:
				break;
		}
	}

	private static void putVec3(List<Vec3> dest, String[] data, int offset) {

		Vec3 vec = new Vec3();

		vec.x = Float.parseFloat(data[    offset]);
		vec.y = Float.parseFloat(data[1 + offset]);
		vec.z = Float.parseFloat(data[2 + offset]);

		dest.add(vec);
	}

	private static void putVec2(List<Vec2> dest, String[] data, int offset) {

		Vec2 vec = new Vec2();

		vec.x = Float.parseFloat(data[    offset]);
		vec.y = Float.parseFloat(data[1 + offset]);

		dest.add(vec);
	}

	public void defineFace(int[] vertexIndices, int[] textureIndices, int[] normalIndices) {

		int[][] triangles = triangulate(vertexIndices.length);

		for (int[] triangle : triangles) {
			for (int index : triangle) {
				this.vertexIndices.add(vertexIndices[index]);
				if (textureIndices != null)
					this.textureIndices.add(textureIndices[index]);
				if (normalIndices != null)
					this.normalIndices.add(normalIndices[index]);
			}
		}
	}

	private static int[][] triangulate(int sides) {
		boolean[] discard = new boolean[sides];

		List<int[]> triangles = new ArrayList<>();
		int index = 0;
		for (int i = 0; i < sides - 2; i++) {
			int[] tri = new int[3];

			for (int j = 0; j < 3; j++) {
				if (index < sides) {

					if (!discard[index]) {
						tri[j] = index;
					} else {
						j--;
					}
					if (j == 1)
						discard[index] = true;
					if (j != 2)
						index++;

				} else {
					index = 0;
					j--;
				}
			}
			triangles.add(tri);
		}

		int[][] trianglesArray = new int[triangles.size()][3];
		triangles.toArray(trianglesArray);
		return trianglesArray;
	}
}
