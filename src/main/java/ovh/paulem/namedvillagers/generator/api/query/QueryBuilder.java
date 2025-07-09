package ovh.paulem.namedvillagers.generator.api.query;

import org.jetbrains.annotations.Nullable;
import ovh.paulem.namedvillagers.generator.api.ApiGate;

import java.util.LinkedList;

public class QueryBuilder {
    public final String baseLink;
    public LinkedList<String> query = new LinkedList<>();

    public QueryBuilder(ApiGate apiGate) {
        String endpoint = apiGate.getEndpoint();

        if(!endpoint.endsWith("/") && !endpoint.endsWith("?")) {
            endpoint += "/";
        } else if(!endpoint.endsWith("?")) {
            endpoint += "?";
        }

        baseLink = endpoint;
    }

    public QueryBuilder add(@Nullable String key, @Nullable String value) {
        if (key == null || value == null || key.isEmpty() || value.isEmpty()) {
            return this;
        }

        query.add(key + "=" + value);
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();

        sb.append(baseLink);

        boolean first = true;
        for (String s : query) {
            if (first) {
                sb.append(s);
                first = false;
            } else {
                sb.append("&").append(s);
            }
        }

        return sb.toString();
    }
}
