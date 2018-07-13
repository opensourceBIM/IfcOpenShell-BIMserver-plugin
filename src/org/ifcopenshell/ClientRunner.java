package org.ifcopenshell;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.bimserver.plugins.renderengine.RenderEngineException;

public class ClientRunner {
	
	public static void main(String [] args)
	{
		IfcGeomServerClient client;
		
		try {
			client = new IfcGeomServerClient(IfcGeomServerClient.ExecutableSource.S3);
		} catch (RenderEngineException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			client.loadModel(new FileInputStream(args[0]));
		} catch (FileNotFoundException | RenderEngineException e) {
			e.printStackTrace();
			return;
		}

		while (client.hasNext()) {
			try {
				IfcGeomServerClientEntity instance = client.getNext();
				if (instance == null) {
					System.out.println("Internal error");
					return;
				}
				System.out.println(String.format("%s %s", instance.getType(), instance.getGuid()));
				float area = instance.getExtendedDataAsFloat("TOTAL_SURFACE_AREA");
				float volume = instance.getExtendedDataAsFloat("TOTAL_SHAPE_VOLUME");
				if (instance.getType().equals("IfcSpace")) {
					float walkable = instance.getExtendedDataAsFloat("WALKABLE_SURFACE_AREA");
					System.out.println(String.format("Volume: %.2f; Area: %.2f; Walkable %.2f", volume, area, walkable));
				} else {
					System.out.println(String.format("Volume: %.2f; Area: %.2f", volume, area));
				}
			} catch (RenderEngineException e) {
				e.printStackTrace();
				return;
			}
		}
		System.exit(0); 
	}
	
}