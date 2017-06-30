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

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.apache.activemq.security.AuthorizationPlugin;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BrokerXMLTest {

    @Rule
    public EmbeddedActiveMQBroker broker = new EmbeddedActiveMQBroker("xbean:activemq.xml") {
        @Override
        public void start() {
            try {
                //NOTE: Default creates an internal client which doesnt support username auth. We skip it here
                this.configure();
                getBrokerService().start();
            } catch (Exception ex) {
                throw new RuntimeException("Exception encountered starting embedded ActiveMQ broker: {}" + this.getBrokerName(), ex);
            }

            getBrokerService().waitUntilStarted();
        }
    };

    @Test
    public void canStartBrokerFromXML() throws Exception {
        assertEquals("testing", broker.getBrokerName());
        assertNotNull(broker.getBrokerService());
        assertTrue(broker.getBrokerService().isStarted());
        assertNotNull(broker.getBrokerService().getBroker());
        assertNotNull(broker.getBrokerService().getPlugins());

        assertTrue(broker.getBrokerService().getPlugins().length == 3);

        for (BrokerPlugin current : broker.getBrokerService().getPlugins()) {

            assertTrue(current instanceof AuthorizationPlugin
                       || current instanceof JaasDualAuthenticationNetworkConnectorPlugin
                       || current instanceof NoBAuthorizationDestinationInterceptorPlugin);
        }

        Broker first = broker.getBrokerService().getBroker();
        Broker next = first.getAdaptor(JaasDualAuthenticationNetworkConnectorBroker.class);

        assertNotNull(next);
        assertTrue(next instanceof JaasDualAuthenticationNetworkConnectorBroker);

        JaasDualAuthenticationNetworkConnectorBroker dualBroker = (JaasDualAuthenticationNetworkConnectorBroker)next;
        assertNotNull(dualBroker.getAuthenticationBroker());
        assertNotNull(dualBroker.getCertificateAuthenticationBroker());
    }
}
