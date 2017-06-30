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

import org.apache.activemq.broker.EmptyBroker;
import org.apache.activemq.security.AuthenticationBroker;
import org.apache.activemq.security.SecurityContext;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class LoggingJaasAuthenticationBrokerTest extends BrokerTestSupport {

    private static final String JAAS_NON_SSL = "activemq-domain";

    @BeforeClass
    public static void setUp() throws Exception {
        createStubbedLoginConfig();
    }

    @Test
    public void canAuthenticate() {
        AuthenticationBroker broker = new LoggingJaasAuthenticationBroker(new EmptyBroker(), JAAS_NON_SSL);
        SecurityContext securityContext = broker.authenticate("admin", "admin", new ArrayList<X509Certificate>().toArray(new X509Certificate[0]));

        assertNotNull(broker);
        assertNotNull(securityContext);
    }
}
