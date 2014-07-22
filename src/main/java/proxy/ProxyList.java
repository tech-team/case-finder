package proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class ProxyList {
    private static List<ProxyPair> proxies = new ArrayList<>();
    private static Random rnd = new Random();
    private static int lastSliceBound = 0;

    public static void loadNewList(List<ProxyPair> list) {
        proxies = list;
    }

    public static ProxyPair getFirst() {
        return getAt(0);
    }

    public static ProxyPair getAt(int index) {
        return proxies.get(index);
    }

    public static ProxyPair getRandom() {
        return getAt(rnd.nextInt(proxies.size()));
    }

    public static List<ProxyPair> getBulk(int bulkSize) {
        int lastIndex = lastSliceBound + bulkSize + 1;
        List<ProxyPair> bulk = proxies.subList(lastSliceBound, lastIndex);
        lastSliceBound = lastIndex;
        return bulk;
    }
}
