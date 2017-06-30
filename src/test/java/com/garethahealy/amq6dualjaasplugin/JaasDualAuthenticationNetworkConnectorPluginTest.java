/*-
 * #%L
 * GarethHealy :: AMQ6 Dual JAAS Plugin
 * %%
 * Copyright (C) 2013 - 2017 Gareth Healy
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.garethahealy.amq6dualjaasplugin;

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.EmptyBroker;
import org.apache.activemq.security.AuthorizationMap;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JaasDualAuthenticationNetworkConnectorPluginTest {

    @Test
    public void canInstallPlugin() throws Exception {
        BrokerPlugin plugin = new JaasDualAuthenticationNetworkConnectorPlugin();
        plugin.installPlugin(new EmptyBroker());
    }

    @Test
    public void canGetSetSslConfiguration() throws Exception {
        JaasDualAuthenticationNetworkConnectorPlugin plugin = new JaasDualAuthenticationNetworkConnectorPlugin();
        plugin.setJaasCertificateConfiguration("test");

        assertEquals("test", plugin.getJaasCertificateConfiguration());
    }

    @Test
    public void canGetSetJaasConfigurationAuthorizationMap() throws Exception {
        JaasDualAuthenticationNetworkConnectorPlugin plugin = new JaasDualAuthenticationNetworkConnectorPlugin();
        plugin.setJaasConfigurationAuthorizationMap(Mockito.mock(AuthorizationMap.class));

        assertNotNull(plugin.getJaasConfigurationAuthorizationMap());
    }
}
