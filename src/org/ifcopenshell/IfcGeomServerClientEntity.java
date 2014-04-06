package org.ifcopenshell;

public class IfcGeomServerClientEntity {
	private int id;
	private String guid;
	private String name;
	private String type;
	private int parentId;
	private float[] matrix;
	private int repId;
	private float[] positions;
	private float[] normals;
	private int[] indices;
	private float[] colors;
	private int[] materialIndices;
	
	public IfcGeomServerClientEntity(int id, String guid, String name,
			String type, int parentId, float[] matrix, int repId,
			float[] positions, float[] normals, int[] indices, float[] colors,
			int[] materialIndices) {
		super();
		this.id = id;
		this.guid = guid;
		this.name = name;
		this.type = type;
		this.parentId = parentId;
		this.matrix = matrix;
		this.repId = repId;
		this.positions = positions;
		this.normals = normals;
		this.indices = indices;
		this.colors = colors;
		this.materialIndices = materialIndices;
	}
	
	public int getId() {
		return id;
	}

	public String getGuid() {
		return guid;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getParentId() {
		return parentId;
	}

	public float[] getMatrix() {
		return matrix;
	}

	public int getRepId() {
		return repId;
	}

	public float[] getPositions() {
		return positions;
	}

	public float[] getNormals() {
		return normals;
	}

	public int[] getIndices() {
		return indices;
	}

	public float[] getColors() {
		return colors;
	}

	public int[] getMaterialIndices() {
		return materialIndices;
	}
	
	public int getNumberOfPrimitives() {
		return indices.length / 3;
	}
	
	public int getNumberOfColors() {
		return colors.length / 4;
	}
}
