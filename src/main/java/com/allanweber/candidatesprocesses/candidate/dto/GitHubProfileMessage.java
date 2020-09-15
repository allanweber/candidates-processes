package com.allanweber.candidatesprocesses.candidate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class GitHubProfileMessage {

    private String user;

    private String candidateId;

    private String apiProfile;

    private String token;
}
