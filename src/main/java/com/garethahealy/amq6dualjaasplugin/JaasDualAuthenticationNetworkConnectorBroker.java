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

import java.security.cert.X509Certificate;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.Connector;
import org.apache.activemq.broker.EmptyBroker;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.TransactionBroker;
import org.apache.activemq.broker.TransportConnectionState;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.security.AuthenticationBroker;
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

    public JaasDualAuthenticationNetworkConnectorBroker(Broker next, String jaasConfiguration, String jaasCertificateConfiguration) {
        super(next);

        LOG.info("Loading {} - {} / {}", JaasDualAuthenticationNetworkConnectorBroker.class.getCanonicalName(), jaasConfiguration, jaasCertificateConfiguration);

        this.authenticationBroker = new JaasAuthenticationBroker(next, jaasConfiguration);
        this.certificateAuthenticationBroker = new JaasCertificateAuthenticationBroker(new EmptyBroker(), jaasCertificateConfiguration);
    }

    public JaasCertificateAuthenticationBroker getCertificateAuthenticationBroker() {
        return certificateAuthenticationBroker;
    }

    public JaasAuthenticationBroker getAuthenticationBroker() {
        return authenticationBroker;
    }

    @Override
    public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {
        LOG.debug("addConnection; {}", context.getClientId());

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
        LOG.debug("removeConnection; {}", context.getClientId());

        if (isSSLConnector(context, info)) {
            if (isNetworkConnector(context)) {
                this.certificateAuthenticationBroker.removeConnection(context, info, error);
            } else {
                this.authenticationBroker.removeConnection(context, info, error);
            }
        }
    }

    @Override
    public SecurityContext authenticate(String username, String password, X509Certificate[] peerCertificates) throws SecurityException {
        LOG.debug("-> authenticate; {}", username);

        if (username == null || username.trim().length() <= 0) {
            return this.certificateAuthenticationBroker.authenticate(username, password, peerCertificates);
        } else {
            return this.authenticationBroker.authenticate(username, password, peerCertificates);
        }
    }

    @Override
    public Destination addDestination(ConnectionContext context, ActiveMQDestination destination, boolean create) throws Exception {
        LOG.debug("-> addDestination; {}", destination.getPhysicalName());

        if (isNetworkConnector(context)) {
            Broker transactionBroker = getAdaptor(TransactionBroker.class);
            return transactionBroker.addDestination(context, destination, create);
        } else {
            return super.addDestination(context, destination, create);
        }
    }

    @Override
    public void removeDestination(ConnectionContext context, ActiveMQDestination destination, long timeout) throws Exception {
        LOG.debug("-> removeDestination; {}", context.getClientId());

        if (isNetworkConnector(context)) {
            this.certificateAuthenticationBroker.removeDestination(context, destination, timeout);
        } else {
            this.authenticationBroker.removeDestination(context, destination, timeout);
        }
    }

    @Override
    public Subscription addConsumer(ConnectionContext context, ConsumerInfo info) throws Exception {
        LOG.debug("addConsumer");

        if (isNetworkConnector(context)) {
            Broker transactionBroker = getAdaptor(TransactionBroker.class);
            return transactionBroker.addConsumer(context, info);
        } else {
            return super.addConsumer(context, info);
        }
    }

    @Override
    public void send(ProducerBrokerExchange producerExchange, Message messageSend) throws Exception {
        LOG.debug("-> send; {}", messageSend.getMessageId());

        if (isNetworkConnector(producerExchange.getConnectionContext())) {
            Broker transactionBroker = getAdaptor(TransactionBroker.class);
            transactionBroker.send(producerExchange, messageSend);
        } else {
            super.send(producerExchange, messageSend);
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

        LOG.debug("---> isSSL; {}", sslCapable);
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

        LOG.debug("---> isNetworkConnector; {}", isNetworkConnection);

        return isNetworkConnection;
    }
}
