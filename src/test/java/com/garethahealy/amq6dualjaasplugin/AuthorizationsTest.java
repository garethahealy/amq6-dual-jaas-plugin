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

import javax.jms.JMSSecurityException;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.junit.ActiveMQDynamicQueueSenderResource;
import org.apache.activemq.junit.ActiveMQQueueReceiverResource;
import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AuthorizationsTest extends BrokerTestSupport {

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

    @Rule
    public ExpectedException cannotLoginThrowsJMSSecurityExceptionEE = ExpectedException.none();

    @Rule
    public ExpectedException cannotSendToSomeoneElseQueueEE = ExpectedException.none();

    @BeforeClass
    public static void setUp() throws Exception {
        createRealLoginConfig();
    }

    @Test
    public void cannotLoginThrowsJMSSecurityException() throws Exception {
        cannotLoginThrowsJMSSecurityExceptionEE.expectCause(isA(JMSSecurityException.class));

        ActiveMQSslConnectionFactory sslConnectionFactory = new ActiveMQSslConnectionFactory();
        sslConnectionFactory.setKeyStore(getClass().getClassLoader().getResource("generated-certs/amq-client.ks").getFile());
        sslConnectionFactory.setKeyStorePassword("password");
        sslConnectionFactory.setTrustStore(getClass().getClassLoader().getResource("generated-certs/amq-client.ts").getFile());
        sslConnectionFactory.setTrustStorePassword("password");
        sslConnectionFactory.setBrokerURL("ssl://localhost:61617");
        sslConnectionFactory.setUserName("invalid");
        sslConnectionFactory.setPassword("invalid");

        ActiveMQDynamicQueueSenderResource sender = new ActiveMQDynamicQueueSenderResource("test", sslConnectionFactory);
        sender.start();

        sender.sendMessage("test");
    }

    @Test
    public void cannotSendToSomeoneElseQueue() throws Exception {
        cannotSendToSomeoneElseQueueEE.expectMessage("User user1 is not authorized to write to: queue://GROUP2-QUEUE");
        cannotSendToSomeoneElseQueueEE.expect(JMSSecurityException.class);

        ActiveMQSslConnectionFactory sslConnectionFactory = new ActiveMQSslConnectionFactory();
        sslConnectionFactory.setKeyStore(getClass().getClassLoader().getResource("generated-certs/amq-client.ks").getFile());
        sslConnectionFactory.setKeyStorePassword("password");
        sslConnectionFactory.setTrustStore(getClass().getClassLoader().getResource("generated-certs/amq-client.ts").getFile());
        sslConnectionFactory.setTrustStorePassword("password");
        sslConnectionFactory.setBrokerURL("ssl://localhost:61617");
        sslConnectionFactory.setUserName("user1");
        sslConnectionFactory.setPassword("password");

        ActiveMQDynamicQueueSenderResource sender = new ActiveMQDynamicQueueSenderResource("GROUP2-QUEUE", sslConnectionFactory);
        sender.start();

        sender.sendMessage("test");
    }

    @Test
    public void canProduceAndConsumeMessage() throws Exception {
        ActiveMQSslConnectionFactory sslConnectionFactory = new ActiveMQSslConnectionFactory();
        sslConnectionFactory.setKeyStore(getClass().getClassLoader().getResource("generated-certs/amq-client.ks").getFile());
        sslConnectionFactory.setKeyStorePassword("password");
        sslConnectionFactory.setTrustStore(getClass().getClassLoader().getResource("generated-certs/amq-client.ts").getFile());
        sslConnectionFactory.setTrustStorePassword("password");
        sslConnectionFactory.setBrokerURL("ssl://localhost:61617");
        sslConnectionFactory.setUserName("user1");
        sslConnectionFactory.setPassword("password");

        ActiveMQDynamicQueueSenderResource sender = new ActiveMQDynamicQueueSenderResource("GROUP1-QUEUE", sslConnectionFactory);
        sender.start();

        sender.sendMessage("test");

        ActiveMQQueueReceiverResource receiver = new ActiveMQQueueReceiverResource("GROUP1-QUEUE", sslConnectionFactory);
        receiver.start();

        TextMessage message = receiver.receiveTextMessage(TimeUnit.SECONDS.toMillis(10));

        assertNotNull(message);
        assertNotNull(message.getText());
        assertEquals("test", message.getText());
    }
}
