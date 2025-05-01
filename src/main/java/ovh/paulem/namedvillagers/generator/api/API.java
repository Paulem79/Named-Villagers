package ovh.paulem.namedvillagers.generator.api;

import org.jetbrains.annotations.Nullable;
import ovh.paulem.namedvillagers.NamedVillagers;
import ovh.paulem.namedvillagers.generator.api.impl.NameParserGate;

public enum API {
    NONE(
            () -> "", () -> "",
            "NONE", null),
    NAME_PARSER(
            () -> "m", () -> "f",
            "NAMEPARSER", NameParserGate.class);

    private final Gender male, female;
    private final String configCall;
    private final Class<? extends ApiGate> apiGate;

    API(Gender male, Gender female, String configCall, Class<? extends ApiGate> apiGate) {
        this.male = male;
        this.female = female;
        this.configCall = configCall;
        this.apiGate = apiGate;
    }

    @Nullable
    public Class<? extends ApiGate> getApiGate() {
        return apiGate;
    }

    public String getConfigCall() {
        return configCall;
    }

    public Gender getMale() {
        return male;
    }

    public Gender getFemale() {
        return female;
    }

    @Nullable
    public String getGenderFromConf() {
        String gender = NamedVillagers.getInstance().getConfig().getString("gender", "BOTH");

        if(gender == null) {
            return null;
        }

        if(gender.equalsIgnoreCase("MALE")) {
            return male.getCalling();
        } else if(gender.equalsIgnoreCase("FEMALE")) {
            return female.getCalling();
        }

        return null;
    }

    public static API getByCall(String called){
        for(API api : API.values()){
            if(api.getConfigCall().equalsIgnoreCase(called)){
                return api;
            }
        }
        return API.NONE;
    }

    public interface Gender {
        @Nullable String getCalling();
    }
}
