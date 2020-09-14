package com.allanweber.candidatesprocesses.candidate.repository;

import com.allanweber.candidatesprocesses.candidate.dto.GithubRepository;
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
public class CandidateRepository {

    private static final String COLLECTION = "candidate";

    private final MongoTemplate mongoTemplate;

    public void updateGitStatus(String candidateId) {
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(new ObjectId(candidateId)).and("socialEntries.type").is("GITHUB"));
        Update update = new Update().set("socialEntries.$.status", "GRANTED");
        mongoTemplate.updateFirst(query, update, COLLECTION);
    }

    public void addPublicRepositories(String candidateId, List<GithubRepository> allPublicRepositories) {
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(new ObjectId(candidateId)));
        Update update = new Update().set("repositories", allPublicRepositories);
        log.info("Saving {} repositories for candidate {}", allPublicRepositories.size(), candidateId);
        mongoTemplate.updateFirst(query, update, COLLECTION);
    }
}
