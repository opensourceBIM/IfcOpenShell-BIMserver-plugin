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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.renderengine.RenderEngine;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.bimserver.plugins.renderengine.RenderEnginePlugin;
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
	public String getDescription() {
		return "Open source IFC geometry engine<br>visit <a href='http://ifcopenshell.org'>ifcopenshell.org</a>";
	}

	public static String getVersionStatic() {
		return "0.5.0-dev";
	}

	@Override
	public String getVersion() {
		return getVersionStatic();
	}

	@Override
	public void init(PluginManager pluginManager) throws PluginException {
		PluginContext pluginContext = pluginManager.getPluginContext(this);
		final String os = System.getProperty("os.name").toLowerCase();
		final String executableName;
		final String operatingSystem;
		if (os.contains("windows")) {
			operatingSystem = "win";
			executableName = "IfcGeomServer.exe";
		} else if (os.contains("osx") || os.contains("os x") || os.contains("darwin")) {
			operatingSystem = "osx";
			executableName = "IfcGeomServer";
		} else if (os.contains("linux")) {
			operatingSystem = "linux";
			executableName = "IfcGeomServer";
		} else {
			throw new PluginException(String.format("IfcOpenShell is not available on the %s platorm", os));
		}
		try {
			final String bitness = operatingSystem.equals("osx") ? "64" : System.getProperty("sun.arch.data.model");
			final String exePath = String.format("exe/%s/%s/%s", bitness, operatingSystem, executableName);
			final InputStream inputStream = pluginContext.getResourceAsInputStream(exePath);
			if (inputStream != null) {
				File nativeFolder = new File(pluginManager.getTempDir(), "IfcOpenShellEngine");
				if (nativeFolder.exists()) {
					try {
						FileUtils.deleteDirectory(nativeFolder);
					} catch (IOException e) {
						// Ignore
					}
				}
				FileUtils.forceMkdir(nativeFolder);
				File file = new File(nativeFolder, executableName);
				IOUtils.copy(inputStream, new FileOutputStream(file));
				try {
					file.setExecutable(true, false);
				} catch (Exception e) {
					// Ignore.. permission bit tested below
				}
				this.filename = file.getAbsolutePath();
				initialized = new File(filename).canExecute();
				if (initialized) {
					LOGGER.info("Using " + exePath);
				}
			}
		} catch (Exception e) {
			throw new PluginException(e);
		}
		if (!initialized) {
			throw new PluginException(String.format("No executable found for the %s platorm", os));
		}
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public String getDefaultName() {
		return "IfcOpenShell Engine";
	}

	@Override
	public ObjectDefinition getSettingsDefinition() {
		return null;
	}
}
