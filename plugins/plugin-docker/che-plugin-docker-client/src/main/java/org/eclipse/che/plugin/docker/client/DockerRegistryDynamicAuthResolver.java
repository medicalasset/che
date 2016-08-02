/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client;

import org.eclipse.che.plugin.docker.client.dto.AuthConfig;

import java.util.Map;

/**
 * Resolves dynamic auth config for docker registries.
 *
 * @author Mykola Morhun
 */
public interface DockerRegistryDynamicAuthResolver {
    /**
     * Retrieves actual auth data for specified registry.
     * Returns null if no credential configured for specified registry.
     *
     * @return actual auth data for specified registry or null if no credentials configured
     */
    AuthConfig getDynamicXRegistryAuth(String registry);

    /**
     * Retrieves all actual auth configs for all registries with dynamic auth credentials.
     * Returns null if no registries with dynamic auth credentials configured.
     *
     * @return all dynamic auth configs or null if no credentials configured
     */
    Map<String, AuthConfig> getDynamicXRegistryConfig();

}
