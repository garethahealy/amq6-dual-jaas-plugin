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

import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.junit.ActiveMQDynamicQueueSenderResource;
import org.apache.activemq.junit.ActiveMQQueueReceiverResource;
import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.apache.activemq.network.NetworkBridge;
import org.apache.activemq.network.NetworkConnector;
import org.apache.activemq.security.AuthorizationBroker;
import org.apache.activemq.security.AuthorizationDestinationFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NetworkOfBrokersTest extends BrokerTestSupport {

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

    @BeforeClass
    public static void setUp() throws Exception {
        createRealLoginConfig();
    }

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

    @Test
    public void canSendToOneBrokerAndConsumeForOther() throws Exception {
        //NOTE: Wait for the NoBs to connect - probably a better way, but its easy, innit!
        TimeUnit.SECONDS.sleep(5);

        ActiveMQSslConnectionFactory nob1ConnectionFactory = new ActiveMQSslConnectionFactory();
        nob1ConnectionFactory.setKeyStore(getClass().getClassLoader().getResource("generated-certs/amq-client.ks").getFile());
        nob1ConnectionFactory.setKeyStorePassword("password");
        nob1ConnectionFactory.setTrustStore(getClass().getClassLoader().getResource("generated-certs/amq-client.ts").getFile());
        nob1ConnectionFactory.setTrustStorePassword("password");
        nob1ConnectionFactory.setBrokerURL("ssl://localhost:61618");
        nob1ConnectionFactory.setUserName("user1");
        nob1ConnectionFactory.setPassword("password");

        ActiveMQDynamicQueueSenderResource sender1 = new ActiveMQDynamicQueueSenderResource("GROUP1-QUEUE", nob1ConnectionFactory);
        sender1.start();

        sender1.sendMessage("test");

        TimeUnit.SECONDS.sleep(5);
        
        ActiveMQSslConnectionFactory nob2ConnectionFactory = new ActiveMQSslConnectionFactory();
        nob2ConnectionFactory.setKeyStore(getClass().getClassLoader().getResource("generated-certs/amq-client.ks").getFile());
        nob2ConnectionFactory.setKeyStorePassword("password");
        nob2ConnectionFactory.setTrustStore(getClass().getClassLoader().getResource("generated-certs/amq-client.ts").getFile());
        nob2ConnectionFactory.setTrustStorePassword("password");
        nob2ConnectionFactory.setBrokerURL("ssl://localhost:61619");
        nob2ConnectionFactory.setUserName("user1");
        nob2ConnectionFactory.setPassword("password");

        ActiveMQQueueReceiverResource receiver2 = new ActiveMQQueueReceiverResource("GROUP1-QUEUE", nob2ConnectionFactory);
        receiver2.start();

        TimeUnit.SECONDS.sleep(5);

        TextMessage message = receiver2.receiveTextMessage();

        //AuthorizationDestinationFilter ss;
        AuthorizationBroker ss;
        assertNotNull(message);
        assertNotNull(message.getText());
        assertEquals("test", message.getText());
    }
}
