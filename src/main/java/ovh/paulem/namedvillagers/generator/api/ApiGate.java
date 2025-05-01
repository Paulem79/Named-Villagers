package ovh.paulem.namedvillagers.generator.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.paulem.namedvillagers.NamedVillagers;
import ovh.paulem.namedvillagers.generator.api.query.QueryBuilder;
import ovh.paulem.namedvillagers.utils.GsonUtils;

public abstract class ApiGate {
    private final API type;

    ApiGate(API type) {
        this.type = type;
    }

    public abstract QueryBuilder getBuilder();

    @Nullable
    public String fromConf(@NotNull String key, @Nullable String def) {
        return NamedVillagers.getInstance().getConfig().getString("api." + key, def);
    }

    @Nullable
    public JsonObject getResponse() {
        try {
            String jsonContent = GsonUtils.readUrl(getBuilder().build());
            return new Gson().fromJson(jsonContent, JsonObject.class);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while getting the response from the API: " + getType(), e);
        }
    }

    public abstract String generate();

    public abstract String getEndpoint();

    public API getType() {
        return type;
    }

    @Nullable
    public String getKey() {
        String key = NamedVillagers.getInstance().getConfig().getString("api.key");

        if(key == null || key.equals("YOUR_API_KEY")) {
            return null;
        }

        return key;
    }
}
