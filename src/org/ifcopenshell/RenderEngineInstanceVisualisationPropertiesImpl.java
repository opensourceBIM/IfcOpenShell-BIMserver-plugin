package org.ifcopenshell;

import org.bimserver.plugins.renderengine.RenderEngineInstanceVisualisationProperties;

public class RenderEngineInstanceVisualisationPropertiesImpl extends RenderEngineInstanceVisualisationProperties {

	public RenderEngineInstanceVisualisationPropertiesImpl(IfcGeomServerClientEntity entity) {
		super(
				entity == null || entity.getIndices() == null ? null : entity.getIndices(), 
				entity == null || entity.getPositions() == null ? null : entity.getPositions(), 
				entity == null || entity.getNormals() == null ? null : entity.getNormals(),
				entity == null || entity.getMaterialIndices() == null ? null : entity.getMaterialIndices(),
				entity == null || entity.getColors() == null ? null : entity.getColors());
	}
}
