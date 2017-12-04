package ai.api.test.services;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ai.api.BuildConfig;

@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = BuildConfig.TESTS_SDK)
@RunWith(RobolectricTestRunner.class)
public class GoogleRecognitionServiceImplTest {
    private static final ArrayList<String> texts = new ArrayList<>(Arrays.asList("9", "2", "5", "4"));
    private static final float[] rates = new float[]{0.9f, 0.2f, 0.5f, 0.4f};

    @Test
    public void sortResultsByRatesTest() throws Exception {
        Method method = Class.forName("ai.api.services.GoogleRecognitionServiceImpl")
                .getDeclaredMethod("sortResultsByRate", List.class, float[].class);
        method.setAccessible(true);
        method.invoke(null, texts, rates);
        Assert.assertEquals("9", texts.get(0));
        Assert.assertEquals("5", texts.get(1));
        Assert.assertEquals("4", texts.get(2));
        Assert.assertEquals("2", texts.get(3));
    }
}
