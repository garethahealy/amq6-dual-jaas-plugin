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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.TransportConnection;
import org.apache.activemq.broker.TransportConnectionState;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.command.ConnectionId;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.jaas.PropertiesLoginModule;
import org.apache.activemq.jaas.TextFileCertificateLoginModule;
import org.apache.activemq.security.StubDualJaasConfiguration;
import org.apache.activemq.security.StubLoginModule;
import org.apache.activemq.transport.TransportServer;
import org.apache.commons.io.FilenameUtils;
import org.mockito.Mockito;

public abstract class BrokerTestSupport {

    private static final String INSECURE_GROUP = "insecureGroup";
    private static final String INSECURE_USERNAME = "insecureUserName";
    private static final String DN_GROUP = "dnGroup";
    private static final String DN_USERNAME = "dnUserName";

    private static final String JAAS_STUB = "org.apache.activemq.security.StubLoginModule";

    protected static void createStubbedLoginConfig() {
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

    protected static void createRealLoginConfig() {
        URL url = PropertiesLoaderTest.class.getResource("/users.properties");
        String root = FilenameUtils.getPath(url.getPath());

        HashMap<String, String> sslConfigOptions = new HashMap<String, String>();
        sslConfigOptions.put("debug", "true");
        sslConfigOptions.put("baseDir", "/" + root);
        sslConfigOptions.put("org.apache.activemq.jaas.textfiledn.user", "users.properties");
        sslConfigOptions.put("org.apache.activemq.jaas.textfiledn.group", "groups.properties");

        HashMap<String, String> configOptions = new HashMap<String, String>();
        configOptions.put("baseDir", "/" + root);
        configOptions.put("debug", "true");
        configOptions.put("org.apache.activemq.jaas.properties.user", "users.properties");
        configOptions.put("org.apache.activemq.jaas.properties.group", "groups.properties");

        AppConfigurationEntry sslConfigEntry = new AppConfigurationEntry(TextFileCertificateLoginModule.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT, sslConfigOptions);
        AppConfigurationEntry configEntry = new AppConfigurationEntry(PropertiesLoginModule.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT, configOptions);

        StubDualJaasConfiguration jaasConfig = new StubDualJaasConfiguration(configEntry, sslConfigEntry);

        Configuration.setConfiguration(jaasConfig);
    }

    protected ConnectionContext getMockedConnectionContext() {
        ConnectionContext mockedConnectionContext = Mockito.mock(ConnectionContext.class);
        Mockito.when(mockedConnectionContext.getConnectionId()).thenReturn(new ConnectionId("brokera->brokerb"));

        return mockedConnectionContext;
    }

    protected ConnectionContext getMockedConnectionContextWithSSL() throws IOException, URISyntaxException {
        TransportServer mockedTransportServer = Mockito.mock(TransportServer.class);
        Mockito.when(mockedTransportServer.isSslServer()).thenReturn(true);

        TransportConnector mockedTransportConnector = Mockito.mock(TransportConnector.class);
        Mockito.when(mockedTransportConnector.getServer()).thenReturn(mockedTransportServer);

        TransportConnection mockedTransportConnection = Mockito.mock(TransportConnection.class);
        Mockito.when(mockedTransportConnection.isNetworkConnection()).thenReturn(true);

        TransportConnectionState mockedTransportConnectionState = Mockito.mock(TransportConnectionState.class);
        Mockito.when(mockedTransportConnectionState.getConnection()).thenReturn(mockedTransportConnection);

        ConnectionContext mockedConnectionContext = getMockedConnectionContext();
        Mockito.when(mockedConnectionContext.getConnector()).thenReturn(mockedTransportConnector);
        Mockito.when(mockedConnectionContext.getConnectionState()).thenReturn(mockedTransportConnectionState);

        return mockedConnectionContext;
    }

    protected ConnectionInfo getMockedConnectionInfo() {
        ConnectionInfo mockedConnectionInfo = Mockito.mock(ConnectionInfo.class);
        Mockito.when(mockedConnectionInfo.getTransportContext()).thenReturn(new X509Certificate[0]);

        return mockedConnectionInfo;
    }
}
