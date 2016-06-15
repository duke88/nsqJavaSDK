package com.youzan.nsq.client.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.nsq.client.exception.NSQException;
import com.youzan.util.HostUtil;
import com.youzan.util.IPUtil;
import com.youzan.util.NotThreadSafe;
import com.youzan.util.SystemUtil;

import io.netty.handler.ssl.SslContext;

/**
 * One config to One cluster with specific topic. <br />
 * It is used for Producer or Consumer, and not both two.
 * 
 * 
 * @author zhaoxi (linzuxiong)
 * @email linzuxiong1988@gmail.com
 *
 */
@NotThreadSafe
public class NSQConfig implements java.io.Serializable {

    private static final long serialVersionUID = 6624842850216901700L;
    private static final Logger logger = LoggerFactory.getLogger(NSQConfig.class);

    private boolean havingMonitoring = false;

    public enum Compression {
        NO_COMPRESSION, DEFLATE, SNAPPY
    }

    /**
     * One lookup cluster
     */
    private String lookupAddresses;

    /**
     * Perform one action during specified timeout
     */
    private int timeoutInSecond = 1;
    private String topic;
    /**
     * In NSQ, it is a channel.
     */
    private String consumerName;
    /**
     * The set of messages is ordered in one specified partition
     */
    private boolean ordered = true;
    /**
     * <pre>
     * Set the thread_pool_size for IO running.
     * It is also used for Netty.
     * In recommended, the size is (CPUs - 1) and bind CPU affinity.
     * </pre>
     */
    private int threadPoolSize4IO = Runtime.getRuntime().availableProcessors() - 1;
    private final String clientId;
    private final String hostname;
    private boolean featureNegotiation;

    private int msgTimeoutInMillisecond = 60 * 1000;
    private Integer heartbeatIntervalInMillisecond = null;

    private Integer outputBufferSize = null;
    private Integer outputBufferTimeoutInMillisecond = null;
    private boolean tlsV1 = false;
    private Integer deflateLevel = null;
    private Integer sampleRate = null;
    private final String userAgent = "Java/com.youzan/nsq-client/2.0.0615-RELEASE";
    private Compression compression = Compression.NO_COMPRESSION;
    // ...
    private SslContext sslContext = null;

    public NSQConfig() throws NSQException {
        try {
            hostname = HostUtil.getLocalIP();
            // JDK8, string contact is OK.
            clientId = "IP:" + IPUtil.ipv4(hostname) + ", PID:" + SystemUtil.getPID();
        } catch (Exception e) {
            throw new NSQException("System cann't get the IPv4!", e);
        }
    }

    /**
     * One lookup cluster
     * 
     * @return the lookupAddresses
     */
    public String getLookupAddresses() {
        return lookupAddresses;
    }

    /**
     * @param lookupAddresses
     *            the lookupAddresses to set
     */
    public void setLookupAddresses(String lookupAddresses) {
        this.lookupAddresses = lookupAddresses;
    }

    /**
     * @return the timeoutInSecond
     */
    public int getTimeoutInSecond() {
        return timeoutInSecond;
    }

    /**
     * @param timeoutInSecond
     *            the timeoutInSecond to set
     */
    public void setTimeoutInSecond(int timeoutInSecond) {
        this.timeoutInSecond = timeoutInSecond;
    }

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @param topic
     *            the topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * @return the consumerName
     */
    public String getConsumerName() {
        return consumerName;
    }

    /**
     * @param consumerName
     *            the consumerName to set
     */
    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    /**
     * @return the ordered
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * @param ordered
     *            the ordered to set
     */
    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    /**
     * @return the threadPoolSize4IO
     */
    public int getThreadPoolSize4IO() {
        return threadPoolSize4IO;
    }

    /**
     * @param threadPoolSize4IO
     *            the threadPoolSize4IO to set
     */
    public void setThreadPoolSize4IO(int threadPoolSize4IO) {
        this.threadPoolSize4IO = threadPoolSize4IO;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return the msgTimeoutInMillisecond
     */
    public int getMsgTimeoutInMillisecond() {
        return msgTimeoutInMillisecond;
    }

    /**
     * @param msgTimeoutInMillisecond
     *            the msgTimeoutInMillisecond to set
     */
    public void setMsgTimeoutInMillisecond(int msgTimeoutInMillisecond) {
        this.msgTimeoutInMillisecond = msgTimeoutInMillisecond;
    }

    /**
     * @return the heartbeatIntervalInMillisecond
     */
    public Integer getHeartbeatIntervalInMillisecond() {
        if (heartbeatIntervalInMillisecond == null) {
            return Integer.valueOf(getMsgTimeoutInMillisecond() / 3);
        }
        return heartbeatIntervalInMillisecond;
    }

    /**
     * @param heartbeatIntervalInMillisecond
     *            the heartbeatIntervalInMillisecond to set
     */
    public void setHeartbeatIntervalInMillisecond(Integer heartbeatIntervalInMillisecond) {
        this.heartbeatIntervalInMillisecond = heartbeatIntervalInMillisecond;
    }

    /**
     * @return the featureNegotiation
     */
    public boolean isFeatureNegotiation() {
        return featureNegotiation;
    }

    /**
     * @param featureNegotiation
     *            the featureNegotiation to set
     */
    public void setFeatureNegotiation(boolean featureNegotiation) {
        this.featureNegotiation = featureNegotiation;
    }

    /**
     * @return the outputBufferSize
     */
    public Integer getOutputBufferSize() {
        return outputBufferSize;
    }

    /**
     * @param outputBufferSize
     *            the outputBufferSize to set
     */
    public void setOutputBufferSize(Integer outputBufferSize) {
        this.outputBufferSize = outputBufferSize;
    }

    /**
     * @return the outputBufferTimeoutInMillisecond
     */
    public Integer getOutputBufferTimeoutInMillisecond() {
        return outputBufferTimeoutInMillisecond;
    }

    /**
     * @param outputBufferTimeoutInMillisecond
     *            the outputBufferTimeoutInMillisecond to set
     */
    public void setOutputBufferTimeoutInMillisecond(Integer outputBufferTimeoutInMillisecond) {
        this.outputBufferTimeoutInMillisecond = outputBufferTimeoutInMillisecond;
    }

    /**
     * @return the userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * @return the havingMonitoring
     */
    public boolean isHavingMonitoring() {
        return havingMonitoring;
    }

    /**
     * @param havingMonitoring
     *            the havingMonitoring to set
     */
    public void setHavingMonitoring(boolean havingMonitoring) {
        this.havingMonitoring = havingMonitoring;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SslContext sslContext) {
        if (null == sslContext) {
            throw new NullPointerException();
        }
        tlsV1 = true;
        this.sslContext = sslContext;
    }

    /**
     * @return the tlsV1
     */
    public boolean isTlsV1() {
        return tlsV1;
    }

    /**
     * @param tlsV1
     *            the tlsV1 to set
     */
    public void setTlsV1(boolean tlsV1) {
        this.tlsV1 = tlsV1;
    }

    /**
     * @return the compression
     */
    public Compression getCompression() {
        return compression;
    }

    /**
     * @param compression
     *            the compression to set
     */
    public void setCompression(Compression compression) {
        this.compression = compression;
    }

    /**
     * @return the deflateLevel
     */
    public Integer getDeflateLevel() {
        return deflateLevel;
    }

    /**
     * @param deflateLevel
     *            the deflateLevel to set
     */
    public void setDeflateLevel(Integer deflateLevel) {
        this.deflateLevel = deflateLevel;
    }

    /**
     * @return the sampleRate
     */
    public Integer getSampleRate() {
        return sampleRate;
    }

    /**
     * @param sampleRate
     *            the sampleRate to set
     */
    public void setSampleRate(Integer sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String identify() {
        final StringBuffer buffer = new StringBuffer(300);
        buffer.append("{\"client_id\":\"" + clientId + "\", ");
        buffer.append("\"hostname\":\"" + hostname + "\", ");
        buffer.append("\"feature_negotiation\": true, ");
        if (outputBufferSize != null) {
            buffer.append("\"output_buffer_size\":" + outputBufferSize + ", ");
        }
        if (outputBufferTimeoutInMillisecond != null) {
            buffer.append("\"output_buffer_timeout\":" + outputBufferTimeoutInMillisecond.toString() + ", ");
        }
        if (tlsV1) {
            buffer.append("\"tls_v1\":" + tlsV1 + ", ");
        }
        if (compression == Compression.SNAPPY) {
            buffer.append("\"snappy\": true, ");
        }
        if (compression == Compression.DEFLATE) {
            buffer.append("\"deflate\": true, ");
            if (deflateLevel != null) {
                buffer.append("\"deflate_level\":" + deflateLevel.toString() + ", ");
            }
        }
        if (sampleRate != null) {
            buffer.append("\"sample_rate\":" + sampleRate.toString() + ",");
        }
        if (getHeartbeatIntervalInMillisecond() != null) {
            buffer.append("\"heartbeat_interval\":" + String.valueOf(getHeartbeatIntervalInMillisecond()) + ", ");
        }
        buffer.append("\"msg_timeout\":" + String.valueOf(msgTimeoutInMillisecond) + ",");
        buffer.append("\"user_agent\": \"" + userAgent + "\"}");
        return buffer.toString();
    }
}