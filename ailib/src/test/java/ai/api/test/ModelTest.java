package ai.api.test;

import ai.api.model.Fulfillment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.google.gson.JsonPrimitive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import ai.api.BuildConfig;
import ai.api.GsonFactory;
import ai.api.model.AIResponse;

import static org.junit.Assert.*;

@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = BuildConfig.TESTS_SDK)
@RunWith(RobolectricTestRunner.class)
public class ModelTest {

    private static final String TEST_JSON = "{\n" +
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
            "      \"remind\": \"remind\",\n" +
            "      \"complex_param\": {\"nested_key\": \"nested_value\"}\n" +
            "    }\n" +
            "  },\n" +
            "  \"status\": {\n" +
            "    \"code\": 200,\n" +
            "    \"errorType\": \"success\"\n" +
            "  }\n" +
            "}";

    final Gson gson = GsonFactory.getGson();

    @Test
    public void trimParametersTest() {
        final AIResponse aiResponse = gson.fromJson(TEST_JSON, AIResponse.class);
        aiResponse.cleanup();

        assertFalse(aiResponse.getResult().getParameters().containsKey("date"));
        assertFalse(aiResponse.getResult().getParameters().containsKey("time"));
        assertFalse(aiResponse.getResult().getParameters().containsKey("priority"));

        assertTrue(aiResponse.getResult().getParameters().containsKey("date-time"));
        assertTrue(aiResponse.getResult().getParameters().containsKey("text"));
        assertTrue(aiResponse.getResult().getParameters().containsKey("remind"));

    }

    @Test
    @Ignore
    public void getDateParameterTest() {
        final AIResponse aiResponse = gson.fromJson(TEST_JSON, AIResponse.class);

        try {
            final Date dateTimeParameter = aiResponse.getResult().getDateTimeParameter("date-time");

            assertEquals(2015, dateTimeParameter.getYear());
            assertEquals(3 - 1 /* month is zero-based */, dateTimeParameter.getMonth());
            assertEquals(19, dateTimeParameter.getDate());
            assertEquals(7, dateTimeParameter.getHours());
            assertEquals(0, dateTimeParameter.getMinutes());

        } catch (final Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void getComplexParameterTest(){
        final AIResponse aiResponse = gson.fromJson(TEST_JSON, AIResponse.class);

        final JsonObject jsonObject = aiResponse.getResult().getComplexParameter("complex_param");

        assertNotNull(jsonObject);
        assertNotNull(jsonObject.get("nested_key"));
        assertEquals("nested_value", jsonObject.get("nested_key").getAsString());
    }

    @Test
    public void parseFulfillmentTest() {
        String stringFulfillment = "{\"speech\":\"hi friend\",\"source\":\"webhook\",\"displayText\":\"hi friend\",\"data\":{\"param\":\"value\"}}";
        {
            final Fulfillment fulfillment = gson.fromJson(stringFulfillment, Fulfillment.class);

            assertNotNull(fulfillment);
            assertEquals("hi friend", fulfillment.getSpeech());
            assertEquals("hi friend", fulfillment.getDisplayText());
            assertEquals("webhook", fulfillment.getSource());
            assertNotNull(fulfillment.getData());

            final JsonObject data = (JsonObject) fulfillment.getData();

            assertEquals("value", data.getAsJsonPrimitive("param").getAsString());
        }

        stringFulfillment = "{\"speech\":\"hi friend\",\"displayText\":\"hi friend\"}";
        {
            final Fulfillment fulfillment = gson.fromJson(stringFulfillment, Fulfillment.class);

            assertNotNull(fulfillment);
            assertEquals("hi friend", fulfillment.getSpeech());
            assertEquals("hi friend", fulfillment.getDisplayText());
            assertNull(fulfillment.getSource());
            assertNull(fulfillment.getData());
        }

        stringFulfillment = "{\"speech\":\"hi friend\",\"data\":\"some string data\"}";
        {
            final Fulfillment fulfillment = gson.fromJson(stringFulfillment, Fulfillment.class);

            assertNotNull(fulfillment);
            assertEquals("hi friend", fulfillment.getSpeech());
            assertNull(fulfillment.getDisplayText());
            assertNull(fulfillment.getSource());
            assertNotNull(fulfillment.getData());

            final JsonPrimitive jsonPrimitive = (JsonPrimitive) fulfillment.getData();
            assertEquals("some string data", jsonPrimitive.getAsString());
        }
    }
}
