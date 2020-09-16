package com.allanweber.candidatesprocesses.github.repository;

import com.allanweber.candidatesprocesses.github.dto.GithubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
@Slf4j
public class CandidateRepoRepository {
    private static final String COLLECTION = "candidate-repository";
    private final MongoTemplate mongoTemplate;

    public void savePublicRepositories(String candidateId, List<GithubRepository> allPublicRepositories) {
        Query query = new Query();
        query.addCriteria(new Criteria("candidateId").is(new ObjectId(candidateId)));
        Update update = new Update().set("repositories", allPublicRepositories);
        log.info("Saving {} repositories for candidate {}", allPublicRepositories.size(), candidateId);
        mongoTemplate.upsert(query, update, COLLECTION);
    }
}
