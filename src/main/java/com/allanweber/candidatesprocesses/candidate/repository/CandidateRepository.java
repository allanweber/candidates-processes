package com.allanweber.candidatesprocesses.candidate.repository;

import com.mongodb.BasicDBObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    public Optional<String> getCandidateOwner(String candidateId) {
        MatchOperation match = Aggregation.match(new Criteria("_id").is(new ObjectId(candidateId)));
        ProjectionOperation project = Aggregation.project("owner").andExclude("_id");
        Aggregation aggregation = Aggregation.newAggregation(match, project);
        AggregationResults<BasicDBObject> aggregate = mongoTemplate.aggregate(aggregation, COLLECTION, BasicDBObject.class);
        return aggregate.getMappedResults().stream().findFirst().map(item -> item.get("owner").toString());
    }
}
