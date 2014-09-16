package org.mule.module.extensions.internal.runtime;

import org.mule.api.MuleEvent;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.runtime.resolver.ValueResolver;

import java.lang.reflect.Method;

public interface ObjectBuilder
{

    ObjectBuilder setPrototypeClass(Class<?> prototypeClass);

    ObjectBuilder addProperty(ExtensionParameter parameter, ValueResolver resolver);

    ObjectBuilder addProperty(Method method, ValueResolver resolver);

    Object build(MuleEvent event) throws Exception;
}
