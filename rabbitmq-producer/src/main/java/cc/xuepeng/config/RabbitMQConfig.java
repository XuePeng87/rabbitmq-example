package cc.xuepeng.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

/**
 * RabbitMQ的配置对象。
 *
 * @author xuepeng
 */
public class RabbitMQConfig {

    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final Log LOGGER = LogFactory.getLog(RabbitMQConfig.class);

    /**
     * 定制AmqpTemplate对象。
     * 可根据需要定制多个。
     *
     * @return AmqpTemplate对象。
     */
    public AmqpTemplate amqpTemplate() {
        // 设置消息转换器为Jackson
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setEncoding("UTF-8");
        // 不接受不可路由的消息，需要在yml中配置：publisher-returns: true
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                String correlationId = message.getMessageProperties().getCorrelationId();
                LOGGER.info(String.format("消息 %s 发送失败，应答码：%s，原因：%s，交换机: %s，路由键：%s",
                        correlationId,
                        replyCode,
                        replyText,
                        exchange,
                        routingKey));
            }
        });
        return rabbitTemplate;
    }

    /**
     * 声明Direct交换机 支持持久化.
     *
     * @return the exchange
     */
    @Bean("directExchange")
    public Exchange directExchange() {
        return ExchangeBuilder.directExchange("DIRECT_EXCHANGE").durable(false).build();
    }



}
