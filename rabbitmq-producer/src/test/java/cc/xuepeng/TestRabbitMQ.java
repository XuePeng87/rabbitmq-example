package cc.xuepeng;

import cc.xuepeng.service.MessageProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * RabbitMQ的单元测试类。
 *
 * @author xuepeng
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TestRabbitMQ {

    /**
     * 消息生产者服务。
     */
    @Autowired
    private MessageProducer messageProducer;

    /**
     * 发送direct消息。
     */
    @Test
    public void testDirect() {
        messageProducer.direct("{}");
    }

    /**
     * 发送direct消息，但消息路由不存在。
     * 交换器存在，但队列不存在，为了测试Mandatory与ReturnCallback。
     */
    @Test
    public void testDirectNotExistQueue() {
        messageProducer.directNotExistQueue("{}");
    }

    /**
     * 发送direct消息，交换器和路由都不存在。
     * 交换器不存在，队列也不存在，为了测试ConfirmCallback。
     */
    @Test
    public void testDirectNotExistExchangeAndQueue() {
        messageProducer.directNotExistExchangeAndQueue("{}");
    }

}
