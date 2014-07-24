package proxy;

public class ProxyInfo {
    private String ip;
    private int port;
    private String country;
    private int rating = 0;
    private int workingPerc = 0;
    private int responseTimePerc = 0;
    private int downloadSpeedPerc = 0;

    public ProxyInfo(String ip, int port, String country, int rating, int workingPerc, int responseTimePerc, int downloadSpeedPerc) {
        this.ip = ip;
        this.port = port;
        this.country = country;
        this.rating = rating;
        this.workingPerc = workingPerc;
        this.responseTimePerc = responseTimePerc;
        this.downloadSpeedPerc = downloadSpeedPerc;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getWorkingPerc() {
        return workingPerc;
    }

    public void setWorkingPerc(int workingPerc) {
        this.workingPerc = workingPerc;
    }

    public int getResponseTimePerc() {
        return responseTimePerc;
    }

    public void setResponseTimePerc(int responseTimePerc) {
        this.responseTimePerc = responseTimePerc;
    }

    public int getDownloadSpeedPerc() {
        return downloadSpeedPerc;
    }

    public void setDownloadSpeedPerc(int downloadSpeedPerc) {
        this.downloadSpeedPerc = downloadSpeedPerc;
    }
}
