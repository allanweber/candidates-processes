package com.allanweber.candidatesprocesses.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class GithubRepositoryPulls {
    private Long id;
}
