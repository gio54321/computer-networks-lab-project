package winsome.lib.utils;

public class Result<T, E> {
    // loosely inspired by https://doc.rust-lang.org/std/result/
    private T okValue;
    private E errValue;

    static public <T, E> Result<T, E> ok(T okValue) {
        var res = new Result<T, E>();
        res.setOkValue(okValue);
        return res;
    }

    static public <T, E> Result<T, E> err(E okValue) {
        var res = new Result<T, E>();
        res.setErrValue(okValue);
        return res;
    }

    public T getOkValue() {
        return okValue;
    }

    public void setOkValue(T okValue) {
        this.okValue = okValue;
        this.errValue = null;
    }

    public E getErrValue() {
        return errValue;
    }

    public void setErrValue(E errValue) {
        this.errValue = errValue;
        this.okValue = null;
    }

    public boolean isOk() {
        return this.okValue != null;
    }

    public boolean isErr() {
        return this.errValue != null;
    }

}
