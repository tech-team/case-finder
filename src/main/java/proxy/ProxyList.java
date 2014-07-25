package proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public abstract class ProxyList {
    private static List<ProxyInfo> proxies = new ArrayList<>();
    private static final Object proxiesMutex = new Object();
    private static boolean proxiesLoaded = false;
    private static Random rnd = new Random();
    private static int lastSliceBound = 0;
    private static int currentId = 0;
    private static final int RATING_THRESHOLD = 3;

    public static void loadNewList(List<ProxyInfo> list) {
        if (list != null) {
            synchronized (proxiesMutex) {
                proxies = list;
                for (int i = proxies.size() - 1; i >= 0; --i) {
                    if (proxies.get(i).getRating() < RATING_THRESHOLD) {
                        proxies.remove(i);
                    }
                }
                System.out.println("Proxies count: " + proxies.size());
                currentId = 0;
                proxiesLoaded = true;
            }
        }
    }

    public static boolean proxiesLoaded() {
        return proxiesLoaded;
    }

    public static ProxyInfo getNext() {
        System.out.println("currentProxyId = " + currentId);
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
    public static ProxyInfo getAt(int index) {
        synchronized (proxiesMutex) {
            return proxies.get(index);
        }
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
