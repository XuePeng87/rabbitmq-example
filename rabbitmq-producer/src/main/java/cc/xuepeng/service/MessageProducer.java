package cc.xuepeng.service;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 在事务模式下，发送direct消息。
     * <p>
     * 第一次发送，消息可以正常路由到队列。
     * 第二次发送，消息不能路由到队列。
     */
     @Transactional(rollbackFor = Exception.class)
    public void directOnTransaction(String message) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("DIRECT_TRANSACTION_EXCHANGE", "DIRECT_TRANSACTION_ROUTING_KEY", message, correlationData);
        rabbitTemplate.convertAndSend("DIRECT_TRANSACTION_EXCHANGE_NOT_EXIST", "DIRECT_TRANSACTION_ROUTING_KEY_NOT_EXIST", message, correlationData);
    }

    /**
     * 发送direct非持久化消息。
     * RabbitTemplate默认采用消息持久化存储。
     *
     * @param message 消息内容。
     */
    public void directNonPersistent(String message) {
        rabbitTemplate.convertAndSend("DIRECT_EXCHANGE",
                "DIRECT_ROUTING_KEY",
                message, msg -> {
                    msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
                    msg.getMessageProperties().setCorrelationId(UUID.randomUUID().toString());
                    return msg;
                }
        );
    }

}
