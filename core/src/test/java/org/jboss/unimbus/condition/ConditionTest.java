package org.jboss.unimbus.condition;

import javax.enterprise.inject.UnsatisfiedResolutionException;

import org.jboss.unimbus.UNimbus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Ken Finnigan
 */
public class ConditionTest {
    private static UNimbus uNimbus;

    @BeforeClass
    public static void setup() {
        uNimbus = new UNimbus(ConditionTest.class).start();
    }

    @AfterClass
    public static void shutdown() {
        uNimbus.stop();
        uNimbus = null;
    }

    @Test
    public void twoRequiredClassesSuccess() {
        assertThat(uNimbus.get(TwoRequiredBean.class)).isNotNull();
    }

    @Test
    public void oneRequiredOneMissingSuccess() {
        assertThat(uNimbus.get(OneNeededOneMissingBean.class)).isNotNull();
    }

    @Test
    public void twoRequiredClassesMissingSuccess() {
        assertThat(uNimbus.get(TwoMissingToBeActiveBean.class)).isNotNull();
    }

    @Test
    public void oneRequiredAndOneMissingFailure() {
        try {
            uNimbus.get(OneNeededOneMissingFailBean.class);
            fail("OneNeededOneMissingFailBean was found when it should not have been.");
        } catch (UnsatisfiedResolutionException e) {
            // Do nothing
        }
    }

    @Test
    public void twoRequiredFailure() {
        try {
            uNimbus.get(TwoRequiredFailBean.class);
            fail("TwoRequiredFailBean was found when it should not have been.");
        } catch (UnsatisfiedResolutionException e) {
            // Do nothing
        }
    }

    @Test
    public void oneRequiredSuccess() {
        assertThat(uNimbus.get(OneNeededBean.class)).isNotNull();
    }

    @Test
    public void oneRequiredFailure() {
        try {
            uNimbus.get(OneNeededFailBean.class);
            fail("OneNeededFailBean was found when it should not have been.");
        } catch (UnsatisfiedResolutionException e) {
            // Do nothing
        }
    }

    @Test
    public void oneMissingSuccess() {
        assertThat(uNimbus.get(OneMissingBean.class)).isNotNull();
    }

    @Test
    public void oneMissingFailure() {
        try {
            uNimbus.get(OneMissingFailBean.class);
            fail("OneMissingFailBean was found when it should not have been.");
        } catch (UnsatisfiedResolutionException e) {
            // Do nothing
        }
    }
}
