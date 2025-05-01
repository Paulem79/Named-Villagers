package ovh.paulem.namedvillagers.generator.api;

import java.util.ArrayList;
import java.util.List;

public abstract class MultipleResultsApiGate extends ApiGate {
    protected List<String> pendingNames = new ArrayList<>();

    protected MultipleResultsApiGate(API type) {
        super(type);
    }

    protected abstract boolean generateMultipleNames();
}
