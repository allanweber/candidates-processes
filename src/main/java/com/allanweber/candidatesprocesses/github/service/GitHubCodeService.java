package com.allanweber.candidatesprocesses.github.service;

import com.allanweber.candidatesprocesses.github.GitHubException;
import com.allanweber.candidatesprocesses.github.dto.GitHubProfileMessage;
import com.allanweber.candidatesprocesses.github.dto.GithubRepository;
import com.allanweber.candidatesprocesses.github.dto.GithubRepositoryLanguage;
import com.allanweber.candidatesprocesses.candidate.repository.CandidateRepository;
import com.allanweber.candidatesprocesses.github.repository.CandidateRepoRepository;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class GitHubCodeService {

    private static final String GIT_API = "https://api.github.com";
    private static final String REPOS_PATH = "repos";
    private static final String LANGUAGES_PATH = "languages";
    private static final String PAGE_QUERY = "page={page}";

    private final CandidateRepository candidateRepository;
    private final CandidateRepoRepository candidateRepoRepository;
    private final RestTemplate restTemplate;

    public void readRepositories(GitHubProfileMessage gitHubProfile) {

        List<GithubRepository> allPublicRepositories = new ArrayList<>();
        boolean hasNextPage;
        Integer page = 1;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + gitHubProfile.getToken());
        headers.add("Accept", "application/vnd.github.v3+json");
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        do {
            String githubUri = UriComponentsBuilder.newInstance()
                    .uri(URI.create(gitHubProfile.getApiProfile()))
                    .pathSegment(REPOS_PATH)
                    .query(PAGE_QUERY).buildAndExpand(page).toUriString();

            log.info("Reading repositories on {}", githubUri);

            ResponseEntity<List<GithubRepository>> githubResponse =
                    restTemplate.exchange(githubUri, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<>() {
                    });

            List<GithubRepository> repositories = Optional.ofNullable(githubResponse.getBody())
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(repo -> !repo.isFork())
                    .collect(Collectors.toList());

            repositories.forEach(getLanguages(gitHubProfile, httpEntity));

            allPublicRepositories.addAll(repositories);

            hasNextPage = githubResponse.getHeaders().getOrEmpty("Link")
                    .stream().findFirst()
                    .map(link -> link.contains("rel=\"next\""))
                    .orElse(false);
            page++;
        } while (hasNextPage);

        String owner = candidateRepository.getCandidateOwner(gitHubProfile.getCandidateId())
                .orElseThrow(() -> new GitHubException(String.format("Owner not found for candidate %s", gitHubProfile.getCandidateId())));

        candidateRepoRepository.savePublicRepositories(gitHubProfile.getCandidateId(), owner, allPublicRepositories);
        candidateRepository.updateGitStatus(gitHubProfile.getCandidateId());
    }

    private Consumer<GithubRepository> getLanguages(GitHubProfileMessage gitHubProfile, HttpEntity<?> httpEntity) {
        return repository -> {
            String githubUri = UriComponentsBuilder.newInstance()
                    .uri(URI.create(GIT_API))
                    .pathSegment(REPOS_PATH)
                    .pathSegment(gitHubProfile.getUser())
                    .pathSegment(repository.getName())
                    .pathSegment(LANGUAGES_PATH)
                    .toUriString();

            log.info("Reading languages of repository {} on {}", repository.getName(), githubUri);

            ResponseEntity<GithubRepositoryLanguage> languagesResponse =
                    restTemplate.exchange(githubUri, HttpMethod.GET, httpEntity, GithubRepositoryLanguage.class);

            GithubRepositoryLanguage languages = languagesResponse.getBody();
            Long total = Objects.requireNonNull(languages).getLanguages().values().stream().reduce(Long::sum).orElse(0L);
            Map<String, BigDecimal> proportions = languages.getLanguages().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> BigDecimal.valueOf((float)(entry.getValue() * 100) / total).setScale(2, RoundingMode.HALF_UP)));
            languages.setProportion(proportions);
            repository.setLanguages(languages);
        };
    }
}
