package util;

public class BasicPair<T1, T2> implements Pair<T1, T2> {
    private T1 first;
    private T2 second;

    public BasicPair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public T1 getFirst() {
        return first;
    }

    @Override
    public T2 getSecond() {
        return second;
    }
}
