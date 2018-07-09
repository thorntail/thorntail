package io.thorntail.testsuite.jpa_wrapping;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author kg6zvp
 */
@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    String name;

    int age;

    public Person(){ }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
}
