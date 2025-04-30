package ovh.paulem.namedvillagers.generator;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.paulem.namedvillagers.utils.Names;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class NameGenerator {
    public static final TypeToken<DataHolder> listType = new TypeToken<DataHolder>(){};

    private final Gson gson = new Gson();

    private final JavaPlugin plugin;

    private List<String> femaleNames;
    private List<String> maleNames;

    private boolean loaded = false;

    public NameGenerator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        maleNames = getDataFromResource("names-male.json", listType).data;
        femaleNames = getDataFromResource("names-female.json", listType).data;

        plugin.getLogger().info("Loaded " + maleNames.size() + " male names and " + femaleNames.size() + " female names.");
        plugin.getLogger().info("Total: " + (maleNames.size() + femaleNames.size()));

        loaded = true;
    }

    public String getRandomName() {
        List<String> villagerNameList = new ArrayList<>();

        villagerNameList.addAll(getFemaleNames());
        villagerNameList.addAll(getMaleNames());

        villagerNameList.removeIf(String::isEmpty);

        if (villagerNameList.isEmpty()) {
            return "";
        }

        return Names.randomFromList(villagerNameList);
    }

    private<T> T getDataFromResource(String resource, TypeToken<T> token) {
        try (InputStream inputStream = plugin.getResource(resource)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resource);
            }
            Reader reader = new InputStreamReader(inputStream, Charsets.UTF_8);
            return gson.fromJson(reader, token.getType());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + resource, e);
        }
    }

    // GIRLS ALSO HAVE A VOICE #ME TOO
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
