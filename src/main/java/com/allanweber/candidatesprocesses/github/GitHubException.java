package com.allanweber.candidatesprocesses.github;

public class GitHubException extends RuntimeException {
    private static final long serialVersionUID = 833601494038994878L;

    public GitHubException(String message) {
        super(message);
    }
}
