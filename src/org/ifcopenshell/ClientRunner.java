package org.ifcopenshell;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

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
			client.setCalculateQuantities(true);
		} catch (RenderEngineException e1) {
			e1.printStackTrace();
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
				for (Map.Entry<String, Double> e : instance.getAllExtendedData().entrySet()) {
					System.out.println(String.format("%s: %.2f", e.getKey(), e.getValue()));
				}
			} catch (RenderEngineException e) {
				e.printStackTrace();
				return;
			}
		}
		System.exit(0); 
	}
	
}