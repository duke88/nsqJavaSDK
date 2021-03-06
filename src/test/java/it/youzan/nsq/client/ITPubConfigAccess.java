package it.youzan.nsq.client;

import com.youzan.nsq.client.*;
import com.youzan.nsq.client.configs.*;
import com.youzan.nsq.client.entity.*;
import com.youzan.nsq.client.exception.ConfigAccessAgentException;
import com.youzan.nsq.client.exception.NSQException;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by lin on 16/12/15.
 */
public class ITPubConfigAccess {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ITPubConfigAccess.class);
    protected final static String controlCnfStr = "{" +
            "\"previous\":[]," +
            "\"current\":[\"http://sqs-qa.s.qima-inc.com:4161\"]," +
            "\"gradation\":{" +
            "\"*\":{\"percent\":10.0}," +
            "\"bc-pifa0\":{\"percent\":10.0}," +
            "\"bc-pifa1\":{\"percent\":20.0}," +
            "\"bc-pifa2\":{\"percent\":30.0}" +
            "}" +
            "}";

    private ConfigAccessAgent agent;
    private Properties props = new Properties();

    @BeforeClass
    public void init() throws IOException {
        logger.info("init of [ITPubConfigAccess].");
        logger.info("Initialize ConfigAccessAgent from system specified config.");
        System.setProperty("nsq.sdk.configFilePath", "src/test/resources/configClientTest.properties");
        //load properties from configClientTest.properties
        InputStream is = getClass().getClassLoader().getResourceAsStream("app-test.properties");
        Properties proTest = new Properties();
        proTest.load(is);
        is.close();
        //initialize system property form config client properties
        logger.info("init of [ConfigAccessAgentTestcase] ends.");
    }

    @Test
    public void testPublish2SQSWithConfigAccess() throws NSQException, InterruptedException, ConfigAccessAgentException {
        //initialize confgi access agent
        agent = ConfigAccessAgent.getInstance();
        final SortedMap<String, String> valueMap = new TreeMap<>();
        valueMap.put("JavaTesting-Migration", controlCnfStr);
        Topic topic = new Topic("JavaTesting-Migration");
        DCCMigrationConfigAccessDomain domain = (DCCMigrationConfigAccessDomain) DCCMigrationConfigAccessDomain.getInstance(topic.getTopicText());
        Role aRole = Role.Producer;
        DCCMigrationConfigAccessKey keyProducer = (DCCMigrationConfigAccessKey) DCCMigrationConfigAccessKey.getInstance(aRole);
        TestConfigAccessAgent.updateValue(domain, new AbstractConfigAccessKey[]{keyProducer}, valueMap, true);

        Role aRoleConsumer = Role.Consumer;
        DCCMigrationConfigAccessKey keyConsumer = (DCCMigrationConfigAccessKey) DCCMigrationConfigAccessKey.getInstance(aRoleConsumer);
        TestConfigAccessAgent.updateValue(domain, new AbstractConfigAccessKey[]{keyConsumer}, valueMap, true);

        NSQConfig config =  new NSQConfig("BaseConsumer");
        Producer producer = new ProducerImplV2(config);
        producer.start();
        Message msg;
        for(int i = 0; i < 10; i++) {
            String msgStr = "Message " + i;
            msg = Message.create(topic, msgStr);
            producer.publish(msg);
        }
        producer.close();

        final CountDownLatch latch = new CountDownLatch(10);
        Consumer consumer = new ConsumerImplV2(config, new MessageHandler() {
            @Override
            public void process(NSQMessage message) {
                logger.info(message.getReadableContent());
                latch.countDown();
            }
        });
        consumer.subscribe(topic);
        consumer.start();
        Assert.assertTrue(latch.await(90, TimeUnit.SECONDS));
        consumer.close();
    }

    @AfterMethod
    public void release() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        System.clearProperty("nsq.sdk.configFilePath");
        Method method = ConfigAccessAgent.class.getDeclaredMethod("release");
        method.setAccessible(true);
        method.invoke(agent);
    }
}
