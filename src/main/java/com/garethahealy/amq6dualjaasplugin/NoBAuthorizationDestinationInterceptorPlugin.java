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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.region.CompositeDestinationInterceptor;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.broker.region.RegionBroker;
import org.apache.activemq.security.AuthorizationBroker;
import org.apache.activemq.security.AuthorizationDestinationInterceptor;

public class NoBAuthorizationDestinationInterceptorPlugin implements BrokerPlugin {

    @Override
    public Broker installPlugin(Broker broker) throws Exception {
        replaceAuthorizationDestinationInterceptor(broker);

        return broker;
    }

    private void replaceAuthorizationDestinationInterceptor(Broker broker) {
        RegionBroker regionBroker = (RegionBroker)broker.getAdaptor(RegionBroker.class);
        if (regionBroker != null) {
            DestinationInterceptor interceptor = regionBroker.getDestinationInterceptor();
            if (interceptor instanceof CompositeDestinationInterceptor) {
                AuthorizationDestinationInterceptor oldInterceptor = null;
                CompositeDestinationInterceptor compositeInterceptor = (CompositeDestinationInterceptor)interceptor;
                for (DestinationInterceptor current : compositeInterceptor.getInterceptors()) {
                    if (current instanceof AuthorizationDestinationInterceptor) {
                        oldInterceptor = (AuthorizationDestinationInterceptor)current;
                        break;
                    }
                }

                if (oldInterceptor != null) {
                    NoBAuthorizationDestinationInterceptor newInterceptor = new NoBAuthorizationDestinationInterceptor(
                        (AuthorizationBroker)broker.getAdaptor(AuthorizationBroker.class));

                    List<DestinationInterceptor> tempList = new ArrayList<>();
                    Collections.replaceAll(tempList, oldInterceptor, newInterceptor);

                    compositeInterceptor.setInterceptors(tempList.toArray(new DestinationInterceptor[tempList.size()]));
                }
            }
        }
    }
}
