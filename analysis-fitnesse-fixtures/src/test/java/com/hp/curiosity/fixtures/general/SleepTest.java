package com.hp.curiosity.fixtures.general;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class) public class SleepTest {

    private Sleep sleep;

    @Rule public final ExpectedException exception = ExpectedException.none();

    @Before public void setup() {
        sleep = new Sleep();
    }

    @Test public void test_that_if_a_nonpositive_time_value_passed_an_exception_is_thrown() {
        exception.expect(IllegalArgumentException.class);
        sleep.setTime(-40L);
    }

}