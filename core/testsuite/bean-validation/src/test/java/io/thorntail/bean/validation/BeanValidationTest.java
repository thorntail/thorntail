package io.thorntail.bean.validation;

import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import io.thorntail.test.ThorntailTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by bob on 3/26/18.
 */
@RunWith(ThorntailTestRunner.class)
public class BeanValidationTest {

    @Test
    public void testDefaultValidator() {
        assertThat( this.validator ).isNotNull();

        Person good = new Person("bob");
        Set<ConstraintViolation<Person>> violations = this.validator.validate(good);
        assertThat( violations ).isEmpty();

        Person bad = new Person(null);

        violations = this.validator.validate(bad);
        assertThat( violations ).hasSize(1);
    }


    @Inject
    Validator validator;

}
