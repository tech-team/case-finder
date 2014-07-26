package util;

import java.util.Collection;

public interface Appendable<T> {
    void append(T obj);
    Collection<T> getCollection();
    void setTotalCount(int count);
    int getTotalCount();
}
