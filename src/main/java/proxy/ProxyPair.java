package proxy;

public class ProxyPair {
    private String ip;
    private int port;
    private String country;

    ProxyPair(String ip, int port, String country) {
        this.ip = ip;
        this.port = port;
        this.country = country;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
