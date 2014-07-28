package proxy;

import util.MyLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.logging.Logger;

public class ProxyList {
    private static ProxyList instance = null;
    private static final int RATING_THRESHOLD = 3;

    private ProxyUpdater proxyUpdater = new ProxyUpdater();
    private List<ProxyInfo> proxies = new ArrayList<>();
    private boolean proxiesLoaded = false;
    private int currentId = 0;
    private Logger logger = MyLogger.getLogger(this.getClass().toString());

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
            for (int i = proxies.size() - 1; i >= 0; --i) {
                if (proxies.get(i).getRating() < RATING_THRESHOLD) {
                    proxies.remove(i);
                }
            }
            logger.info("Proxies count: " + proxies.size());
            currentId = 0;
            proxiesLoaded = true;
        }
    }

    public boolean proxiesLoaded() {
        return proxiesLoaded;
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

    public synchronized ProxyInfo getNext() throws InterruptedException {
        waitForProxiesLoaded();
        logger.fine("currentProxyId = " + currentId);
        ProxyInfo proxyInfo = getAt(currentId);
        currentId += 1;
        int size = proxies.size();
        if (currentId >= size)
            currentId -= size;
        return proxyInfo;
    }

    public synchronized ProxyInfo getAt(int index) {
        return proxies.get(index);
    }

}
