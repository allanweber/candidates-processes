package com.allanweber.candidatesprocesses.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class GithubRepository {
    private String name;

    private String description;

    private boolean fork;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("clone_url")
    private String cloneUrl;

    @JsonProperty("language")
    private String mainLanguage;

    private GithubRepositoryLanguage languages;
}