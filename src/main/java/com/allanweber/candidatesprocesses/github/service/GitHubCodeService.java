package com.allanweber.candidatesprocesses.github.service;

import com.allanweber.candidatesprocesses.candidate.repository.CandidateRepository;
import com.allanweber.candidatesprocesses.github.GitHubException;
import com.allanweber.candidatesprocesses.github.dto.*;
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
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
@SuppressWarnings("PMD")
public class GitHubCodeService {

    private static final String GIT_API = "https://api.github.com";
    private static final String REPOS_PATH = "repos";
    private static final String LANGUAGES_PATH = "languages";
    private static final String COMMITS_PATH = "commits";
    private static final String BRANCHES_PATH = "branches";
    private static final String PULLS_PATH = "pulls";
    private static final String PAGE_QUERY = "page={page}";

    private final CandidateRepository candidateRepository;
    private final CandidateRepoRepository candidateRepoRepository;
    private final RestTemplate restTemplate;

    private HttpEntity<?> httpEntity;

    public void readRepositories(GitHubProfileMessage gitHubProfile) {

        List<GithubRepository> allPublicRepositories = new ArrayList<>();
        boolean hasNextPage;
        Integer page = 1;

        this.setHttpEntity(gitHubProfile);

        do {
            String githubUri = UriComponentsBuilder.newInstance()
                    .uri(URI.create(gitHubProfile.getApiProfile()))
                    .pathSegment(REPOS_PATH)
                    .query(PAGE_QUERY).buildAndExpand(page).toUriString();

            log.info("Reading repositories on {}", githubUri);

            ResponseEntity<List<GithubRepository>> githubResponse =
                    restTemplate.exchange(githubUri, HttpMethod.GET, this.httpEntity, new ParameterizedTypeReference<>() {
                    });

            List<GithubRepository> repositories = Optional.ofNullable(githubResponse.getBody())
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(repo -> !repo.isFork())
                    .collect(Collectors.toList());

            repositories.forEach(repository -> {
                getLanguages(gitHubProfile, repository);
                getCommits(gitHubProfile, repository);
                getBranches(gitHubProfile, repository);
                getPulls(gitHubProfile, repository);
            });

            allPublicRepositories.addAll(repositories);

            hasNextPage = hasNextPage(githubResponse);
            page++;
        } while (hasNextPage);

        String owner = candidateRepository.getCandidateOwner(gitHubProfile.getCandidateId())
                .orElseThrow(() -> new GitHubException(String.format("Owner not found for candidate %s", gitHubProfile.getCandidateId())));

        candidateRepoRepository.savePublicRepositories(gitHubProfile.getCandidateId(), owner, allPublicRepositories);
        candidateRepository.updateGitStatus(gitHubProfile.getCandidateId());
    }

    private void getLanguages(GitHubProfileMessage gitHubProfile, GithubRepository repository) {
        String githubUri = getGithubUriFeature(gitHubProfile, repository, LANGUAGES_PATH).toUriString();

        log.info("Reading languages of repository {} on {}", repository.getName(), githubUri);

        ResponseEntity<GithubRepositoryResponseLanguage> languagesResponse;
        try {
            languagesResponse = restTemplate.exchange(githubUri, HttpMethod.GET, this.httpEntity, GithubRepositoryResponseLanguage.class);
        } catch (Exception e) {
            log.error("Erro reading {} repository languages on {}", repository.getName(), githubUri, e);
            return;
        }

        GithubRepositoryResponseLanguage languages = languagesResponse.getBody();
        Long total = Objects.requireNonNull(languages).getLanguages().values().stream().reduce(Long::sum).orElse(0L);

        List<GithubRepositoryLanguage> repositoryLanguages = languages.getLanguages().entrySet().stream()
                .map(entry ->
                        GithubRepositoryLanguage
                                .builder()
                                .name(entry.getKey())
                                .size(entry.getValue())
                                .proportion(BigDecimal.valueOf((float) (entry.getValue() * 100) / total).setScale(2, RoundingMode.HALF_UP))
                                .build()
                ).collect(Collectors.toList());

        repository.setLanguages(repositoryLanguages);
    }

    private void getCommits(GitHubProfileMessage gitHubProfile, GithubRepository repository) {
        List<GithubRepositoryCommits> commits = new ArrayList<>();
        boolean hasNextPage = false;
        Integer page = 1;

        do {
            String githubUri = getGithubUriFeature(gitHubProfile, repository, COMMITS_PATH)
                    .query(PAGE_QUERY).buildAndExpand(page).toUriString();

            log.info("Reading commits of repository {} on {}", repository.getName(), githubUri);

            ResponseEntity<List<GithubRepositoryCommits>> commitsResponse;
            try {
                commitsResponse = restTemplate.exchange(githubUri, HttpMethod.GET, this.httpEntity, new ParameterizedTypeReference<>() {
                });
            } catch (Exception e) {
                log.error("Erro reading {} repository commits on {}", repository.getName(), githubUri, e);
                continue;
            }
            Optional.ofNullable(commitsResponse.getBody()).ifPresent(commits::addAll);
            hasNextPage = hasNextPage(commitsResponse);
            page++;
        } while (hasNextPage);

        repository.setCommits(commits.size());
    }

    private void getBranches(GitHubProfileMessage gitHubProfile, GithubRepository repository) {
        List<GithubRepositoryBranches> branches = new ArrayList<>();
        boolean hasNextPage = false;
        Integer page = 1;

        do {
            String githubUri = getGithubUriFeature(gitHubProfile, repository, BRANCHES_PATH)
                    .query(PAGE_QUERY).buildAndExpand(page).toUriString();

            log.info("Reading branches of repository {} on {}", repository.getName(), githubUri);

            ResponseEntity<List<GithubRepositoryBranches>> commitsResponse;
            try {
                commitsResponse = restTemplate.exchange(githubUri, HttpMethod.GET, this.httpEntity, new ParameterizedTypeReference<>() {
                });
            } catch (Exception e) {
                log.error("Erro reading {} repository branches on {}", repository.getName(), githubUri, e);
                continue;
            }
            Optional.ofNullable(commitsResponse.getBody()).ifPresent(branches::addAll);
            hasNextPage = hasNextPage(commitsResponse);
            page++;
        } while (hasNextPage);

        repository.setBranches(branches.size());
    }

    private void getPulls(GitHubProfileMessage gitHubProfile, GithubRepository repository) {
        List<GithubRepositoryPulls> pulls = new ArrayList<>();
        boolean hasNextPage = false;
        Integer page = 1;

        do {
            String githubUri = getGithubUriFeature(gitHubProfile, repository, PULLS_PATH)
                    .query(PAGE_QUERY)
                    .queryParam("state", "all")
                    .buildAndExpand(page)
                    .toUriString();

            log.info("Reading pulls of repository {} on {}", repository.getName(), githubUri);

            ResponseEntity<List<GithubRepositoryPulls>> commitsResponse;
            try {
                commitsResponse = restTemplate.exchange(githubUri, HttpMethod.GET, this.httpEntity, new ParameterizedTypeReference<>() {
                });
            } catch (Exception e) {
                log.error("Erro reading {} repository pulls on {}", repository.getName(), githubUri, e);
                continue;
            }
            Optional.ofNullable(commitsResponse.getBody()).ifPresent(pulls::addAll);
            hasNextPage = hasNextPage(commitsResponse);
            page++;
        } while (hasNextPage);

        repository.setPulls(pulls.size());
    }

    private UriComponentsBuilder getGithubUriFeature(GitHubProfileMessage gitHubProfile, GithubRepository repository, String commitsPath) {
        return UriComponentsBuilder.newInstance()
                .uri(URI.create(GIT_API))
                .pathSegment(REPOS_PATH)
                .pathSegment(gitHubProfile.getUser())
                .pathSegment(repository.getName())
                .pathSegment(commitsPath);
    }

    private void setHttpEntity(GitHubProfileMessage gitHubProfile) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + gitHubProfile.getToken());
        headers.add("Accept", "application/vnd.github.v3+json");
        this.httpEntity = new HttpEntity<>(headers);
    }

    private boolean hasNextPage(ResponseEntity<?> response) {
        return response.getHeaders().getOrEmpty("Link")
                .stream().findFirst()
                .map(link -> link.contains("rel=\"next\""))
                .orElse(false);
    }
}
