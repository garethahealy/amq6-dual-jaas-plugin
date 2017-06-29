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

import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.EmptyBroker;
import org.apache.activemq.broker.TransportConnection;
import org.apache.activemq.broker.TransportConnectionState;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.command.ConnectionId;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.security.AuthenticationBroker;
import org.apache.activemq.transport.TransportServer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;

public class JaasDualAuthenticationNetworkConnectorBrokerTest extends BrokerTestSupport {

    private static final String JAAS_NON_SSL = "activemq-domain";
    private static final String JAAS_SSL = "activemq-cert";

    @BeforeClass
    public static void setUp() throws Exception {
        createLoginConfig();
    }

    @Test
    public void canCreate() {
        Broker broker = new JaasDualAuthenticationNetworkConnectorBroker(new EmptyBroker(), JAAS_NON_SSL, JAAS_SSL);

        assertNotNull(broker);
    }

    @Test
    public void canAddConnection() throws Exception {
        Broker broker = new JaasDualAuthenticationNetworkConnectorBroker(new EmptyBroker(), JAAS_NON_SSL, JAAS_SSL);

        assertNotNull(broker);

        broker.addConnection(Mockito.mock(ConnectionContext.class), Mockito.mock(ConnectionInfo.class));
    }

    @Test
    public void canAddConnectionForSSL() throws Exception {
        Broker broker = new JaasDualAuthenticationNetworkConnectorBroker(new EmptyBroker(), JAAS_NON_SSL, JAAS_SSL);

        assertNotNull(broker);

        ConnectionContext mockedConnectionContext = Mockito.mock(ConnectionContext.class);
        Mockito.when(mockedConnectionContext.getConnectionId()).thenReturn(new ConnectionId("brokera->brokerb"));

        ConnectionInfo mockedConnectionInfo = Mockito.mock(ConnectionInfo.class);
        Mockito.when(mockedConnectionInfo.getTransportContext()).thenReturn(new X509Certificate[0]);

        broker.addConnection(mockedConnectionContext, mockedConnectionInfo);
    }

    @Test
    public void canAddConnectionForSSLWhenTransportConnectorIsSSL() throws Exception {
        Broker broker = new JaasDualAuthenticationNetworkConnectorBroker(new EmptyBroker(), JAAS_NON_SSL, JAAS_SSL);

        assertNotNull(broker);

        TransportServer mockedTransportServer = Mockito.mock(TransportServer.class);
        Mockito.when(mockedTransportServer.isSslServer()).thenReturn(true);

        TransportConnector mockedTransportConnector = Mockito.mock(TransportConnector.class);
        Mockito.when(mockedTransportConnector.getServer()).thenReturn(mockedTransportServer);

        TransportConnection mockedTransportConnection = Mockito.mock(TransportConnection.class);
        Mockito.when(mockedTransportConnection.isNetworkConnection()).thenReturn(true);

        TransportConnectionState mockedTransportConnectionState = Mockito.mock(TransportConnectionState.class);
        Mockito.when(mockedTransportConnectionState.getConnection()).thenReturn(mockedTransportConnection);

        ConnectionContext mockedConnectionContext = Mockito.mock(ConnectionContext.class);
        Mockito.when(mockedConnectionContext.getConnectionId()).thenReturn(new ConnectionId("brokera->brokerb"));
        Mockito.when(mockedConnectionContext.getConnector()).thenReturn(mockedTransportConnector);
        Mockito.when(mockedConnectionContext.getConnectionState()).thenReturn(mockedTransportConnectionState);

        ConnectionInfo mockedConnectionInfo = Mockito.mock(ConnectionInfo.class);
        Mockito.when(mockedConnectionInfo.getTransportContext()).thenReturn(new X509Certificate[0]);

        broker.addConnection(mockedConnectionContext, mockedConnectionInfo);
    }

    @Test
    public void canAddConnectionForProperties() throws Exception {
        Broker broker = new JaasDualAuthenticationNetworkConnectorBroker(new EmptyBroker(), JAAS_NON_SSL, JAAS_SSL);

        assertNotNull(broker);

        ConnectionInfo mockedConnectionInfo = Mockito.mock(ConnectionInfo.class);
        Mockito.when(mockedConnectionInfo.getTransportContext()).thenReturn(new X509Certificate[0]);

        broker.addConnection(Mockito.mock(ConnectionContext.class), mockedConnectionInfo);
    }

    @Test
    public void canRemoveConnection() throws Exception {
        Broker broker = new JaasDualAuthenticationNetworkConnectorBroker(new EmptyBroker(), JAAS_NON_SSL, JAAS_SSL);

        assertNotNull(broker);

        broker.removeConnection(Mockito.mock(ConnectionContext.class), Mockito.mock(ConnectionInfo.class), null);
    }

    @Test
    public void canRemoveConnectionForSSL() throws Exception {
        Broker broker = new JaasDualAuthenticationNetworkConnectorBroker(new EmptyBroker(), JAAS_NON_SSL, JAAS_SSL);

        assertNotNull(broker);

        ConnectionContext mockedConnectionContext = Mockito.mock(ConnectionContext.class);
        Mockito.when(mockedConnectionContext.getConnectionId()).thenReturn(new ConnectionId("brokera->brokerb"));

        ConnectionInfo mockedConnectionInfo = Mockito.mock(ConnectionInfo.class);
        Mockito.when(mockedConnectionInfo.getTransportContext()).thenReturn(new X509Certificate[0]);

        broker.removeConnection(mockedConnectionContext, mockedConnectionInfo, null);
    }

    @Test
    public void canRemoveConnectionForProperties() throws Exception {
        Broker broker = new JaasDualAuthenticationNetworkConnectorBroker(new EmptyBroker(), JAAS_NON_SSL, JAAS_SSL);

        assertNotNull(broker);

        ConnectionInfo mockedConnectionInfo = Mockito.mock(ConnectionInfo.class);
        Mockito.when(mockedConnectionInfo.getTransportContext()).thenReturn(new X509Certificate[0]);

        broker.removeConnection(Mockito.mock(ConnectionContext.class), mockedConnectionInfo, null);
    }

    @Test
    public void canAuthenticateForSSL() {
        AuthenticationBroker broker = new JaasDualAuthenticationNetworkConnectorBroker(new EmptyBroker(), JAAS_NON_SSL, JAAS_SSL);

        assertNotNull(broker);

        broker.authenticate(null, null, new ArrayList<X509Certificate>().toArray(new X509Certificate[0]));
    }

    @Test
    public void canAuthenticateForSSLWithEmptyUsername() {
        AuthenticationBroker broker = new JaasDualAuthenticationNetworkConnectorBroker(new EmptyBroker(), JAAS_NON_SSL, JAAS_SSL);

        assertNotNull(broker);

        broker.authenticate(" ", null, new ArrayList<X509Certificate>().toArray(new X509Certificate[0]));
    }

    @Test
    public void canAuthenticateForProperties() {
        AuthenticationBroker broker = new JaasDualAuthenticationNetworkConnectorBroker(new EmptyBroker(), JAAS_NON_SSL, JAAS_SSL);

        assertNotNull(broker);

        broker.authenticate("admin", "admin", new ArrayList<X509Certificate>().toArray(new X509Certificate[0]));
    }
}
