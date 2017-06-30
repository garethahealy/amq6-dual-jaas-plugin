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

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.Connector;
import org.apache.activemq.broker.EmptyBroker;
import org.apache.activemq.broker.TransportConnectionState;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.security.AuthenticationBroker;
import org.apache.activemq.security.AuthorizationBroker;
import org.apache.activemq.security.AuthorizationMap;
import org.apache.activemq.security.JaasAuthenticationBroker;
import org.apache.activemq.security.JaasCertificateAuthenticationBroker;
import org.apache.activemq.security.SecurityContext;
import org.apache.activemq.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaasDualAuthenticationNetworkConnectorBroker extends BrokerFilter implements AuthenticationBroker {

    private static final Logger LOG = LoggerFactory.getLogger(JaasDualAuthenticationNetworkConnectorBroker.class);

    private final JaasCertificateAuthenticationBroker certificateAuthenticationBroker;
    private final JaasAuthenticationBroker authenticationBroker;

    public JaasDualAuthenticationNetworkConnectorBroker(Broker next, String jaasConfiguration, String jaasCertificateConfiguration,
                                                        AuthorizationMap jaasConfigurationAuthorizationMap) {
        super(next);

        LOG.info("Loading {} - {} / {}", JaasDualAuthenticationNetworkConnectorBroker.class.getCanonicalName(), jaasConfiguration, jaasCertificateConfiguration);

        Broker jaasConfigurationAuthorizationBroker = jaasConfigurationAuthorizationMap == null ? next : new AuthorizationBroker(next, jaasConfigurationAuthorizationMap);

        this.authenticationBroker = new LoggingJaasAuthenticationBroker(jaasConfigurationAuthorizationBroker, jaasConfiguration);
        this.certificateAuthenticationBroker = new JaasCertificateAuthenticationBroker(next, jaasCertificateConfiguration);
    }

    public JaasCertificateAuthenticationBroker getCertificateAuthenticationBroker() {
        return certificateAuthenticationBroker;
    }

    public JaasAuthenticationBroker getAuthenticationBroker() {
        return authenticationBroker;
    }

    @Override
    public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {
        LOG.info("addConnection; {}", context.getClientId());

        if (isSSLConnector(context, info)) {
            if (isNetworkConnector(context)) {
                this.certificateAuthenticationBroker.addConnection(context, info);
            } else {
                this.authenticationBroker.addConnection(context, info);
            }
        }
    }

    @Override
    public void removeConnection(ConnectionContext context, ConnectionInfo info, Throwable error) throws Exception {
        LOG.info("removeConnection; {}", context.getClientId());

        if (isSSLConnector(context, info)) {
            if (isNetworkConnector(context)) {
                this.certificateAuthenticationBroker.removeConnection(context, info, error);
            } else {
                this.authenticationBroker.removeConnection(context, info, error);
            }
        }
    }

    @Override
    public void removeDestination(ConnectionContext context, ActiveMQDestination destination, long timeout) throws Exception {
        if (isNetworkConnector(context)) {
            this.certificateAuthenticationBroker.removeDestination(context, destination, timeout);
        } else {
            this.authenticationBroker.removeDestination(context, destination, timeout);
        }
    }

    @Override
    public SecurityContext authenticate(String username, String password, X509Certificate[] peerCertificates) throws SecurityException {
        LOG.info("-> authenticate; {}", username);

        if (username == null || username.trim().length() <= 0) {
            return this.certificateAuthenticationBroker.authenticate(username, password, peerCertificates);
        } else {
            return this.authenticationBroker.authenticate(username, password, peerCertificates);
        }
    }

    private boolean isSSLConnector(ConnectionContext context, ConnectionInfo info) throws Exception {
        boolean sslCapable = false;
        Connector connector = context.getConnector();
        if (connector != null && connector instanceof TransportConnector) {
            TransportConnector transportConnector = (TransportConnector)connector;
            sslCapable = transportConnector.getServer().isSslServer();
        }

        // AMQ-5943, also check if transport context carries X509 cert
        if (!sslCapable && info.getTransportContext() instanceof X509Certificate[]) {
            sslCapable = true;
        }

        LOG.info("-> isSSL; {}", sslCapable);
        return sslCapable;
    }

    private boolean isNetworkConnector(ConnectionContext context) throws Exception {
        boolean isNetworkConnection = false;

        ConnectionState connectionState = context.getConnectionState();
        if (connectionState != null && connectionState instanceof TransportConnectionState) {
            TransportConnectionState transportConnectionState = (TransportConnectionState)connectionState;
            isNetworkConnection = transportConnectionState.getConnection().isNetworkConnection();
        }

        if (!isNetworkConnection && context.getConnectionId() != null) {
            isNetworkConnection = context.getConnectionId().getValue().contains("->");
        }

        LOG.info("-> isNetworkConnector; {}", isNetworkConnection);

        return isNetworkConnection;
    }
}
