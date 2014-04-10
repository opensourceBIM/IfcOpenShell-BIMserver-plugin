package org.ifcopenshell.test;

import java.io.File;
import java.io.IOException;

import org.bimserver.plugins.renderengine.RenderEngineException;
import org.bimserver.plugins.renderengine.RenderEngineInstance;
import org.bimserver.plugins.renderengine.RenderEngineModel;
import org.ifcopenshell.IfcOpenShellEngine;

public class Test {
	public static void main(String[] args) {
		new Test().start();
	}

	private void start() {
		try {
			IfcOpenShellEngine ifcOpenShellEngine = new IfcOpenShellEngine("exe/64/win/IfcGeomServer.exe");
			RenderEngineModel model = ifcOpenShellEngine.openModel(new File("E:\\Git\\BIMserver\\TestData\\data\\AC11-FZK-Haus-IFC.ifc"));
			model.generateGeneralGeometry();
			RenderEngineInstance instance = model.getInstanceFromExpressId(12345);
			System.out.println(instance.getTransformationMatrix());
			model.close();
			ifcOpenShellEngine.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RenderEngineException e) {
			e.printStackTrace();
		}
	}
}
