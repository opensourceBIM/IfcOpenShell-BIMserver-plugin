/*******************************************************************************
*                                                                              *
* This file is part of IfcOpenShell.                                           *
*                                                                              *
* IfcOpenShell is free software: you can redistribute it and/or modify         *
* it under the terms of the Lesser GNU General Public License as published by  *
* the Free Software Foundation, either version 3.0 of the License, or          *
* (at your option) any later version.                                          *
*                                                                              *
* IfcOpenShell is distributed in the hope that it will be useful,              *
* but WITHOUT ANY WARRANTY; without even the implied warranty of               *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the                 *
* Lesser GNU General Public License for more details.                          *
*                                                                              *
* You should have received a copy of the Lesser GNU General Public License     *
* along with this program. If not, see <http://www.gnu.org/licenses/>.         *
*                                                                              *
********************************************************************************/

/*******************************************************************************
*                                                                              *
* An intermediate between the Plugin implementation and the Model              *
* implementation, nothing fancy going on here.                                 *
*                                                                              *
********************************************************************************/

package org.ifcopenshell;

import java.io.IOException;
import java.io.InputStream;

import org.bimserver.plugins.renderengine.RenderEngine;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.bimserver.plugins.renderengine.RenderEngineModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcOpenShellEngine implements RenderEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger(IfcOpenShellEngine.class);
	public static final Boolean debug = false;
	private String filename;
	private IfcGeomServerClient client;
	private Double maxDeflection;

	public IfcOpenShellEngine(String fn, Double maxDeflection) throws IOException {
		filename = fn;
		this.maxDeflection = maxDeflection;
	}

	@Override
	public void init() throws RenderEngineException {
		LOGGER.debug("Initializing IfcOpenShell engine");
		
		client = new IfcGeomServerClient(filename);
		client.setDeflection(maxDeflection);
	}
	
	@Override
	public void close() {
		LOGGER.debug("Closing IfcOpenShell engine");
	}

	@Override
	public RenderEngineModel openModel(InputStream inputStream, long size) throws RenderEngineException {
		return openModel(inputStream);
	}

	@Override
	public RenderEngineModel openModel(InputStream inputStream) throws RenderEngineException {
		if (!client.isRunning()) {
			client = new IfcGeomServerClient(filename);
		}
		try {
			IfcOpenShellModel model = new IfcOpenShellModel(client, filename, inputStream);
			return model;
		} catch (IOException e) {
			throw new RenderEngineException(e);
		}
	}
}