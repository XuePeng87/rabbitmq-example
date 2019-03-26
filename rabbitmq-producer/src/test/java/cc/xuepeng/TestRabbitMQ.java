package cc.xuepeng;

import cc.xuepeng.service.MessageProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TestRabbitMQ {

    @Autowired
    private MessageProducer messageProducer;

    @Test
    public void testSend() {
        messageProducer.direct("hello world");
    }

}
