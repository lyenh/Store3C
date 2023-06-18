package com.example.user.store3c;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getContext();

        assertEquals("com.example.user.store3c", appContext.getPackageName());
    }
}