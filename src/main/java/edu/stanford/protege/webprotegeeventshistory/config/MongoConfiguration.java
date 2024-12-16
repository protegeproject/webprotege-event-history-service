package edu.stanford.protege.webprotegeeventshistory.config;

import com.mongodb.*;
import edu.stanford.protege.webprotegeeventshistory.config.converters.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import javax.annotation.Nonnull;
import java.util.Arrays;


@Configuration
@EnableMongoRepositories(basePackages = "edu.stanford.protege.webprotegeeventshistory")
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017}")
    private String mongoUri;

    @Nonnull
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Nonnull
    @Override
    public MongoClientSettings mongoClientSettings() {
        return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .build();
    }

    @Nonnull
    @Override
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new ProjectIdToStringConverter(),
                new StringToProjectIdConverter()
        ));
    }
}

