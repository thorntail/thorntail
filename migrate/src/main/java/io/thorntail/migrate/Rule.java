package io.thorntail.migrate;

import java.util.List;

/**
 * Created by bob on 3/13/18.
 */
public interface Rule<IN,OUT> {
    List<? extends Action<IN,OUT>> match(IN context);
}
