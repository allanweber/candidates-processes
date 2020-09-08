package com.allanweber.candidatesprocesses.candidate.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonPropertyOrder({ "candidateId", "apiProfile", "token" })
public class GitHubProfileMessage {
    private String candidateId;

    private String apiProfile;

    private String token;
}
