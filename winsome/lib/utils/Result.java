package winsome.lib.utils;

/**
 * Class that implemets a result type, that is a specific case
 * of a coproduct.
 * This is loosely inspired by https://doc.rust-lang.org/std/result/
 */
public class Result<T, E> {
    private T okValue;
    private E errValue;

    static public <T, E> Result<T, E> ok(T okValue) {
        var res = new Result<T, E>();
        res.setOkValue(okValue);
        return res;
    }

    /**
     * error constructor
     * 
     * @param errValue error value
     * @return an error result
     */
    static public <T, E> Result<T, E> err(E errValue) {
        var res = new Result<T, E>();
        res.setErrValue(errValue);
        return res;
    }

    /**
     * get the ok value
     * 
     * @return the ok value, null if the result is an error
     */
    public T getOkValue() {
        return okValue;
    }

    /**
     * Set the ok value
     * 
     * @param okValue
     */
    public void setOkValue(T okValue) {
        this.okValue = okValue;
        this.errValue = null;
    }

    /**
     * get error value
     * 
     * @return the error value, null if the result is an ok
     */
    public E getErrValue() {
        return errValue;
    }

    /**
     * Set error value
     * 
     * @param errValue
     */
    public void setErrValue(E errValue) {
        this.errValue = errValue;
        this.okValue = null;
    }

    /**
     * @return true if the result is ok
     */
    public boolean isOk() {
        return this.okValue != null;
    }

    /**
     * @return true if result is err
     */
    public boolean isErr() {
        return this.errValue != null;
    }

}
