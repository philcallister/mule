/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml.schema;

import static org.mule.util.Preconditions.checkArgument;
import static org.mule.util.Preconditions.checkState;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.capability.XmlCapability;
import org.mule.util.Preconditions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class SchemaGenerator
{

    private void validate(Extension extension, XmlCapability xmlCapability)
    {
        checkArgument(extension != null, "extension cannot be null");
        checkArgument(xmlCapability != null, "capability cannot be null");
        checkState(!StringUtils.isBlank(xmlCapability.getNamespace()), "capability can't provide a blank namespace");
    }

    public void generate(Extension extension, XmlCapability xmlCapability) {
        validate(extension, xmlCapability);
        SchemaBuilder schemaBuilder = SchemaBuilder.newSchema(xmlCapability.getNamespace());

        for (ExtensionConfiguration configuration : extension.getConfigurations())
        {
            schemaBuilder.registerConfigElement(module, moduleClass, ctx())

        }

                        .registerProcessorsAndSourcesAndFilters(module)
                        .registerTransformers(module);

            schemaBuilder.registerEnums();
            schemaBuilder.registerSimpleTypes();

            if ( oneModuleFromThatNamespace == null ) {
                throw new IllegalStateException(String.format("A namespace (%s) was found but no module is registered in it.", targetNamespace));
            }

            Module module = oneModuleFromThatNamespace;
            String fileName = "META-INF/mule-" + module.getModuleName() + SchemaConstants.XSD_EXTENSION;

            String versionedLocation = module.getVersionedSchemaLocation();
            String currentLocation = module.getCurrentSchemaLocation();
            String namespaceHandlerName = ctx().<GeneratedClass>getProduct(Product.NAMESPACE_HANDLER, null, targetNamespace).boxify().fullName();
            String className = getModuleClass(module).fullName();

            Schema schema = schemaBuilder.getSchema();
            SchemaLocation versionedSchemaLocation = new SchemaLocation(schema, schema.getTargetNamespace(), fileName, versionedLocation, namespaceHandlerName, className);

            ctx().getSchemaModel().addSchemaLocation(versionedSchemaLocation);

            if (currentLocation != null) {
                SchemaLocation currentSchemaLocation = new SchemaLocation(null, schema.getTargetNamespace(), fileName, currentLocation, namespaceHandlerName, className);
                ctx().getSchemaModel().addSchemaLocation(currentSchemaLocation);
            }
        }
    }

    private Set<String> getTargetNamespaces(List<Module> modules) {
        Set<String> targetNamespaces = new HashSet<String>();
        for (Module module : modules) {
            if ( (module.getKind() == ModuleKind.CONNECTOR || module.getKind() == ModuleKind.GENERIC)
                 && (!targetNamespaces.contains(module.getXmlNamespace()))) {
                targetNamespaces.add(module.getXmlNamespace());
            }
        }
        return targetNamespaces;
    }

    private GeneratedClass getModuleClass(Module module) {
        GeneratedClass moduleClass;
        if (module instanceof ManagedConnectionModule) {
            moduleClass = ctx().getProduct(Product.CONNECTION_MANAGER, module);
        } else {
            moduleClass = ctx().<GeneratedClass>getProduct(Product.CAPABILITIES_ADAPTER, module).topLevelClass();
        }
        return moduleClass;
    }
}
