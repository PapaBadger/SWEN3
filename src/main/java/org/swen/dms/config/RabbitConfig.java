package org.swen.dms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration class defining the messaging infrastructure for the application.
 *
 * Declares the exchange, queue, and binding used for document events,
 * and registers the JSON message converter.
 *
 * Components:
 *  - Exchange: {@code docs.exchange} (topic exchange for document events)
 *  - Queue: {@code docs.ocr.queue} (receives document creation messages)
 *  - Routing Key: {@code docs.created}
 *
 * Also enables RabbitMQ listener support through {@link org.springframework.amqp.rabbit.annotation.EnableRabbit}.
 */

@EnableRabbit
@Configuration
public class RabbitConfig {
    public static final String EXCHANGE_DOCS = "docs.exchange";
    public static final String ROUTING_DOC_CREATED = "docs.created";
    public static final String QUEUE_OCR = "docs.ocr.queue";
    public static final String ROUTING_DOC_UPDATED = "docs.updated";

    @Bean
    public TopicExchange docsExchange() {
        return new TopicExchange(EXCHANGE_DOCS, true, false);
    }

    @Bean
    public Queue ocrQueue() {
        return QueueBuilder.durable(QUEUE_OCR).build();
    }

    @Bean
    public Binding bindOcrQueue() {
        return BindingBuilder.bind(ocrQueue())
                .to(docsExchange())
                .with(ROUTING_DOC_CREATED);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
