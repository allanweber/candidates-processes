package com.allanweber.candidatesprocesses.candidate.listeners;

import com.allanweber.candidatesprocesses.candidate.dto.GitHubProfileMessage;
import com.allanweber.candidatesprocesses.candidate.service.GitHubCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class CandidateListener {

    private final GitHubCodeService gitHubCodeService;

    @RabbitListener(queues = "${app.queue.candidate.candidate-code-queue}")
    public void receive(GitHubProfileMessage gitHubProfileMessage) {
        if (log.isInfoEnabled()) {
            log.info("Received new candidate code message-> {}", gitHubProfileMessage);
        }
        gitHubCodeService.readRepositories(gitHubProfileMessage);
    }
}
