package proxy;

import util.MyLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.logging.Logger;

public abstract class ProxyList {
    private static List<ProxyInfo> proxies = new ArrayList<>();
    private static final Object proxiesMutex = new Object();
    private static boolean proxiesLoaded = false;
    private static Random rnd = new Random();
    private static int lastSliceBound = 0;
    private static int currentId = 0;
    private static final int RATING_THRESHOLD = 3;
    private static Logger logger = MyLogger.getLogger(ProxyList.class.toString());

    public static synchronized void loadNewList(List<ProxyInfo> list) {
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

    public static boolean proxiesLoaded() {
        return proxiesLoaded;
    }

    public static void waitForProxiesLoaded() {
        if (!proxiesLoaded()) {
            logger.info("Waiting for proxies to load");
            while (!ProxyList.proxiesLoaded()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logger.info("Proxies loaded");
        }
    }

    public static synchronized ProxyInfo getNext() {
        waitForProxiesLoaded();
        logger.fine("currentProxyId = " + currentId);
        ProxyInfo proxyInfo = getAt(currentId);
        currentId += 1;
        int size = proxies.size();
        if (currentId >= size)
            currentId -= size;
        return proxyInfo;
    }

//    public static ProxyInfo getFirst() {
//        return getAt(0);
//    }
//
    public static synchronized ProxyInfo getAt(int index) {
        return proxies.get(index);
    }
//
//    public static ProxyInfo getRandom() {
//        return getAt(rnd.nextInt(proxies.size()));
//    }
//
//    public static List<ProxyInfo> getBulk(int bulkSize) {
//        synchronized (proxiesMutex) {
//            int lastIndex = lastSliceBound + bulkSize + 1;
//            List<ProxyInfo> bulk = proxies.subList(lastSliceBound, lastIndex);
//            lastSliceBound = lastIndex;
//            return bulk;
//        }
//    }
}
