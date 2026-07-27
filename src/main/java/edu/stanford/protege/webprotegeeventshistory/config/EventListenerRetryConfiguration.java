package edu.stanford.protege.webprotegeeventshistory.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

/**
 * Installs a bounded, backing-off retry policy on the events listener container.
 * <p>
 * The container that consumes {@code PackagedProjectChangeEvent}s is created inside the ipc library
 * ({@code RabbitMQEventsConfiguration#eventsListenerContainer}) with no advice chain and the broker
 * default of requeue-on-reject. Once {@code registerEvent} rethrows a failed archive write (#299),
 * that default would requeue the message immediately and, for a genuinely poison message, hot-loop.
 * The container is not constructor-configurable from this repo, so this {@link BeanPostProcessor}
 * reaches the already-built bean by name (before it is started) and:
 * <ul>
 *   <li>wraps the listener in a stateless retry interceptor: bounded attempts with exponential
 *       backoff, so a transient Mongo blip is retried in-process and recovers;</li>
 *   <li>on exhaustion, the {@link RejectAndDontRequeueRecoverer} rejects without requeue, so a
 *       persistently failing (poison) message is dropped instead of looping;</li>
 *   <li>sets {@code defaultRequeueRejected=false} as a backstop against requeue-driven loops.</li>
 * </ul>
 * A redelivered event is safe: {@code HighLevelBusinessEvent} uses eventId as its Mongo @Id, so the
 * save upserts. Tunable via {@code webprotege.events.retry.*}.
 */
@Configuration
public class EventListenerRetryConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventListenerRetryConfiguration.class);

    /** Bean name of the ipc events listener container this policy applies to. */
    static final String EVENTS_CONTAINER_BEAN = "eventsListenerContainer";

    @Bean
    static BeanPostProcessor eventsListenerRetryPostProcessor(
            @Value("${webprotege.events.retry.max-attempts:5}") int maxAttempts,
            @Value("${webprotege.events.retry.initial-interval-ms:1000}") long initialIntervalMs,
            @Value("${webprotege.events.retry.multiplier:2.0}") double multiplier,
            @Value("${webprotege.events.retry.max-interval-ms:30000}") long maxIntervalMs) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof SimpleMessageListenerContainer container
                        && EVENTS_CONTAINER_BEAN.equals(beanName)) {
                    RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                            .maxAttempts(maxAttempts)
                            .backOffOptions(initialIntervalMs, multiplier, maxIntervalMs)
                            .recoverer(new RejectAndDontRequeueRecoverer(
                                    "Event archive write kept failing after retries; dropping message to avoid a hot loop"))
                            .build();
                    container.setAdviceChain(retryInterceptor);
                    container.setDefaultRequeueRejected(false);
                    LOGGER.info("Installed bounded retry policy on '{}' (maxAttempts={}, initialIntervalMs={}, multiplier={}, maxIntervalMs={})",
                            beanName, maxAttempts, initialIntervalMs, multiplier, maxIntervalMs);
                }
                return bean;
            }
        };
    }
}
