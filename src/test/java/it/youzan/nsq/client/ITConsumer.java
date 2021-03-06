package it.youzan.nsq.client;

import com.youzan.nsq.client.ConsumerImplV2;
import com.youzan.nsq.client.MessageHandler;
import com.youzan.nsq.client.core.ConnectionManager;
import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.nsq.client.utils.TopicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Test(groups = {"ITConsumer-Base"}, dependsOnGroups = {"ITProducer-Base"}, priority = 5)
public class ITConsumer extends AbstractITConsumer{
    private static final Logger logger = LoggerFactory.getLogger(ITConsumer.class);

    public void testExpectedRdy() throws Exception {
        final CountDownLatch latch = new CountDownLatch(10);
        final AtomicInteger received = new AtomicInteger(0);
        int expectedRdy = 3;
        NSQConfig config = new NSQConfig("BaseConsumer");
        config.setLookupAddresses(lookups)
                .setConsumerWorkerPoolSize(expectedRdy * 4);
        //so rdy should be 12/2 = 6
        consumer = new ConsumerImplV2(config, new MessageHandler() {
            @Override
            public void process(NSQMessage message) {
                received.incrementAndGet();
                latch.countDown();
            }
        });
        consumer.setAutoFinish(true);
        consumer.subscribe("JavaTesting-Producer-Base");
        consumer.start();
        try {
            Assert.assertTrue(latch.await(1, TimeUnit.MINUTES));
            Thread.sleep(20000);
            //verify rdy
            ConsumerImplV2 consumerImpl = (ConsumerImplV2)consumer;
            Assert.assertEquals(consumerImpl.getRdyPerConnection(), expectedRdy);
            ConnectionManager conMgr = consumerImpl.getConnectionManager();
            ConnectionManager.NSQConnectionWrapper connWrapper = conMgr.getSubscribeConnections("JavaTesting-Producer-Base").iterator().next();
            int currentRdy = connWrapper.getConn().getCurrentRdyCount();
            int expectedRdyReal = connWrapper.getConn().getExpectedRdy();
            Assert.assertEquals(expectedRdy, currentRdy);
            Assert.assertEquals(expectedRdy, expectedRdyReal);
        }finally {
            consumer.close();
            logger.info("Consumer received {} messages.", received.get());
            TopicUtil.emptyQueue("http://" + adminHttp, "JavaTesting-Producer-Base", "BaseConsumer");
        }
    }

    public void test() throws Exception {
        final CountDownLatch latch = new CountDownLatch(10);
        final AtomicInteger received = new AtomicInteger(0);
        final String msgStr = "The quick brown fox jumps over the lazy dog, 那只迅捷的灰狐狸跳过了那条懒狗";
        consumer = new ConsumerImplV2(config, new MessageHandler() {
            @Override
            public void process(NSQMessage message) {
                logger.info("Message received: " + message.getReadableContent());
                org.testng.Assert.assertTrue(message.getReadableContent().startsWith(msgStr));
                logger.info("From topic {}", message.getTopicInfo());
                Assert.assertNotNull(message.getTopicInfo());
                received.incrementAndGet();
                latch.countDown();
            }
        });
        consumer.setAutoFinish(true);
        consumer.subscribe("JavaTesting-Producer-Base");
        consumer.start();
        try {
            Assert.assertTrue(latch.await(1, TimeUnit.MINUTES));
            Thread.sleep(100);
        }finally {
            consumer.close();
            logger.info("Consumer received {} messages.", received.get());
            TopicUtil.emptyQueue("http://" + adminHttp, "JavaTesting-Producer-Base", "BaseConsumer");
        }
    }

    public void testMpub() throws Exception {
        final CountDownLatch latch = new CountDownLatch(30);
        final AtomicInteger received = new AtomicInteger(0);
        final String msgStr = "The quick brow";
        consumer = new ConsumerImplV2(config, new MessageHandler() {
            @Override
            public void process(NSQMessage message) {
                logger.info("Message received: " + message.getReadableContent());
                org.testng.Assert.assertTrue(message.getReadableContent().startsWith(msgStr));
                Assert.assertNotNull(message.getTopicInfo());
                received.incrementAndGet();
                latch.countDown();
            }
        });
        consumer.setAutoFinish(true);
        consumer.subscribe("JavaTesting-Producer-Base");
        consumer.start();
        try {
            Assert.assertTrue(latch.await(1, TimeUnit.MINUTES));
            Thread.sleep(100);
        }finally {
            consumer.close();
            logger.info("Consumer received {} messages.", received.get());
            TopicUtil.emptyQueue("http://" + adminHttp, "JavaTesting-Producer-Base", "BaseConsumer");
        }
    }

    public void testSnappy() throws Exception {
        final CountDownLatch latch = new CountDownLatch(10);
        final AtomicInteger received = new AtomicInteger(0);
        config.setCompression(NSQConfig.Compression.SNAPPY);
        final String msgStr = "The quick brown fox jumps over the lazy dog, 那只迅捷的灰狐狸跳过了那条懒狗";
        consumer = new ConsumerImplV2(config, new MessageHandler() {
            @Override
            public void process(NSQMessage message) {
                logger.info("Message received: " + message.getReadableContent());
                org.testng.Assert.assertTrue(message.getReadableContent().startsWith(msgStr));
                logger.info("From topic {}", message.getTopicInfo());
                Assert.assertNotNull(message.getTopicInfo());
                received.incrementAndGet();
                latch.countDown();
            }
        });
        consumer.setAutoFinish(true);
        consumer.subscribe("JavaTesting-Producer-Base");
        consumer.start();
        try {
            Assert.assertTrue(latch.await(2, TimeUnit.MINUTES));
            Thread.sleep(100);
        }finally {
            consumer.close();
            logger.info("Consumer received {} messages.", received.get());
            TopicUtil.emptyQueue("http://" + adminHttp, "JavaTesting-Producer-Base", "BaseConsumer");
        }
    }

    public void testDeflate() throws Exception {
        final CountDownLatch latch = new CountDownLatch(10);
        final AtomicInteger received = new AtomicInteger(0);
        config.setCompression(NSQConfig.Compression.DEFLATE);
        config.setDeflateLevel(3);
        final String msgStr = "The quick brown fox jumps over the lazy dog, 那只迅捷的灰狐狸跳过了那条懒狗";
        consumer = new ConsumerImplV2(config, new MessageHandler() {
            @Override
            public void process(NSQMessage message) {
                logger.info("Message received: " + message.getReadableContent());
                org.testng.Assert.assertTrue(message.getReadableContent().startsWith(msgStr));
                logger.info("From topic {}", message.getTopicInfo());
                Assert.assertNotNull(message.getTopicInfo());
                received.incrementAndGet();
                latch.countDown();
            }
        });
        consumer.setAutoFinish(true);
        consumer.subscribe("JavaTesting-Producer-Base");
        consumer.start();
        try {
            Assert.assertTrue(latch.await(2, TimeUnit.MINUTES));
            Thread.sleep(100);
        }finally {
            consumer.close();
            logger.info("Consumer received {} messages.", received.get());
            TopicUtil.emptyQueue("http://" + adminHttp, "JavaTesting-Producer-Base", "BaseConsumer");
        }
    }

}
