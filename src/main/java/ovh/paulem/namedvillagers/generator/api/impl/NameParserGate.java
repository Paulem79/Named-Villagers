package ovh.paulem.namedvillagers.generator.api.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import ovh.paulem.namedvillagers.NamedVillagers;
import ovh.paulem.namedvillagers.generator.api.API;
import ovh.paulem.namedvillagers.generator.api.MultipleResultsApiGate;
import ovh.paulem.namedvillagers.generator.api.query.QueryBuilder;

public class NameParserGate extends MultipleResultsApiGate {
    public NameParserGate() {
        super(API.NAME_PARSER);
    }

    @Override
    public QueryBuilder getBuilder() {
        return new QueryBuilder(this)
                .add("api_key", getKey())
                .add("endpoint", "generate")
                .add("country_code", fromConf("country-code", null))
                .add("gender", getType().getGenderFromConf())
                .add("results", "25");
    }

    @Override
    public String getEndpoint() {
        return "https://api.parser.name/";
    }

    @Override
    public String generate() {
        if(pendingNames.isEmpty() && !generateMultipleNames()) {
            return NamedVillagers.getInstance().getGenerator().getRandomOfflineName();
        }

        return pendingNames.removeFirst();
    }

    @Override
    protected boolean generateMultipleNames() {
        @Nullable JsonObject response = getResponse();

        if(response == null || response.get("results").getAsInt() == 0) {
            return false;
        }

        JsonArray results = response
                .get("data").getAsJsonArray();

        results.forEach(jsonElement -> {
            String name = jsonElement.getAsJsonObject().get("name").getAsJsonObject()
                    .get("firstname").getAsJsonObject()
                    .get("name").getAsString();

            pendingNames.add(name);
        });

        return true;
    }
}
