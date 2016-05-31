package org.ifcopenshell.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.bimserver.plugins.renderengine.RenderEngineException;
import org.bimserver.plugins.renderengine.RenderEngineGeometry;
import org.bimserver.plugins.renderengine.RenderEngineInstance;
import org.bimserver.plugins.renderengine.RenderEngineModel;
import org.ifcopenshell.IfcOpenShellEngine;

public class Test {
	public static void main(String[] args) {
		new Test().start();
	}

	private void start() {
		try {
			IfcOpenShellEngine ifcOpenShellEngine = new IfcOpenShellEngine("exe/64/win/IfcGeomServer.exe", 0.1, false);
			ifcOpenShellEngine.init();
//			RenderEngineModel model = ifcOpenShellEngine.openModel(new FileInputStream(new File("D:\\Dropbox\\Shared\\IFC files\\ArenA 2014\\3D IFC\\arena.ifc")));
			RenderEngineModel model = ifcOpenShellEngine.openModel(new FileInputStream(new File("C:\\Git\\TestFiles\\TestData\\data\\06-03-01_windows_in_curved_wall_vw.ifc")));
			model.generateGeneralGeometry();
			int nrTriangles = 0;
			for (RenderEngineInstance renderEngineInstance : model.listInstances()) {
				RenderEngineGeometry geometry = renderEngineInstance.generateGeometry();
				nrTriangles += geometry.getIndices().length / 3;
			}
			
			System.out.println("Nr triangles (1mm): " + nrTriangles);

			model.close();
			ifcOpenShellEngine.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RenderEngineException e) {
			e.printStackTrace();
		}
	}
}
