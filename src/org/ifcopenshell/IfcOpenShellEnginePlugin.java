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
 * This class ensures that a valid binary is available for the platform the     *
 * code is running on.                                                          *
 *                                                                              *
 ********************************************************************************/

package org.ifcopenshell;

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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.renderengine.RenderEngine;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.bimserver.plugins.renderengine.RenderEnginePlugin;
import org.bimserver.shared.exceptions.PluginException;
import org.bimserver.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcOpenShellEnginePlugin implements RenderEnginePlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(IfcOpenShellEnginePlugin.class);
	
	private boolean initialized = false;
	private String filename;

	@Override
	public RenderEngine createRenderEngine(PluginConfiguration pluginConfiguration, String schema) throws RenderEngineException {
		try {
			return new IfcOpenShellEngine(filename);
		} catch (IOException e) {
			throw new RenderEngineException(e);
		}
	}

	@Override
	public void init(PluginContext pluginContext) throws PluginException {
		try {
			Path exePath = IfcGeomServerClient.getExecutablePathFromRepo(pluginContext.getRootPath());
			final InputStream inputStream = Files.newInputStream(exePath);
			if (inputStream != null) {
				try {
					Path nativeFolder = pluginContext.getTempDir();
					if (Files.exists(nativeFolder)) {
						try {
							PathUtils.removeDirectoryWithContent(nativeFolder);
						} catch (IOException e) {
							// Ignore
						}
					}
					Files.createDirectories(nativeFolder);
					Path file = nativeFolder.resolve(exePath.getFileName());
					OutputStream outputStream = Files.newOutputStream(file);
					try {
						IOUtils.copy(inputStream, outputStream);
					} finally {
						outputStream.close();
					}
					try {
						Files.setPosixFilePermissions(file, Collections.singleton(PosixFilePermission.OWNER_EXECUTE));
					} catch (Exception e) {
						// Ignore.. permission bit tested below
					}
					this.filename = file.toString();
					initialized = new File(filename).canExecute();
					if (initialized) {
						LOGGER.info("Using " + exePath);
					} else {
						throw new PluginException("File " + filename + " is not executable");
					}
				} finally {
					inputStream.close();
				}
			} else {
				throw new PluginException(exePath.toString() + " was not found");
			}
		} catch (Exception e) {
			throw new PluginException(e);
		}
		if (!initialized) {
			throw new PluginException("IfcOpenShell plugin did not initialize successfully");
		}
	}

	@Override
	public ObjectDefinition getSettingsDefinition() {
		return null;
	}
}