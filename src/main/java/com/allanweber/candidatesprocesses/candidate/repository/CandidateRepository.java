package com.allanweber.candidatesprocesses.candidate.repository;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CandidateRepository {

    private static final String COLLECTION = "candidate";

    private final MongoTemplate mongoTemplate;

    public void updateGitStatus(String candidateId) {
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(new ObjectId(candidateId)).and("socialEntries.type").is("GITHUB"));
        Update update = new Update().set("socialEntries.$.status", "GRANTED");
        mongoTemplate.updateFirst(query, update, COLLECTION);
    }
}
