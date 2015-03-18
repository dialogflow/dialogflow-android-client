package ai.api.test;

import com.google.gson.Gson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import ai.api.GsonFactory;
import ai.api.model.AIResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ModelTest {

    @Test
    public void testTrimParameters() {
        final Gson gson = GsonFactory.getGson();
        final String testJson = "{\n" +
                "  \"id\": \"d872e7d9-d2ee-4ebd-aaff-655bfc8fbf33\",\n" +
                "  \"timestamp\": \"2015-03-18T09:54:36.216Z\",\n" +
                "  \"result\": {\n" +
                "    \"resolvedQuery\": \"remind feed cat tomorrow 7 am\",\n" +
                "    \"action\": \"task_create\",\n" +
                "    \"parameters\": {\n" +
                "      \"date\": \"\",\n" +
                "      \"date-time\": \"2015-03-19T07:00:00+06:00\",\n" +
                "      \"time\": \"\",\n" +
                "      \"text\": \"feed cat\",\n" +
                "      \"priority\": \"\",\n" +
                "      \"remind\": \"remind\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"status\": {\n" +
                "    \"code\": 200,\n" +
                "    \"errorType\": \"success\"\n" +
                "  }\n" +
                "}";

        final AIResponse aiResponse = gson.fromJson(testJson, AIResponse.class);

        aiResponse.cleanup();

        assertFalse(aiResponse.getResult().getParameters().containsKey("date"));
        assertFalse(aiResponse.getResult().getParameters().containsKey("time"));
        assertFalse(aiResponse.getResult().getParameters().containsKey("priority"));

        assertTrue(aiResponse.getResult().getParameters().containsKey("date-time"));
        assertTrue(aiResponse.getResult().getParameters().containsKey("text"));
        assertTrue(aiResponse.getResult().getParameters().containsKey("remind"));

    }
}
