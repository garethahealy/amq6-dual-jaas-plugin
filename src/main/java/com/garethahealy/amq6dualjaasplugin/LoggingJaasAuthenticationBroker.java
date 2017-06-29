package com.garethahealy.amq6dualjaasplugin;

import java.security.cert.X509Certificate;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.security.JaasAuthenticationBroker;
import org.apache.activemq.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingJaasAuthenticationBroker extends JaasAuthenticationBroker {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingJaasAuthenticationBroker.class);

    public LoggingJaasAuthenticationBroker(Broker next, String jassConfiguration) {
        super(next, jassConfiguration);
    }

    @Override
    public SecurityContext authenticate(String username, String password, X509Certificate[] certificates) throws SecurityException {
        LOG.info("{} - {}", username, password);

        return super.authenticate(username, password, certificates);
    }
}
