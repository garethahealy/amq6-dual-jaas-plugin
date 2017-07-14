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
        LOG.debug("authenticate: {} / {}", username, password);

        return super.authenticate(username, password, certificates);
    }
}
