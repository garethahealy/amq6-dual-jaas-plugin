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

import java.util.Arrays;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.filter.DefaultDestinationMapEntry;
import org.apache.activemq.filter.DestinationMapEntry;
import org.apache.activemq.security.AuthorizationEntry;
import org.apache.activemq.security.AuthorizationMap;
import org.apache.activemq.security.DefaultAuthorizationMap;
import org.apache.activemq.security.JaasAuthenticationPlugin;
import org.apache.activemq.security.SimpleAuthorizationMap;
import org.apache.activemq.security.TempDestinationAuthorizationEntry;

public class JaasDualAuthenticationNetworkConnectorPlugin extends JaasAuthenticationPlugin {

    private String jaasCertificateConfiguration = "activemq-cert";
    private AuthorizationMap jaasConfigurationAuthorizationMap;

    @Override
    public Broker installPlugin(Broker broker) {
        initialiseJaas();

        return new JaasDualAuthenticationNetworkConnectorBroker(broker, configuration, jaasCertificateConfiguration, jaasConfigurationAuthorizationMap);
    }

    public AuthorizationMap defaultAuthorizationMap() {
        TempDestinationAuthorizationEntry tempDestination = new TempDestinationAuthorizationEntry();
        AuthorizationEntry authorizationEntry = new AuthorizationEntry();
        try {
            tempDestination.setAdmin("admin");
            authorizationEntry.setAdmin("admin");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //queue names
        DefaultDestinationMapEntry devQueue = new DefaultDestinationMapEntry();
        devQueue.setQueue("DEV.>");
        devQueue.setValue(authorizationEntry);

        DefaultAuthorizationMap adminACLs = new DefaultAuthorizationMap(Arrays.asList((DestinationMapEntry)devQueue));
        DefaultAuthorizationMap readACLs = new DefaultAuthorizationMap(Arrays.asList((DestinationMapEntry)devQueue));
        DefaultAuthorizationMap writeACLs = new DefaultAuthorizationMap(Arrays.asList((DestinationMapEntry)devQueue));

        SimpleAuthorizationMap simpleAuthorizationMap = new SimpleAuthorizationMap();
        simpleAuthorizationMap.setAdminACLs(adminACLs);
        simpleAuthorizationMap.setReadACLs(readACLs);
        simpleAuthorizationMap.setWriteACLs(writeACLs);
        simpleAuthorizationMap.setTempDestinationAuthorizationEntry(tempDestination);

        return simpleAuthorizationMap;
    }

    public String getJaasCertificateConfiguration() {
        return jaasCertificateConfiguration;
    }

    public void setJaasCertificateConfiguration(String jaasCertificateConfiguration) {
        this.jaasCertificateConfiguration = jaasCertificateConfiguration;
    }

    public AuthorizationMap getJaasConfigurationAuthorizationMap() {
        return jaasConfigurationAuthorizationMap;
    }

    public void setJaasConfigurationAuthorizationMap(AuthorizationMap jaasConfigurationAuthorizationMap) {
        this.jaasConfigurationAuthorizationMap = jaasConfigurationAuthorizationMap;
    }
}
