package proxy;

import util.MyLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class ProxyList {
    private static ProxyList instance = null;

    private final ProxyUpdater proxyUpdater = new ProxyUpdater();
    private List<ProxyInfo> proxies = new ArrayList<>();
    private PriorityQueue<ProxyInfo> googleProxies = new PriorityQueue<>();
    private boolean proxiesLoaded = false;
    private boolean googleProxiesLoaded = false;
    private int currentId = 0;
    private final Object proxyLock = new Object();
    private final Object googleProxyLock = new Object();

    private int proxiesEra = 0;
    private int googleProxiesEra = 0;

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
            proxiesLoaded = false;
            synchronized (proxyLock) {
                proxiesEra += 1;

                proxies = new ArrayList<>();
                for (ProxyInfo proxy : list) {
                    proxy.setEra(proxiesEra);
                    proxies.add(proxy);
                }
                logger.info("Proxies count: " + proxies.size());
                currentId = 0;
                proxiesLoaded = true;
            }
        }
    }

    public void loadNewGoogleList(List<ProxyInfo> list) {
        if (list != null) {
            googleProxiesLoaded = false;
            synchronized (googleProxyLock) {
                googleProxiesEra += 1;

                googleProxies.clear();
                for (ProxyInfo proxy : list) {
                    proxy.setEra(googleProxiesEra);
                    googleProxies.add(proxy);
                }
                logger.info("Google Proxies count: " + googleProxies.size());
                googleProxiesLoaded = true;
            }
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
        waitForProxiesLoaded();
        synchronized (proxyLock) {
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
        waitForGoogleProxiesLoaded();
            synchronized (googleProxyLock) {
            return googleProxies.poll();
        }
    }

    public void returnGoogleProxy(ProxyInfo proxy) throws InterruptedException {
        waitForGoogleProxiesLoaded();
        synchronized (googleProxyLock) {
            if (proxy.getEra() == googleProxiesEra) {
                googleProxies.add(proxy);
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        ProxyList.instance().waitForProxiesLoaded();
    }
}
