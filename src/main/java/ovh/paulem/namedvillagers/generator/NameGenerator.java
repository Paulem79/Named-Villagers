package ovh.paulem.namedvillagers.generator;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import ovh.paulem.namedvillagers.NamedVillagers;
import ovh.paulem.namedvillagers.generator.api.API;
import ovh.paulem.namedvillagers.generator.api.ApiGate;
import ovh.paulem.namedvillagers.utils.Names;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class NameGenerator {
    public static final TypeToken<DataHolder> listType = new TypeToken<DataHolder>(){};

    private final Gson gson = new Gson();

    private List<String> femaleNames;
    private List<String> maleNames;

    @Nullable
    private final ApiGate apiGate;

    private boolean loaded = false;

    public NameGenerator() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        API api = API.getByCall(NamedVillagers.getInstance().getConfig().getString("api.type", "NONE"));

        apiGate = api.getApiGate().getConstructor().newInstance();
    }

    public void load() {
        maleNames = getDataFromResource("names-male.json", listType).data;
        femaleNames = getDataFromResource("names-female.json", listType).data;

        NamedVillagers.getInstance().getLogger().info("Loaded " + maleNames.size() + " male names and " + femaleNames.size() + " female names.");
        NamedVillagers.getInstance().getLogger().info("Total: " + (maleNames.size() + femaleNames.size()));

        loaded = true;
    }

    public String getRandomOfflineName() {
        List<String> villagerNameList = new ArrayList<>();

        String both = NamedVillagers.getInstance().getConfig().getString("gender", "BOTH");
        if(both == null) {
            both = "BOTH";
        }

        boolean generateBoth = both.equals("BOTH");
        boolean generateMale = both.equals("MALE") || generateBoth;
        boolean generateFemale = both.equals("FEMALE") || generateBoth;

        if(generateMale) villagerNameList.addAll(getFemaleNames());
        if(generateFemale) villagerNameList.addAll(getMaleNames());

        villagerNameList.removeIf(String::isEmpty);

        if (villagerNameList.isEmpty()) {
            return "";
        }

        return Names.randomFromList(villagerNameList);
    }

    public String getRandomName() {
        if(apiGate == null) {
            return getRandomOfflineName();
        }

        String name = apiGate.generate();

        if(name == null || name.isEmpty()) {
            return getRandomOfflineName();
        }

        return name;
    }

    private<T> T getDataFromResource(String resource, TypeToken<T> token) {
        File file = NamedVillagers.getInstance().getDataFolder()
                .toPath().resolve(resource)
                .toFile();

        try (InputStream inputStream = file.toURI().toURL()
                .openConnection().getInputStream()) {

            if (inputStream == null) {
                return getT(resource, token, file);
            }

            Reader reader = new InputStreamReader(inputStream, Charsets.UTF_8);
            return gson.fromJson(reader, token.getType());

        } catch (Exception e) {
            try {
                return getT(resource, token, file);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to load resource: " + resource, e);
            }
        }
    }

    private <T> T getT(String resource, TypeToken<T> token, File file) throws IOException {
        InputStream stream = NamedVillagers.getInstance().getResource(resource);

        if (stream == null) {
            throw new RuntimeException("Resource not found: " + resource);
        }

        FileUtils.copyInputStreamToFile(stream, file);

        return getDataFromResource(resource, token);
    }

    // GIRLS ALSO HAVE A VOICE | #METOO
    public List<String> getFemaleNames() {
        Preconditions.checkState(loaded, "Names not loaded yet!");
        return femaleNames;
    }

    public List<String> getMaleNames() {
        Preconditions.checkState(loaded, "Names not loaded yet!");
        return maleNames;
    }

    public static class DataHolder {
        public List<String> data;
    }
}
