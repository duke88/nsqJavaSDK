package com.youzan.nsq.client.entity.lookup;

import com.fasterxml.jackson.databind.JsonNode;
import com.youzan.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * SeedLookupdAddress
 * Created by lin on 16/12/5.
 */
public class SeedLookupdAddress extends AbstractLookupdAddress {
    private static ConcurrentHashMap<String, SeedLookupdAddress> seedLookupMap = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(SeedLookupdAddress.class);

    private static final String HTTP_PRO_HEAD = "http://";
    private volatile int INDEX = 0;

    private String clusterId;
    private AtomicLong refCounter = new AtomicLong(0);
    //lookupAddresses fetched from listlookup API
    private ReentrantReadWriteLock lookupAddressLock = new ReentrantReadWriteLock();
    private List<SoftReference<LookupdAddress>> lookupAddressesRefs;
    private Set<String> lookupAddressInUse;

    protected SeedLookupdAddress(String address) {
        super(address, address);
        //TODO: do we really need cluster info
        this.clusterId = address;
        lookupAddressesRefs = new ArrayList<>();
        lookupAddressInUse = new HashSet<>();
    }

    public String getClusterId() {
        return this.clusterId;
    }

    /**
     * Fetch lookup addresses of current {@link SeedLookupdAddress}. Update references to lookupaddresses
     */
    private void listLookup(boolean force) throws IOException {
        try {
            this.lookupAddressLock.readLock().lock();
            if (!force && this.lookupAddressesRefs.size() > 0)
                return;
        } finally {
            this.lookupAddressLock.readLock().unlock();
        }

        String seed = this.getAddress();
        if (!seed.startsWith(HTTP_PRO_HEAD))
            seed = HTTP_PRO_HEAD + seed;
        final String url = String.format("%s/listlookup", seed);
        if (logger.isDebugEnabled())
            logger.debug("Begin to get the new lookup servers. From URL: {}", url);
        JsonNode tmpRootNode = null;
        final List<String> newLookups = new ArrayList<>();
        URL lookupUrl;
        try {
            lookupUrl = new URL(url);
            tmpRootNode = IOUtil.readFromUrl(lookupUrl);
        } catch (ConnectException ce) {
            _handleConnectionTimeout(seed, ce);
        } catch (FileNotFoundException e) {
            //add seed lookup directlly as lookup address
            //TODO: choose a clusterId
            LookupdAddress aLookup = LookupdAddress.create(seed, seed);
            this.addLookupdAddress(aLookup);
            logger.info("You run with lower server version, current seed lookup address will be added directly as lookup address");
            return;
        }
        final JsonNode nodes = tmpRootNode.get("lookupdnodes");
        if (null == nodes) {
            logger.error("NSQ listlookup responses without any lookup servers for seed lookup address: {}.", seed);
            return;
        }
        for (JsonNode node : nodes) {
            final String host = node.get("NodeIP").asText();
            final int port = node.get("HttpPort").asInt();
            final String address = host + ":" + port;
            newLookups.add(address);
        }
        String[] newLookupsArr = newLookups.toArray(new String[0]);
        this.addLookupdAddresses(newLookupsArr);
        if (logger.isDebugEnabled())
            logger.debug("Recently have got the lookup servers: {} from seed lookup: {}", newLookupsArr, seed);
    }

    private void _handleConnectionTimeout(String lookup, ConnectException ce) throws IOException {
        String ip = "EMPTY", address = "EMPTY";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();//ip where sdk resides
            address = addr.getHostName();//address where sdk resides
        } catch (Exception e) {
            logger.error("Could not fetch ip or address form local client, should not occur.", e);
        }
        logger.warn("Fail to connect to NSQ lookup. SDK Client, ip:{} address:{}. Remote lookup:{}. Will kick off another try in another round.", ip, address, lookup);
        logger.warn("Nested connection exception stacktrace:", ce);
    }

    private long updateRefCounter(long delta) {
        if (delta > 0) {
            return this.refCounter.incrementAndGet();
        } else if (delta < 0) {
            return this.refCounter.decrementAndGet();
        }
        return this.refCounter.get();
    }

    /**
     * add one {@link LookupdAddress} to current {@link SeedLookupdAddress} object, reference to passin lookup address
     * updated in function;
     *
     * @param lookupd
     */
    void addLookupdAddress(final LookupdAddress lookupd) {
        try {
            this.lookupAddressLock.writeLock().lock();
            if (!lookupAddressInUse.contains(lookupd.getAddress())) {
                this.lookupAddressesRefs.add(new SoftReference<>(lookupd));
                lookupd.addReference();
                lookupAddressInUse.add(lookupd.getAddress());
            }
        } finally {
            this.lookupAddressLock.writeLock().unlock();
        }
    }

    /**
     * add passin lookupd addresses string to current {@link SeedLookupdAddress} object.
     *
     * @param lookupds
     */
    private void addLookupdAddresses(String... lookupds) {
        try {
            lookupAddressLock.writeLock().lock();
            for (String aLookupStr : lookupds) {
                if (!lookupAddressInUse.contains(aLookupStr)) {
                    LookupdAddress aLookup = LookupdAddress.create(this.getClusterId(), aLookupStr);
                    this.lookupAddressesRefs.add(new SoftReference<>(aLookup));
                    aLookup.addReference();
                    lookupAddressInUse.add(aLookupStr);
                }
            }
        } finally {
            lookupAddressLock.writeLock().unlock();
        }
    }

    /**
     * remove all {@link LookupdAddress} under current SeedLookup address
     */
    private void removeAllLookupdAddress() {
        try {
            this.lookupAddressLock.writeLock().lock();
            for (SoftReference<LookupdAddress> lookupdRef : this.lookupAddressesRefs) {
                LookupdAddress aLookupd = lookupdRef.get();
                if (null != aLookupd) {
                    aLookupd.removeReference();
                }
            }
            this.lookupAddressInUse.clear();
        } finally {
            this.lookupAddressLock.writeLock().unlock();
        }

    }

    public static SeedLookupdAddress create(String address) {
        SeedLookupdAddress aSeed = new SeedLookupdAddress(address);
        if (!seedLookupMap.containsKey(address)) {
            seedLookupMap.put(address, aSeed);
        } else {
            aSeed = seedLookupMap.get(address);
        }
        return aSeed;
    }

    /**
     * Add one reference count to current {@link SeedLookupdAddress} object
     *
     * @param aSeed
     * @return
     */
    static long addReference(SeedLookupdAddress aSeed) {
        if (seedLookupMap.containsValue(aSeed)) {
            synchronized (aSeed) {
                return aSeed.updateRefCounter(1);
            }
        }
        return -1;
    }

    /**
     * Decrease one reference count to current {@link SeedLookupdAddress}, if updated reference count is smaller than 1,
     * current SeedLookupdAddress object will be removed.
     *
     * @param aSeed
     * @return
     */
    static long removeReference(SeedLookupdAddress aSeed) {
        if (seedLookupMap.containsValue(aSeed)) {
            synchronized (aSeed) {
                if (seedLookupMap.containsValue(aSeed)) {
                    long cnt = aSeed.updateRefCounter(-1);
                    if (cnt <= 0) {
                        seedLookupMap.remove(aSeed.getAddress());
                        aSeed.clean();
                        return 0;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * pubch out one lookup address
     *
     * @return
     */
    public String punchLookupdAddressStr() {
        try {
            this.listLookup(false);
        } catch (IOException e) {
            logger.error("Fail to get lookup address for seed lookup address: {}.", this.getAddress());
        }

        LookupdAddress lookupdAddress = null;
        try {
            this.lookupAddressLock.readLock().lock();
            if (this.lookupAddressesRefs.size() > 0)
                lookupdAddress = this.lookupAddressesRefs.get((INDEX++ & Integer.MAX_VALUE) % this.lookupAddressesRefs.size()).get();
        } finally {
            this.lookupAddressLock.readLock().unlock();
        }
        if (null != lookupdAddress)
            return lookupdAddress.getAddress();
        return null;
    }

    long getReferenceCount() {
        return this.refCounter.get();
    }

    void clean() {
        this.removeAllLookupdAddress();
    }

    public static void listAllLookup() {
        long start = 0L;
        if(logger.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }
        for (SeedLookupdAddress aSeed : seedLookupMap.values()) {
            try {
                aSeed.listLookup(true);
            } catch (IOException e) {
                logger.error("Fail to get lookup address for seed lookup address: {}.", aSeed.getAddress());
            }
        }
        if(logger.isDebugEnabled()) {
            logger.debug("Time eclapse: {} millsec for all seed lookup addresses to listLookup, size {}", System.currentTimeMillis() - start, seedLookupMap.size());
        }
    }
}