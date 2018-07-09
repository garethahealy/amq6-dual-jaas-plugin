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

import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.security.AuthorizationBroker;
import org.apache.activemq.security.AuthorizationDestinationInterceptor;

public class NoBAuthorizationDestinationInterceptor extends AuthorizationDestinationInterceptor {

    private final AuthorizationBroker broker;

    public NoBAuthorizationDestinationInterceptor(AuthorizationBroker broker) {
        super(broker);

        this.broker = broker;
    }

    @Override
    public Destination intercept(Destination destination) {
        return new NoBAuthorizationDestinationFilter(destination, broker);
    }
}
