package proxy;

import util.MyLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProxyList {
    private static ProxyList instance = null;

    private final ProxyUpdater proxyUpdater = new ProxyUpdater();
    private List<ProxyInfo> proxies = new ArrayList<>();
    private List<ProxyInfo> googleProxies = new ArrayList<>();
    private boolean proxiesLoaded = false;
    private boolean googleProxiesLoaded = false;
    private int currentId = 0;
    private int currentGoogleId = 0;
    private final Object proxyLock = new Object();
    private final Object googleProxyLock = new Object();
    private final Logger logger = MyLogger.getLogger(this.getClass().toString());

    private ProxyList() {
    }

    public static ProxyList instance() {
        if (instance == null) {
            instance = new ProxyList();
            instance.proxyUpdater.run(instance);
        }
        return instance;
    }

    public void loadNewList(List<ProxyInfo> list) {
        if (list != null) {
            proxies = list;
            logger.info("Proxies count: " + proxies.size());
            currentId = 0;
            proxiesLoaded = true;
        }
    }

    public void loadNewGoogleList(List<ProxyInfo> list) {
        if (list != null) {
            googleProxies = list;
            logger.info("Google Proxies count: " + googleProxies.size());
            currentGoogleId = 0;
            googleProxiesLoaded = true;
        }
    }

    public boolean proxiesLoaded() {
        return proxiesLoaded;
    }

    public boolean googleProxiesLoaded() {
        return googleProxiesLoaded;
    }

    public void waitForProxiesLoaded() throws InterruptedException {
        if (!proxiesLoaded()) {
            logger.info("Waiting for proxies to load");
            while (!proxiesLoaded()) {
                Thread.sleep(100);
            }
            logger.info("Proxies loaded");
        }
    }

    public void waitForGoogleProxiesLoaded() throws InterruptedException {
        if (!googleProxiesLoaded()) {
            logger.info("Waiting for Google proxies to load");
            while (!googleProxiesLoaded()) {
                Thread.sleep(100);
            }
            logger.info("Google proxies loaded");
        }
    }

    public ProxyInfo getNext() throws InterruptedException {
        synchronized (proxyLock) {
            waitForProxiesLoaded();
            logger.fine("currentProxyId = " + currentId);
            ProxyInfo proxyInfo = proxies.get(currentId);
            currentId += 1;
            int size = proxies.size();
            if (currentId >= size)
                currentId -= size;
            return proxyInfo;
        }
    }

    public ProxyInfo getGoogleNext() throws InterruptedException {
        synchronized (googleProxyLock) {
            waitForGoogleProxiesLoaded();
            logger.fine("currentGoogleProxyId = " + currentGoogleId);
            ProxyInfo proxyInfo = googleProxies.get(currentGoogleId);
            currentGoogleId += 1;
            int size = googleProxies.size();
            if (currentGoogleId >= size)
                currentGoogleId -= size;
            return proxyInfo;
        }
    }


    public static void main(String[] args) throws InterruptedException {
        ProxyList.instance().waitForProxiesLoaded();
    }
}
