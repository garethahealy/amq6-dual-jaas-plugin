[![Build Status](https://travis-ci.org/garethahealy/amq6-dual-jaas-plugin.svg?branch=master)](https://travis-ci.org/garethahealy/amq6-dual-jaas-plugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.garethahealy.amq6-dual-jaas-plugin%3Aamq6-dual-jaas-plugin-parent&metric=coverage)](https://sonarcloud.io/dashboard?id=com.garethahealy.amq6-dual-jaas-plugin%3Aamq6-dual-jaas-plugin-parent)
[![Release Version](https://img.shields.io/maven-central/v/com.garethahealy.amq6-dual-jaas-plugin/amq6-dual-jaas-plugin-parent.svg?maxAge=2592000)](https://mvnrepository.com/artifact/com.garethahealy.amq6-dual-jaas-plugin/amq6-dual-jaas-plugin-parent)
[![License](https://img.shields.io/hexpm/l/plug.svg?maxAge=2592000)]()

# amq6-dual-jaas-plugin
This plugin is a customization and extension of the ActiveMQ JaasDualAuthenticationPlugin.
It allows clients to connect via username/password and Network of Brokers via certificates, both over SSL.

## Usage

    <broker>
    
        <!-- All your normal AMQ stuff -->
        
        <plugins>
            <bean id="jaasDualAuthenticationNetworkConnectorPlugin" class="com.garethahealy.amq6dualjaasplugin.JaasDualAuthenticationNetworkConnectorPlugin" xmlns="http://www.springframework.org/schema/beans">
                <property name="configuration" value="activemq-props"/>
                <property name="sslConfiguration" value="activemq-cert"/>
            </bean>
        </plugins>
        
    </broker>
