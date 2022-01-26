package winsome.lib.utils;

/**
 * Class that represent a wrapper over a value.
 * This is usually used to "lift" primitive types as objects.
 */
public class Wrapper<T> {
    private T value;

    public Wrapper(T value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public T getValue() {
        return value;
    }

    /**
     * Set the value
     * 
     * @param value
     */
    public void setValue(T value) {
        this.value = value;
    }

}
