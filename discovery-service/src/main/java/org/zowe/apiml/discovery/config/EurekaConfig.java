/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package org.zowe.apiml.discovery.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.zowe.apiml.discovery.ApimlInstanceRegistry;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.resources.ServerCodecs;
import org.springframework.cloud.netflix.eureka.server.InstanceRegistryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration to rewrite default Eureka's implementation with custom one
 */
@Configuration
public class EurekaConfig {

    @Value("${apiml.discovery.serviceIdPrefixReplacer:#{null}}")
    private String tuple;

    @Bean
    @Primary
    public ApimlInstanceRegistry getApimlInstanceRegistry(
        EurekaServerConfig serverConfig,
        EurekaClientConfig clientConfig,
        ServerCodecs serverCodecs,
        EurekaClient eurekaClient,
        InstanceRegistryProperties instanceRegistryProperties,
        ApplicationContext appCntx)
    {
        eurekaClient.getApplications(); // force initialization
        return new ApimlInstanceRegistry(serverConfig, clientConfig, serverCodecs, eurekaClient, instanceRegistryProperties,appCntx, new Tuple(tuple));
    }

    public static class Tuple {

        boolean valid;
        String oldPrefix;
        String newPrefix;

        public Tuple(String tuple) {
            if (isValidTuple(tuple)) {
                String[] prefixes = tuple.split(",");
                this.oldPrefix = prefixes[0];
                this.newPrefix = prefixes[1];
                this.valid = true;
            }
        }

        public boolean isValid() {
            return valid;
        }

        public String getOldPrefix() {
            return oldPrefix;
        }

        public String getNewPrefix() {
            return newPrefix;
        }

        public static boolean isValidTuple(String tuple) {
            if (StringUtils.isNotEmpty(tuple)) {
                String[] replacer = tuple.split(",");
                return replacer.length > 1 &&
                    StringUtils.isNotEmpty(replacer[0]) &&
                    StringUtils.isNotEmpty(replacer[1]) &&
                    !replacer[0].equals(replacer[1]);
            }
            return false;
        }

    }

}
