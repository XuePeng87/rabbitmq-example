package cc.xuepeng.service;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * 消息生产者服务。
 *
 * @author xuepeng
 */
@Service
public class MessageProducer {

    /**
     * RabbitTemplate对象。
     */
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送direct消息。
     *
     * @param message 消息内容。
     */
    public void direct(String message) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("DIRECT_EXCHANGE", "DIRECT_ROUTING_KEY", message, correlationData);
    }

    /**
     * 发送direct消息。
     * 交换器存在，但队列不存在，为了测试Mandatory与ReturnCallback。
     *
     * @param message 消息内容。
     */
    public void directNotExistQueue(String message) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("DIRECT_EXCHANGE", "DIRECT_ROUTING_KEY_NOT_EXIST", message, correlationData);
    }

    /**
     * 发送direct消息。
     * 交换器不存在，队列也不存在，为了测试ConfirmCallback。
     *
     * @param message 消息内容。
     */
    public void directNotExistExchangeAndQueue(String message) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("DIRECT_EXCHANGE_NOT_EXIST", "DIRECT_ROUTING_KEY_NOT_EXIST", message, correlationData);
    }

}
