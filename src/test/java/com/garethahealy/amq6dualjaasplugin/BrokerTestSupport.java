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

import java.util.HashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.activemq.security.StubDualJaasConfiguration;
import org.apache.activemq.security.StubLoginModule;

public abstract class BrokerTestSupport {

    private static final String INSECURE_GROUP = "insecureGroup";
    private static final String INSECURE_USERNAME = "insecureUserName";
    private static final String DN_GROUP = "dnGroup";
    private static final String DN_USERNAME = "dnUserName";

    private static final String JAAS_STUB = "org.apache.activemq.security.StubLoginModule";

    protected static void createLoginConfig() {
        HashMap<String, String> sslConfigOptions = new HashMap<String, String>();
        sslConfigOptions.put(StubLoginModule.ALLOW_LOGIN_PROPERTY, "true");
        sslConfigOptions.put(StubLoginModule.USERS_PROPERTY, DN_USERNAME);
        sslConfigOptions.put(StubLoginModule.GROUPS_PROPERTY, DN_GROUP);
        AppConfigurationEntry sslConfigEntry = new AppConfigurationEntry(JAAS_STUB, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, sslConfigOptions);

        HashMap<String, String> configOptions = new HashMap<String, String>();
        configOptions.put(StubLoginModule.ALLOW_LOGIN_PROPERTY, "true");
        configOptions.put(StubLoginModule.USERS_PROPERTY, INSECURE_USERNAME);
        configOptions.put(StubLoginModule.GROUPS_PROPERTY, INSECURE_GROUP);

        AppConfigurationEntry configEntry = new AppConfigurationEntry(JAAS_STUB, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, configOptions);

        StubDualJaasConfiguration jaasConfig = new StubDualJaasConfiguration(configEntry, sslConfigEntry);

        Configuration.setConfiguration(jaasConfig);
    }
}
