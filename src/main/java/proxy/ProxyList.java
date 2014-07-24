package proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class ProxyList {
    private static List<ProxyInfo> proxies = new ArrayList<>();
    private static final Object proxiesMutex = new Object();
    private static boolean proxiesLoaded = false;
    private static Random rnd = new Random();
    private static int lastSliceBound = 0;

    public static void loadNewList(List<ProxyInfo> list) {
        synchronized (proxiesMutex) {
            proxies = list;
            proxiesLoaded = true;
        }
    }

    public static boolean proxiesLoaded() {
        return proxiesLoaded;
    }

    public static ProxyInfo getFirst() {
        return getAt(0);
    }

    public static ProxyInfo getAt(int index) {
        return proxies.get(index);
    }

    public static ProxyInfo getRandom() {
        return getAt(rnd.nextInt(proxies.size()));
    }

    public static List<ProxyInfo> getBulk(int bulkSize) {
        synchronized (proxiesMutex) {
            int lastIndex = lastSliceBound + bulkSize + 1;
            List<ProxyInfo> bulk = proxies.subList(lastSliceBound, lastIndex);
            lastSliceBound = lastIndex;
            return bulk;
        }
    }
}
