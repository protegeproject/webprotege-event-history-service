package edu.stanford.protege.webprotegeeventshistory;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoTestExtension implements BeforeAllCallback, AfterAllCallback {

    private  static MongoDBContainer mongoDBContainer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        var imageName = DockerImageName.parse("mongo");
        mongoDBContainer = new MongoDBContainer(imageName)
                .withExposedPorts(27017, 27017);
        mongoDBContainer.start();

        var mappedHttpPort = mongoDBContainer.getMappedPort(27017);
        System.setProperty("spring.data.mongodb.port", Integer.toString(mappedHttpPort));

    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        mongoDBContainer.stop();
    }
}
