package edu.stanford.protege.webprotegeeventshistory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@Configuration
@EnableMongoRepositories(basePackages = "edu.stanford.protege.webprotegeeventshistory")
public class MongoConfiguration {
}
