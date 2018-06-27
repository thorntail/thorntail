package io.thorntail.condition;

import javax.enterprise.inject.UnsatisfiedResolutionException;

import io.thorntail.Thorntail;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Ken Finnigan
 */
public class ConditionTest {
    private static Thorntail thorntail;

    @BeforeClass
    public static void setup() {
        thorntail = new Thorntail(ConditionTest.class).start();
    }

    @AfterClass
    public static void shutdown() {
        thorntail.stop();
        thorntail = null;
    }

    @Test
    public void twoRequiredClassesSuccess() {
        assertThat(thorntail.get(TwoRequiredBean.class)).isNotNull();
    }

    @Test
    public void oneRequiredOneMissingSuccess() {
        assertThat(thorntail.get(OneNeededOneMissingBean.class)).isNotNull();
    }

    @Test
    public void twoRequiredClassesMissingSuccess() {
        assertThat(thorntail.get(TwoMissingToBeActiveBean.class)).isNotNull();
    }

    @Test
    public void oneRequiredAndOneMissingFailure() {
        try {
            thorntail.get(OneNeededOneMissingFailBean.class);
            fail("OneNeededOneMissingFailBean was found when it should not have been.");
        } catch (UnsatisfiedResolutionException e) {
            // Do nothing
        }
    }

    @Test
    public void twoRequiredFailure() {
        try {
            thorntail.get(TwoRequiredFailBean.class);
            fail("TwoRequiredFailBean was found when it should not have been.");
        } catch (UnsatisfiedResolutionException e) {
            // Do nothing
        }
    }

    @Test
    public void oneRequiredSuccess() {
        assertThat(thorntail.get(OneNeededBean.class)).isNotNull();
    }

    @Test
    public void oneRequiredFailure() {
        try {
            thorntail.get(OneNeededFailBean.class);
            fail("OneNeededFailBean was found when it should not have been.");
        } catch (UnsatisfiedResolutionException e) {
            // Do nothing
        }
    }

    @Test
    public void oneMissingSuccess() {
        assertThat(thorntail.get(OneMissingBean.class)).isNotNull();
    }

    @Test
    public void oneMissingFailure() {
        try {
            thorntail.get(OneMissingFailBean.class);
            fail("OneMissingFailBean was found when it should not have been.");
        } catch (UnsatisfiedResolutionException e) {
            // Do nothing
        }
    }
}
