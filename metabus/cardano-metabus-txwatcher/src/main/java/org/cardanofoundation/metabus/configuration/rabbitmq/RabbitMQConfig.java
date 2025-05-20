package org.cardanofoundation.metabus.configuration.rabbitmq;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import org.cardanofoundation.metabus.configuration.CardanoMetabusTxwatcherProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(value = {CardanoMetabusTxwatcherProperties.class})
public class RabbitMQConfig implements BeanFactoryAware {
    public static final String EXCHANGE = "exchange";
    public static final String BINDING = "binding";
    public static final String QUEUE = "queue";
    private BeanFactory beanFactory;



    private final CardanoMetabusTxwatcherProperties cardanoMetabusTxwatcherProperties;

    public RabbitMQConfig(CardanoMetabusTxwatcherProperties cardanoMetabusTxwatcherProperties) {
        this.cardanoMetabusTxwatcherProperties = cardanoMetabusTxwatcherProperties;
    }

    @Override
    public void setBeanFactory(@NotNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    // Programmatically create binding, routing key, queue beans from custom config
    @PostConstruct
    public void onPostConstruct() {
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        CardanoMetabusTxwatcherProperties.RabbitMQ rabbitMQProperty = cardanoMetabusTxwatcherProperties.getRabbitmq();

        // Config Dead Letter Queue
        // Get the queue name, exchange name 
        final String deadLetterExchangeName = rabbitMQProperty.getDeadLetterExchange();
        final String deadLetterRoutingKey = rabbitMQProperty.getDeadLetterRoutingKey();
        
        // Create dead letter exchange, dead queue and binding the dead queue to dead exchange
        final DirectExchange deadLetterExchange = new DirectExchange(deadLetterExchangeName);
        final Queue queueDeadLetter = new Queue(rabbitMQProperty.getDeadLetterQueue());
        final Binding bindingDeadLetter = BindingBuilder.bind(queueDeadLetter).to(deadLetterExchange).with(deadLetterRoutingKey);
        configurableBeanFactory.registerSingleton("dead-letter-exchange", deadLetterExchange);
        configurableBeanFactory.registerSingleton(QUEUE + rabbitMQProperty.getDeadLetterExchange(), queueDeadLetter);
        configurableBeanFactory.registerSingleton(BINDING + rabbitMQProperty.getDeadLetterRoutingKey(), bindingDeadLetter);

        // Config Main Queue
        String exchange = rabbitMQProperty.getExchange();
        DirectExchange directExchange = new DirectExchange(exchange);
        configurableBeanFactory.registerSingleton(EXCHANGE, directExchange);

        List<CardanoMetabusTxwatcherProperties.Binding> bindings = rabbitMQProperty.getBindings();

        for (CardanoMetabusTxwatcherProperties.Binding bindingProperty : bindings) {
            String queueName = bindingProperty.getQueue();
            Queue queue = configQueue(queueName, deadLetterExchangeName, deadLetterRoutingKey, bindingProperty.getMessageTTL(), bindingProperty.getHasDLQ());
            String routingKey = bindingProperty.getRoutingKey();
            Binding binding = BindingBuilder.bind(queue).to(directExchange).with(routingKey);

            configurableBeanFactory.registerSingleton(QUEUE + queueName, queue);
            configurableBeanFactory.registerSingleton(BINDING + routingKey, binding);
        }
    }

    private Queue configQueue(String queueName, String deadLetterExchange, String deadLetterRoutingKey, Long messageTTL, Boolean hasDLQ) {
        final Map<String, Object> args = new HashMap<>();
        if(hasDLQ != null && hasDLQ) {
            args.put("x-dead-letter-exchange", deadLetterExchange);
            args.put("x-dead-letter-routing-key", deadLetterRoutingKey);
        }

        if (messageTTL != null && messageTTL > 0) {
            args.put("x-message-ttl", messageTTL);
        }
        
        return new Queue(queueName, true, false, false, args);
    }

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
