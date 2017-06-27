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
import java.util.HashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.EmptyBroker;
import org.apache.activemq.command.ConnectionId;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.security.AuthenticationBroker;
import org.apache.activemq.security.StubDualJaasConfiguration;
import org.apache.activemq.security.StubLoginModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;

public class JaasDualAuthenticationNetworkConnectorBrokerTest {

    private static final String INSECURE_GROUP = "insecureGroup";
    private static final String INSECURE_USERNAME = "insecureUserName";
    private static final String DN_GROUP = "dnGroup";
    private static final String DN_USERNAME = "dnUserName";

    private static final String JAAS_STUB = "org.apache.activemq.security.StubLoginModule";
    private static final String JAAS_NON_SSL = "activemq-domain";
    private static final String JAAS_SSL = "activemq-cert";

    static void createLoginConfig() {
        HashMap<String, String> sslConfigOptions = new HashMap<String, String>();
        sslConfigOptions.put(StubLoginModule.ALLOW_LOGIN_PROPERTY, "true");
        sslConfigOptions.put(StubLoginModule.USERS_PROPERTY, DN_USERNAME);
        sslConfigOptions.put(StubLoginModule.GROUPS_PROPERTY, DN_GROUP);
        AppConfigurationEntry sslConfigEntry = new AppConfigurationEntry(JAAS_STUB, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, sslConfigOptions);

        HashMap<String, String> configOptions = new HashMap<String, String>();
        configOptions.put(StubLoginModule.ALLOW_LOGIN_PROPERTY, "true");
        configOptions.put(StubLoginModule.USERS_PROPERTY, INSECURE_USERNAME);
        configOptions.put(StubLoginModule.GROUPS_PROPERTY, INSECURE_GROUP);

        AppConfigurationEntry configEntry = new AppConfigurationEntry(JAAS_STUB, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, configOptions);

        StubDualJaasConfiguration jaasConfig = new StubDualJaasConfiguration(configEntry, sslConfigEntry);

        Configuration.setConfiguration(jaasConfig);
    }

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
