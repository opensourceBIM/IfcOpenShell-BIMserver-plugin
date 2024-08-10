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
 * Copyright (C) 2009-2019  BIMserver.org
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

import java.io.IOException;
import java.nio.file.Path;

import org.bimserver.models.store.BooleanType;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.models.store.ParameterDefinition;
import org.bimserver.models.store.PrimitiveDefinition;
import org.bimserver.models.store.PrimitiveEnum;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.models.store.StringType;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.renderengine.RenderEngine;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.bimserver.plugins.renderengine.RenderEnginePlugin;
import org.bimserver.plugins.renderengine.VersionInfo;
import org.bimserver.shared.exceptions.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcOpenShellEnginePlugin implements RenderEnginePlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(IfcOpenShellEnginePlugin.class);
	public static final String DEFAULT_BUILD_VERSION = "v0.6.0-721fe47"; // last v0.6.0 version, from 17.09.2021
	private static final String BUILD_VERSION = "buildversion";
	private static final String CALCULATE_QUANTITIES_SETTING = "calculatequantities";
	private static final String APPLY_LAYER_SETS = "applylayersets";
	private static final String DISABLE_OPENING_SUBTRACTIONS = "disableopeningsubtrations";
	private Path executableFilename;
	private VersionInfo versionInfo;
	private boolean calculateQuantities = true;
	private boolean applyLayerSets = true;
	private boolean disableOpeningSubtractions = false;

	@Override
	public RenderEngine createRenderEngine(PluginConfiguration pluginConfiguration, String schema) throws RenderEngineException {
		try {
			return new IfcOpenShellEngine(executableFilename, calculateQuantities, applyLayerSets, disableOpeningSubtractions);
		} catch (IOException e) {
			throw new RenderEngineException(e);
		}
	}

	@Override
	public void init(PluginContext pluginContext, PluginConfiguration systemSettings) throws PluginException {
		// Make sure an executable is downloaded before invoking the plug-in using multiple threads.
		// This also checks whether the version of the executable matches the java source.

		if (systemSettings != null) {
			calculateQuantities = systemSettings.getBoolean(CALCULATE_QUANTITIES_SETTING, true);
			applyLayerSets = systemSettings.getBoolean(APPLY_LAYER_SETS, true);
			disableOpeningSubtractions = systemSettings.getBoolean(DISABLE_OPENING_SUBTRACTIONS, false);
		}
		
		String buildVersion = DEFAULT_BUILD_VERSION;
		if (systemSettings != null && systemSettings.getString(BUILD_VERSION) != null && !systemSettings.getString(BUILD_VERSION).trim().contentEquals("")) {
			// Overruled by system settings
			buildVersion = systemSettings.getString(BUILD_VERSION);
			LOGGER.info("Using overruled system setting for build version");
		}
		
		try (IfcGeomServerClient test = new IfcGeomServerClient(IfcGeomServerClient.ExecutableSource.S3, buildVersion, pluginContext.getTempDir())) {
			executableFilename = test.getExecutableFilename();
			versionInfo = new VersionInfo(buildVersion, test.getProtocolVersion(), test.getBuildDateTime(), test.getPlatform());
		}
		
		LOGGER.info("Using " + executableFilename);
	}

	@Override
	public ObjectDefinition getUserSettingsDefinition() {
		return null;
	}
	
	@Override
	public ObjectDefinition getSystemSettingsDefinition() {
		ObjectDefinition settings = StoreFactory.eINSTANCE.createObjectDefinition();
		
		PrimitiveDefinition stringType = StoreFactory.eINSTANCE.createPrimitiveDefinition();
		stringType.setType(PrimitiveEnum.STRING);

		PrimitiveDefinition booleanType = StoreFactory.eINSTANCE.createPrimitiveDefinition();
		booleanType.setType(PrimitiveEnum.BOOLEAN);
		
		StringType defaultBuildVersion = StoreFactory.eINSTANCE.createStringType();
		defaultBuildVersion.setValue(DEFAULT_BUILD_VERSION);

		BooleanType defaultTrue = StoreFactory.eINSTANCE.createBooleanType();
		defaultTrue.setValue(true);

		BooleanType defaultFalse = StoreFactory.eINSTANCE.createBooleanType();
		defaultFalse.setValue(false);

		ParameterDefinition buildVersionParameter = StoreFactory.eINSTANCE.createParameterDefinition();
		buildVersionParameter.setIdentifier(BUILD_VERSION);
		buildVersionParameter.setName("Build version");
		buildVersionParameter.setDescription("Build version of IfcOpenShell binary, usually consisting of version number and commit SHA. This overrules the default for the currently installed IfcOpenShell plugin");
		buildVersionParameter.setType(stringType);
		buildVersionParameter.setRequired(false);
		buildVersionParameter.setDefaultValue(defaultBuildVersion);

		ParameterDefinition calculateQuantities = StoreFactory.eINSTANCE.createParameterDefinition();
		calculateQuantities.setIdentifier(CALCULATE_QUANTITIES_SETTING);
		calculateQuantities.setName("Calculate Quantities");
		calculateQuantities.setDescription("Calculates volumes and areas, Takes a bit more time (about 15%)");
		calculateQuantities.setType(booleanType);
		calculateQuantities.setRequired(false);
		calculateQuantities.setDefaultValue(defaultTrue);

		ParameterDefinition applyLayerSets = StoreFactory.eINSTANCE.createParameterDefinition();
		applyLayerSets.setIdentifier(APPLY_LAYER_SETS);
		applyLayerSets.setName("Apply Layer Sets");
		applyLayerSets.setDescription("Splits certain objects into several layers, depending on the model can take about 10x more processing time, and results in more geometry");
		applyLayerSets.setType(booleanType);
		applyLayerSets.setRequired(false);
		applyLayerSets.setDefaultValue(defaultTrue);

		ParameterDefinition disableOpeningSubtractions = StoreFactory.eINSTANCE.createParameterDefinition();
		disableOpeningSubtractions.setIdentifier(DISABLE_OPENING_SUBTRACTIONS);
		disableOpeningSubtractions.setName("Disable Opening Subtractions");
		disableOpeningSubtractions.setDescription("Omits subtractions of voiding opening elements form the voided elements.");
		disableOpeningSubtractions.setType(booleanType);
		disableOpeningSubtractions.setRequired(false);
		disableOpeningSubtractions.setDefaultValue(defaultFalse);
		
		settings.getParameters().add(buildVersionParameter);
		settings.getParameters().add(calculateQuantities);
		settings.getParameters().add(applyLayerSets);
		settings.getParameters().add(disableOpeningSubtractions);
		return settings;
	}
	
	public VersionInfo getVersionInfo() {
		return versionInfo;
	}
}
