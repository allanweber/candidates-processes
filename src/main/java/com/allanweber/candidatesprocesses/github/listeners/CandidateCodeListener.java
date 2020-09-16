package com.allanweber.candidatesprocesses.github.listeners;

import com.allanweber.candidatesprocesses.github.dto.GitHubProfileMessage;
import com.allanweber.candidatesprocesses.github.service.GitHubCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class CandidateCodeListener {

    private final GitHubCodeService gitHubCodeService;

    @RabbitListener(queues = "${app.queue.candidate.candidate-code-queue}")
    public void receive(GitHubProfileMessage gitHubProfileMessage) {
        if (log.isInfoEnabled()) {
            log.info("Received new candidate code message-> {}", gitHubProfileMessage);
        }
        gitHubCodeService.readRepositories(gitHubProfileMessage);
    }
}
