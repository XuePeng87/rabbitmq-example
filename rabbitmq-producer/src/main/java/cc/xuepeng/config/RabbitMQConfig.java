package cc.xuepeng.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;

/**
 * RabbitMQ的配置对象。
 *
 * @author xuepeng
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    /**
     * RabbitTemplate对象。
     */
    @Resource
    private RabbitTemplate rabbitTemplate;
    /**
     * 死信交换器名称。
     */
    private static final String DEAD_LETTER_EXCHANGE_NAME = "DEAD_LETTER_EXCHANGE";
    /**
     * 死信队列名称。
     */
    private static final String DEAD_LETTER_QUEUE_NAME = "DEAD_LETTER_QUEUE";
    /**
     * 死信队列路由键。
     */
    private static final String DEAD_LETTER_ROUTING_KEY_NAME = "DEAD_LETTER_ROUTING_KEY";

    /**
     * 定制AmqpTemplate对象。
     * 可根据需要定制多个。
     *
     * @return AmqpTemplate对象。
     */
    @Bean
    public AmqpTemplate amqpTemplate() {
        // 设置消息转换器为Jackson
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setEncoding("UTF-8");
        // 设置不接受不可路由的消息，需要在yml中配置：publisher-returns: true
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            String correlationId = message.getMessageProperties().getCorrelationId();
            log.warn("setReturnCallback -> 消息 {} 发送失败，应答码：{}，原因：{}，交换器: {}，路由键：{}",
                    correlationId,
                    replyCode,
                    replyText,
                    exchange,
                    routingKey);
        });
        // 设置消息发布确认功能，需要在yml中配置：publisher-confirms: true
//        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
//            if (ack) {
//                log.info("setConfirmCallback -> 消息发布到交换器成功，id：{}", correlationData);
//            } else {
//                log.warn("setConfirmCallback -> 消息发布到交换器失败，错误原因为：{}", cause);
//            }
//        });
        // 开启事务模式，需要在yml中配置：publisher-confirms: false
        rabbitTemplate.setChannelTransacted(true);
        return rabbitTemplate;
    }

    /**
     * 声明RabbitMQ事务管理器。
     *
     * @param connectionFactory 连接工厂。
     * @return PlatformTransactionManager对象。
     */
    @Bean
    public PlatformTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new RabbitTransactionManager(connectionFactory);
    }

    /**
     * 声明Direct交换器。
     * 同时指定备用交换器。
     *
     * @return Exchange对象。
     */
    @Bean("directExchange")
    public Exchange directExchange() {
        return ExchangeBuilder.directExchange("DIRECT_EXCHANGE")
                .durable(false)
                .withArgument("alternate-exchange", "UN_ROUTE_EXCHANGE")
                .build();
    }

    /**
     * 声明备用交换器。
     *
     * @return Exchange对象。
     */
    @Bean("unRouteExchange")
    public Exchange unRouteExchange() {
        return ExchangeBuilder.fanoutExchange("UN_ROUTE_EXCHANGE")
                .durable(false)
                .build();
    }

    /**
     * 声明死信交换器。
     *
     * @return Exchange对象。
     */
    @Bean("deadLetterExchange")
    public Exchange deadLetterExchange() {
        return ExchangeBuilder.fanoutExchange(DEAD_LETTER_EXCHANGE_NAME)
                .durable(false)
                .build();
    }

    /**
     * 声明测试事务的Direct交换器。
     *
     * @return Exchange对象。
     */
    @Bean("directTransactionExchange")
    public Exchange directTransactionExchange() {
        return ExchangeBuilder.directExchange("DIRECT_TRANSACTION_EXCHANGE")
                .durable(false)
                .build();
    }

    /**
     * 声明队列。
     * 同时指定死信队列。
     *
     * @return Queue对象。
     */
    @Bean("directQueue")
    public Queue directQueue() {
        return QueueBuilder.durable("DIRECT_QUEUE")
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY_NAME)
                .build();
    }

    /**
     * 声明备用队列。
     *
     * @return Queue对象。
     */
    @Bean("unRouteQueue")
    public Queue unRouteQueue() {
        return QueueBuilder.durable("UN_ROUTE_QUEUE").build();
    }

    /**
     * 声明死信队列。
     *
     * @return Queue对象。
     */
    @Bean("deadLetterQueue")
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE_NAME).build();
    }

    /**
     * 声明测试事务的队列。
     *
     * @return Queue对象。
     */
    @Bean("transactionQueue")
    public Queue transactionQueue() {
        return QueueBuilder.durable("TRANSACTION_QUEUE").build();
    }

    /**
     * 绑定交换器与队列。
     *
     * @param queue    队列。
     * @param exchange 交换器。
     * @return Binding对象。
     */
    @Bean
    public Binding directBinding(@Qualifier("directQueue") Queue queue,
                                 @Qualifier("directExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("DIRECT_ROUTING_KEY").noargs();
    }

    /**
     * 绑定备用交换器与队列。
     *
     * @param queue    队列。
     * @param exchange 交换器。
     * @return Binding对象。
     */
    @Bean
    public Binding unRouteBinding(@Qualifier("unRouteQueue") Queue queue,
                                  @Qualifier("unRouteExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("UN_ROUTE_ROUTING_KEY").noargs();
    }

    /**
     * 绑定死信交换器与队列。
     *
     * @param queue    死信队列。
     * @param exchange 死信交换器。
     * @return Binding对象。
     */
    @Bean
    public Binding deadLetterBinding(@Qualifier("deadLetterQueue") Queue queue,
                                     @Qualifier("deadLetterExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(DEAD_LETTER_ROUTING_KEY_NAME).noargs();
    }

    /**
     * 绑定测试事务的交换器与队列。
     *
     * @param queue    队列。
     * @param exchange 交换器。
     * @return Binding对象。
     */
    @Bean
    public Binding directTransactionBinding(@Qualifier("transactionQueue") Queue queue,
                                            @Qualifier("directTransactionExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("DIRECT_TRANSACTION_ROUTING_KEY").noargs();
    }

}
