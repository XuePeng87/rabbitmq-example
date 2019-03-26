package cc.xuepeng.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

@Service
public class MessageProducer {

    private static final Log LOGGER = LogFactory.getLog(MessageProducer.class);

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void direct(String message) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("DIRECT_EXCHANGE", "DIRECT_ROUTING_KEY", message, correlationData);
    }

}
