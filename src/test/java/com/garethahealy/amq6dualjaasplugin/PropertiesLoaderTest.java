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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.activemq.jaas.PropertiesLoader;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PropertiesLoaderTest extends PropertiesLoader {

    @Test
    public void canGetUsers() {
        URL url = PropertiesLoaderTest.class.getResource("/users.properties");
        String root = FilenameUtils.getPath(url.getPath());
        Map<String, String> options = new HashMap<String, String>();
        options.put("baseDir", "/" + root);

        Properties users = load("users.properties", "users.properties", options).getProps();

        assertNotNull(users);
        assertEquals(3, users.size());
        assertEquals("CN=broker-amqtest-amq-tcp-ssl, O=GarethHealy, C=UK", users.get("networkconnector"));
    }
}
