package com.allanweber.candidatesprocesses.github.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class GithubRepositoryLanguage {

    private final Map<String, Long> languages = new ConcurrentHashMap<>();

    @Setter
    private Map<String, BigDecimal> proportion;

    @JsonAnySetter
    public void setLanguages(String key, Long value) {
        languages.put(key, value);
    }
}
