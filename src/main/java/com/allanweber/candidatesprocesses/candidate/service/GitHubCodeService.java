package com.allanweber.candidatesprocesses.candidate.service;

import com.allanweber.candidatesprocesses.candidate.dto.GitHubProfileMessage;
import com.allanweber.candidatesprocesses.candidate.dto.GithubRepository;
import com.allanweber.candidatesprocesses.candidate.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class GitHubCodeService {

    private static final String REPOS_PATH = "repos";
    private static final String PAGE_QUERY = "page={page}";

    private final CandidateRepository repository;
    private final RestTemplate restTemplate;

    public void readRepositories(GitHubProfileMessage gitHubProfile) {

        List<GithubRepository> allPublicRepositories = new ArrayList<>();
        boolean hasNextPage;
        Integer page = 1;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + gitHubProfile.getToken());
        headers.add("Accept", "application/vnd.github.v3+json");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        do {
            String githubUri = UriComponentsBuilder.newInstance()
                    .uri(URI.create(gitHubProfile.getApiProfile()))
                    .pathSegment(REPOS_PATH)
                    .query(PAGE_QUERY).buildAndExpand(page).toUriString();

            log.info("Reading repositories on {}", githubUri);

            ResponseEntity<List<GithubRepository>> githubResponse =
                    restTemplate.exchange(githubUri, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
                    });
            allPublicRepositories.addAll(Optional.ofNullable(githubResponse.getBody()).orElse(Collections.emptyList()));

            hasNextPage = githubResponse.getHeaders().getOrEmpty("Link")
                    .stream().findFirst()
                    .map(link -> link.contains("rel=\"next\""))
                    .orElse(false);
            page++;
        } while (hasNextPage);

        List<GithubRepository> notForked = allPublicRepositories.stream().filter(repo -> !repo.isFork()).collect(Collectors.toList());
        repository.addPublicRepositories(gitHubProfile.getCandidateId(), notForked);
        repository.updateGitStatus(gitHubProfile.getCandidateId());
    }
}
