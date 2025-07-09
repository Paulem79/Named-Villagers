package ovh.paulem.namedvillagers.config;

import org.bukkit.configuration.file.FileConfiguration;
import ovh.paulem.namedvillagers.NamedVillagers;

public class ConfigManager {
    private final NamedVillagers plugin;

    public ConfigManager(NamedVillagers plugin){
        this.plugin = plugin;
    }

    public void migrate(){
        FileConfiguration config = plugin.getConfig();

        int detectedVersion = config.getInt("version", 0);
        new ConfigUpdater(plugin).checkUpdate(detectedVersion);
    }
}
