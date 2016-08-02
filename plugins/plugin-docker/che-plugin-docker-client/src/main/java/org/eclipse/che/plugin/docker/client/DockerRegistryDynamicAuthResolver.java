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

/**
 * Resolves dynamic auth config for docker registries.
 *
 * @author Mykola Morhun
 */
public interface DockerRegistryDynamicAuthResolver {
    /**
     * Retrieves actual auth config for specified registry.
     * Returns null if no credential configured for specified registry.
     *
     * @return actual auth config for specified registry or null if no credentials configured
     */
    AuthConfig getDynamicAuthConfig(String registry);

}
