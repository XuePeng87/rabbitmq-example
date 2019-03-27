package cc.xuepeng.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            log.warn("消息 {} 发送失败，应答码：{}，原因：{}，交换机: {}，路由键：{}",
                    correlationId,
                    replyCode,
                    replyText,
                    exchange,
                    routingKey);
        });
        // 设置消息发布确认功能，需要在yml中配置：publisher-confirms: true
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息发布到交换器成功，id：{}", correlationData);
            } else {
                log.warn("消息发布到交换器失败，错误原因为：{}", cause);
            }
        });
        return rabbitTemplate;
    }

    /**
     * 声明Direct交换机。
     *
     * @return Exchange对象。
     */
    @Bean("directExchange")
    public Exchange directExchange() {
        return ExchangeBuilder.directExchange("DIRECT_EXCHANGE").durable(false).build();
    }

    /**
     * 声明队列。
     *
     * @return Queue对象。
     */
    @Bean("directQueue")
    public Queue directQueue() {
        return QueueBuilder.durable("DIRECT_QUEUE").build();
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

}
