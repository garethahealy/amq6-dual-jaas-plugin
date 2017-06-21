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
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.Connector;
import org.apache.activemq.broker.EmptyBroker;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.network.NetworkConnector;
import org.apache.activemq.security.JaasAuthenticationBroker;
import org.apache.activemq.security.JaasCertificateAuthenticationBroker;
import org.apache.activemq.security.JaasDualAuthenticationBroker;
import org.apache.activemq.security.SecurityContext;

public class JaasDualAuthenticationNetworkConnectorBroker extends JaasDualAuthenticationBroker {

    private final JaasCertificateAuthenticationBroker certificateAuthenticationBroker;
    private final JaasAuthenticationBroker authenticationBroker;

    public JaasDualAuthenticationNetworkConnectorBroker(Broker next, String jaasConfiguration, String jaasSslConfiguration) {
        super(next, jaasConfiguration, jaasSslConfiguration);

        this.authenticationBroker = new JaasAuthenticationBroker(new EmptyBroker(), jaasConfiguration);
        this.certificateAuthenticationBroker = new JaasCertificateAuthenticationBroker(new EmptyBroker(), jaasSslConfiguration);
    }

    @Override
    public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {
        if (isSSL(context, info)) {
            if (isNetworkConnector(context)) {
                this.certificateAuthenticationBroker.addConnection(context, info);
            } else {
                this.authenticationBroker.addConnection(context, info);
            }
        }
    }

    @Override
    public void removeConnection(ConnectionContext context, ConnectionInfo info, Throwable error) throws Exception {
        if (isSSL(context, info)) {
            if (isNetworkConnector(context)) {
                this.certificateAuthenticationBroker.removeConnection(context, info, error);
            } else {
                this.authenticationBroker.removeConnection(context, info, error);
            }
        }
    }

    @Override
    public SecurityContext authenticate(String username, String password, X509Certificate[] peerCertificates) throws SecurityException {
        if (username == null || username.trim().length() <= 0) {
            return this.certificateAuthenticationBroker.authenticate(username, password, peerCertificates);
        } else {
            return this.authenticationBroker.authenticate(username, password, peerCertificates);
        }
    }

    private boolean isSSL(ConnectionContext context, ConnectionInfo info) throws Exception {
        boolean sslCapable = false;
        Connector connector = context.getConnector();
        if (connector instanceof TransportConnector) {
            TransportConnector transportConnector = (TransportConnector)connector;
            sslCapable = transportConnector.getServer().isSslServer();
        }

        // AMQ-5943, also check if transport context carries X509 cert
        if (!sslCapable && info.getTransportContext() instanceof X509Certificate[]) {
            sslCapable = true;
        }

        return sslCapable;
    }

    private boolean isNetworkConnector(ConnectionContext context) throws Exception {
        Connector connector = context.getConnector();

        return connector instanceof NetworkConnector;
    }
}
