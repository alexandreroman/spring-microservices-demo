/*
 * Copyright (c) 2018 Pivotal Software, Inc.
 *
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
 */

package fr.alexandreroman.demos.springmicroservices.whoami;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@Slf4j
public class WhoamiController {
    private static final String VAR_CF_INSTANCE_INDEX = "CF_INSTANCE_INDEX";
    private static final String VAR_VCAP_APPLICATION = "VCAP_APPLICATION";
    private @Value("${spring.application.name}")
    String springApplicationName;

    @GetMapping("/api/whoami/hostname")
    public String hostname() throws UnknownHostException {
        return InetAddress.getLocalHost().getCanonicalHostName();
    }

    @GetMapping("/api/whoami/ip")
    public String ip() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    @GetMapping("/api/whoami/id")
    public String id(ObjectMapper objectMapper) throws IOException {
        final String appIndexStr = System.getenv(VAR_CF_INSTANCE_INDEX);
        final String appJson = System.getenv(VAR_VCAP_APPLICATION);
        if (appIndexStr == null || appJson == null) {
            return hostname();
        }

        final int appIndex = Integer.parseInt(appIndexStr);
        String appName = null;
        if (appJson != null) {
            final VcapApplication vcap = objectMapper.readValue(appJson, VcapApplication.class);
            appName = vcap.applicationName;
        }
        if (appName == null) {
            appName = springApplicationName;
        }
        return appName + "/" + appIndex;
    }

    @GetMapping("/api/whoami/info")
    public Info info(ObjectMapper objectMapper) throws IOException {
        final Info info = new Info();
        info.id = id(objectMapper);
        info.hostname = hostname();
        info.ip = ip();
        return info;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class VcapApplication {
        @JsonProperty("application_name")
        private String applicationName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class Info {
        private String id;
        private String hostname;
        private String ip;
    }
}
