/*-
 * #%L
 * GarethHealy :: AMQ6 Dual JAAS Plugin
 * %%
 * Copyright (C) 2013 - 2018 Gareth Healy
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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.jms.TextMessage;
import javax.management.ObjectName;

import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.broker.jmx.DestinationView;
import org.apache.activemq.broker.jmx.ManagedRegionBroker;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.RegionBroker;
import org.apache.activemq.junit.ActiveMQDynamicQueueSenderResource;
import org.apache.activemq.junit.ActiveMQQueueReceiverResource;
import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.apache.activemq.network.NetworkConnector;
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
    public void canSendToOneBrokerAndConsumeForOther() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<Boolean> future = pool.submit(new NobConnectedCallable());
        Boolean isReady = future.get();
        if (!isReady) {
            throw new RuntimeException("NoBs are not ready...");
        }

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

        TextMessage sentMessage = sender1.sendMessage("test");

        assertNotNull(sentMessage);
        assertNotNull(sentMessage.getText());
        assertEquals("test", sentMessage.getText());

        Boolean hasCheckedNob1Count = false;
        ManagedRegionBroker nob1ManagedRegionBroker = (ManagedRegionBroker)brokerNob1.getBrokerService().getRegionBroker().getAdaptor(ManagedRegionBroker.class);
        for (DestinationView current : nob1ManagedRegionBroker.getQueueViews().values()) {
            if (current.getName().equalsIgnoreCase("GROUP1-QUEUE")) {
                assertTrue(current.getEnqueueCount() == 1);
                hasCheckedNob1Count = true;
            }
        }

        assertTrue(hasCheckedNob1Count);

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

        TextMessage receivedMessage = receiver2.receiveTextMessage(TimeUnit.SECONDS.toMillis(10));

        assertNotNull(receivedMessage);
        assertNotNull(receivedMessage.getText());
        assertEquals("test", receivedMessage.getText());
    }

    private class NobConnectedCallable implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {
            long expiration = Math.max(0, 600000L + System.currentTimeMillis());
            Boolean networkConnectsStarted = false;
            while (!networkConnectsStarted && expiration > System.currentTimeMillis()) {

                Boolean bridgeOneActive = false;
                Boolean networkConnectorsOneIsTwo = brokerNob1.getBrokerService().getNetworkConnectors().size() == 2;
                for (NetworkConnector current : brokerNob1.getBrokerService().getNetworkConnectors()) {
                    bridgeOneActive = current.isStarted() && current.activeBridges().size() == 1;
                }

                Boolean bridgeTwoActive = false;
                Boolean networkConnectorsTwoIsTwo = brokerNob2.getBrokerService().getNetworkConnectors().size() == 2;
                for (NetworkConnector current : brokerNob2.getBrokerService().getNetworkConnectors()) {
                    bridgeTwoActive = current.isStarted() && current.activeBridges().size() == 1;
                }

                networkConnectsStarted = networkConnectorsOneIsTwo && networkConnectorsTwoIsTwo
                                         && bridgeOneActive && bridgeTwoActive;
            }

            return networkConnectsStarted;
        }
    }
}
