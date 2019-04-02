package cc.yesway.service;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 消费者监听消息队列
 */
@Component
@Slf4j
@RabbitListener(queues = "DIRECT_QUEUE")
public class DirectQueueListener {

    @RabbitHandler
    public void process(String message,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException, InterruptedException {
        log.info("消费消息成功: {}", message);
        Thread.sleep(1000);
        switch (message) {
            case "nack":
                channel.basicNack(tag, true, false);
                break;
            case "nack-requeue":
                channel.basicNack(tag, true, true);
                break;
            case "reject":
                channel.basicReject(tag, false);
                break;
            case "reject-requeue": // 容易造成死循环
                channel.basicReject(tag, true);
                break;
            default:
                channel.basicAck(tag, true);
                break;
        }
    }

}
