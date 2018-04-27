package io.thorntail.migrate;

/**
 * Created by bob on 3/13/18.
 */
public interface Action<IN,OUT> {
    Rule<IN,OUT> getRule();
    void apply(OUT out);
}
