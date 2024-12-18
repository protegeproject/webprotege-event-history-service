package edu.stanford.protege.webprotegeeventshistory;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoTestExtension implements BeforeAllCallback, AfterAllCallback {

    private static MongoDBContainer mongoDBContainer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        var imageName = DockerImageName.parse("mongo");
        mongoDBContainer = new MongoDBContainer(imageName)
                .withExposedPorts(27017);
        mongoDBContainer.start();

        var mappedPort = mongoDBContainer.getMappedPort(27017);
        var mongoUri = String.format("mongodb://localhost:%d", mappedPort);

        System.setProperty("spring.data.mongodb.uri", mongoUri);
        System.setProperty("spring.data.mongodb.port", Integer.toString(mappedPort));
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        if (mongoDBContainer != null) {
            mongoDBContainer.stop();
        }
    }
}

