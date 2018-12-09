package org.ifcopenshell;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.bimserver.plugins.renderengine.RenderEngineException;

public class ClientRunner {
	
	public static void main(String [] args)
	{
		try (IfcGeomServerClient client = new IfcGeomServerClient(IfcGeomServerClient.ExecutableSource.S3, IfcOpenShellEnginePlugin.DEFAULT_COMMIT_SHA)) {
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
					System.out.println(instance.getAllExtendedData().toString());
				} catch (RenderEngineException e) {
					e.printStackTrace();
					return;
				}
			}
			System.exit(0); 
		} catch (RenderEngineException e) {
			e.printStackTrace();
			return;
		}
		
<<<<<<< HEAD
=======
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
		
		double t0 = java.lang.System.nanoTime();

		while (client.hasNext()) {
			try {
				IfcGeomServerClientEntity instance = client.getNext();
				if (instance == null) {
					System.out.println("Internal error");
					return;
				}
				System.out.println(String.format("%s %s", instance.getType(), instance.getGuid()));
				System.out.println(instance.getAllExtendedData().toString());
			} catch (RenderEngineException e) {
				e.printStackTrace();
				return;
			}
		}
		
		System.out.println(String.format("Conversion took %.2f seconds", (java.lang.System.nanoTime() - t0) / 1.e9));
		
		System.exit(0);
>>>>>>> branch 'master' of https://github.com/opensourceBIM/IfcOpenShell-BIMserver-plugin.git
	}
	
}