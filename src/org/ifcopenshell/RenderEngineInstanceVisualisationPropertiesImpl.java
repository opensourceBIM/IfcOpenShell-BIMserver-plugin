package org.ifcopenshell;

import org.bimserver.plugins.renderengine.RenderEngineInstanceVisualisationProperties;

public class RenderEngineInstanceVisualisationPropertiesImpl extends RenderEngineInstanceVisualisationProperties {
	private IfcGeomServerClientEntity entity;

	public RenderEngineInstanceVisualisationPropertiesImpl(IfcGeomServerClientEntity entity) {
		super(0, 0, entity == null ? 0 : entity.getNumberOfPrimitives(), 0, 0, entity == null ? 0 : entity.getNumberOfColors());
		this.entity = entity;
	}
	
	public float[] getVertices() {
		return entity.getPositions();
	}
	
	public int[] getIndices() {
		return entity.getIndices();
	}
	
	public float[] getMaterials() {
		return entity.getColors();
	}
	
	public int[] getMaterialIndices() {
		return entity.getMaterialIndices();
	}
	
	public float[] getNormals() {
		return entity.getNormals();
	}
}
