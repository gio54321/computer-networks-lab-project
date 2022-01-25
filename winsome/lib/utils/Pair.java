package winsome.lib.utils;

public class Pair<T, E> {
    private T first;
    private E second;

    public Pair(T first, E second) {
        this.first = first;
        this.second = second;
    }

    public T first() {
        return this.first;
    }

    public E second() {
        return this.second;
    }

}
