IfcOpenShell-BIMserver-plugin
=============================

IfcOpenShell BIMserver plugin

Occasional problems and solutions
-----------------

### The 3D view does not show anything
If you see `org.bimserver.plugins.renderengine.RenderEngineException: java.io.EOFException` in the geometry generation report, have a look at [BIMserver issue #633](https://github.com/opensourceBIM/BIMserver/issues/633) for a possible reason and [solution](https://github.com/opensourceBIM/BIMserver/issues/633#issuecomment-341644945). Updating gcc and libstdc++ may solve the issue.

### Some products are missing from the 3D view
If you see `java.lang.Exception: Missing objects in model` in the geometry generation report, IfcOpenShell was not able to generate geometry for these products. This may happen when you use an older version of IfcOpenShell. In that case it may help to update IfcOpenShell to a newer version through the "commit sha" plugin setting as described in [BIMserver issue #1207](https://github.com/opensourceBIM/BIMserver/issues/1207#issuecomment-878117652).
