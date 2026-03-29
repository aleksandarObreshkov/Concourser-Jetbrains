package org.alex.concourserjetbrains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {
    @JsonProperty("resources")
    Map<String, String> resources;
}
