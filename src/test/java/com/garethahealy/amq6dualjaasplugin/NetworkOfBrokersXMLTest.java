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

import java.util.concurrent.TimeUnit;

import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.apache.activemq.network.NetworkConnector;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NetworkOfBrokersXMLTest extends BrokerTestSupport {

    @Rule
    public EmbeddedActiveMQBroker brokerNob1 = new EmbeddedActiveMQBroker("xbean:nob-1.xml") {
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

    @Rule
    public EmbeddedActiveMQBroker brokerNob2 = new EmbeddedActiveMQBroker("xbean:nob-2.xml") {
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
    public void canStartNobBrokersFromXML() throws Exception {
        //NOTE: Wait for the NoBs to connect - probably a better way, but its easy, innit!
        TimeUnit.SECONDS.sleep(5);

        assertNotNull(brokerNob1);
        assertNotNull(brokerNob1.getBrokerService());
        assertTrue(brokerNob1.getBrokerService().isStarted());
        assertNotNull(brokerNob1.getBrokerService().getBroker());
        assertNotNull(brokerNob1.getBrokerService().getNetworkConnectors());
        assertTrue(brokerNob1.getBrokerService().getNetworkConnectors().size() == 2);

        assertNotNull(brokerNob2);
        assertNotNull(brokerNob2.getBrokerService());
        assertTrue(brokerNob2.getBrokerService().isStarted());
        assertNotNull(brokerNob2.getBrokerService().getBroker());
        assertNotNull(brokerNob2.getBrokerService().getNetworkConnectors());
        assertTrue(brokerNob2.getBrokerService().getNetworkConnectors().size() == 2);

        for (NetworkConnector current : brokerNob1.getBrokerService().getNetworkConnectors()) {
            assertTrue(current.isStarted());
            assertNotNull(current.activeBridges());
            assertTrue(String.valueOf(current.activeBridges().size()), current.activeBridges().size() == 1);
        }

        for (NetworkConnector current : brokerNob2.getBrokerService().getNetworkConnectors()) {
            assertTrue(current.isStarted());
            assertNotNull(current.activeBridges());
            assertTrue(String.valueOf(current.activeBridges().size()), current.activeBridges().size() == 1);
        }
    }
}
