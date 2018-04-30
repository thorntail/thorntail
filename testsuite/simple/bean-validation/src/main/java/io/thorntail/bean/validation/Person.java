package io.thorntail.bean.validation;

import javax.validation.constraints.NotNull;

/**
 * Created by bob on 3/26/18.
 */
public class Person {

    @NotNull
    private String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
