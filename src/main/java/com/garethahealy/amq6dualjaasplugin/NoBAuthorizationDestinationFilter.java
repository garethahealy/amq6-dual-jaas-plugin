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

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.security.AuthorizationBroker;
import org.apache.activemq.security.AuthorizationDestinationFilter;

public class NoBAuthorizationDestinationFilter extends AuthorizationDestinationFilter {

    public NoBAuthorizationDestinationFilter(Destination destination, AuthorizationBroker broker) {
        super(destination, broker);
    }

    @Override
    public void addSubscription(ConnectionContext context, Subscription sub) throws Exception {
        try {
            super.addSubscription(context, sub);
        } catch (SecurityException ex) {
            if (isNetworkConnector(context)) {
                next.addSubscription(context, sub);
            } else {
                throw new SecurityException(ex.getMessage(), ex);
            }
        }
    }

    private boolean isNetworkConnector(ConnectionContext context) {
        return context.getConnectionId().getValue().contains("->");
    }
}
