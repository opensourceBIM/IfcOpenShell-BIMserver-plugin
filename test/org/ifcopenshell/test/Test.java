package org.ifcopenshell.test;

/******************************************************************************
 * Copyright (C) 2009-2018  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

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
