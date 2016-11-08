package com.youzan.nsq.client.entity;

public class Address implements java.io.Serializable, Comparable<Address> {
    private static final long serialVersionUID = -5091525135068063293L;

    private final String host;
    private final int port;

    //version number, used to specify old nsq and new
    private final String version;
    private Boolean isHA = null;

    /**
     *
     * @param host
     * @param port
     */
    public Address(String host, String port, String version) {
        this.host = host;
        this.port = Integer.valueOf(port);
        this.version = version;
    }

    /**
     * @param host a {@link String}
     * @param port a integer number
     */
    public Address(String host, int port, String version) {
        this.host = host;
        this.port = port;
        this.version = version;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getVersion(){
        return this.version;
    }

    /**
     * check if version # ends with HA.*
     * @return {@link Boolean#TRUE} if NSQd has HA capability within.
     */
    public boolean isHA(){
        if(null != isHA)
            return isHA;
        else
            if(this.version.contains("-HA."))
                return isHA = Boolean.TRUE;
            else return isHA = Boolean.FALSE;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

    @Override
    public int compareTo(Address o2) {
        if (null == o2) {
            return 1;
        }
        final Address o1 = this;
        final int hostComparator = o1.host.compareTo(o2.getHost());
        return hostComparator == 0 ? o1.port - o2.port : hostComparator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Address other = (Address) obj;
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        return port == other.port;
    }

}
