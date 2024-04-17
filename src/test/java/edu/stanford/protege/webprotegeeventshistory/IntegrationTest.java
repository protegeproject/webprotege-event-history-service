package edu.stanford.protege.webprotegeeventshistory;


import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import edu.stanford.protege.webprotege.ipc.impl.RabbitMQEventsConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@RunWith(SpringRunner.class)
@Import({WebProtegeIpcApplication.class, WebprotegeEventsHistoryApplication.class, RabbitMQEventsConfiguration.class})
public abstract class IntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoTestExtension.class);

    private  static MongoDBContainer mongoDBContainer;
    private static RabbitMQContainer rabbitContainer;

    @BeforeClass
    public static void setUpContainers(){
        setUpMongo();
        setUpRabbitMq();
    }

    @AfterClass
    public static void closeContainers(){
        rabbitContainer.close();
        mongoDBContainer.close();
    }

    private static void setUpMongo(){
        var imageName = DockerImageName.parse("mongo");
        mongoDBContainer = new MongoDBContainer(imageName)
                .withExposedPorts(27017, 27017);
        mongoDBContainer.start();

        var mappedHttpPort = mongoDBContainer.getMappedPort(27017);
        LOGGER.info("MongoDB port 27017 is mapped to {}", mappedHttpPort);
        System.setProperty("spring.data.mongodb.port", Integer.toString(mappedHttpPort));
    }

    private static void setUpRabbitMq(){
        var imageName = DockerImageName.parse("rabbitmq:3.7.25-management-alpine");
        rabbitContainer = new RabbitMQContainer(imageName)
                .withExposedPorts(5672);
        rabbitContainer.start();

        System.setProperty("spring.rabbitmq.host", rabbitContainer.getHost());
        System.setProperty("spring.rabbitmq.port", String.valueOf(rabbitContainer.getAmqpPort()));
    }

}
