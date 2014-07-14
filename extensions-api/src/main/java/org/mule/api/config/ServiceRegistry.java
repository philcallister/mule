/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.api.config;

import java.util.Iterator;

/**
 * API component to be used as standard SPI discovery mechanism.
 * Locates and returns all registered instances of a given provider
 * class.
 * <p/>
 * Notice that in future versions of Mule this interface might me moved to
 * a new higher level mule-api artifact
 *
 * @since 1.0
 */
//TODO: Move this to mule-api project when created
public interface ServiceRegistry
{

    /**
     * Searches for implementations of a particular service class
     * using the given class loader.
     * <p/>
     * <p> This method transforms the name of the given service class
     * into a provider-configuration filename as described in the
     * class comment and then uses the <code>getResources</code>
     * method of the given class loader to find all available files
     * with that name.  These files are then read and parsed to
     * produce a list of provider-class names.  The iterator that is
     * returned uses the given class loader to look up and then
     * instantiate each element of the list.
     * <p/>
     * <p> Because it is possible for extensions to be installed into
     * a running Java virtual machine, this method may return
     * different results each time it is invoked.
     *
     * @param providerClass a <code>Class</code>object indicating the
     *                      class or interface of the service providers being detected.
     * @param loader        the class loader to be used to load
     *                      provider-configuration files and instantiate provider classes,
     *                      or <code>null</code> if the system class loader (or, failing that
     *                      the bootstrap class loader) is to be used.
     * @return An <code>Iterator</code> that yields provider objects
     * for the given service, in some arbitrary order.  The iterator
     * will throw an <code>Error</code> if a provider-configuration
     * file violates the specified format or if a provider class
     * cannot be found and instantiated.
     * @throws IllegalArgumentException if
     *                                  <code>providerClass</code> is <code>null</code>.
     */
    <T> Iterator<T> lookupProviders(Class<T> providerClass, ClassLoader loader);

    /**
     * Locates and incrementally instantiates the available providers
     * of a given service using the context class loader.  This
     * convenience method is equivalent to:
     * <p/>
     * <pre>
     *   ClassLoader cl = Thread.currentThread().getContextClassLoader();
     *   return Service.providers(service, cl);
     * </pre>
     *
     * @param providerClass a <code>Class</code>object indicating the
     *                      class or interface of the service providers being detected.
     * @return An <code>Iterator</code> that yields provider objects
     * for the given service, in some arbitrary order.  The iterator
     * will throw an <code>Error</code> if a provider-configuration
     * file violates the specified format or if a provider class
     * cannot be found and instantiated.
     * @throws IllegalArgumentException if
     *                                  <code>providerClass</code> is <code>null</code>.
     */
    <T> Iterator<T> lookupProviders(Class<T> providerClass);

}
