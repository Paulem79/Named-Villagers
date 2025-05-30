package ovh.paulem.namedvillagers;

import com.github.Anon8281.universalScheduler.UniversalRunnable;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Illager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.paulem.namedvillagers.config.ConfigManager;
import ovh.paulem.namedvillagers.generator.NameGenerator;
import ovh.paulem.namedvillagers.protocollib.ProtocolLibCompat;
import ovh.paulem.namedvillagers.utils.Names;
import ovh.paulem.namedvillagers.utils.Versioning;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NamedVillagers extends JavaPlugin implements Listener {
    private static NamedVillagers instance;
    private static TaskScheduler scheduler;

    public static NamespacedKey PDC_NAMEDVILLAGER;

    private NameGenerator generator;

    @Override
    public void onEnable() {
        if(!Versioning.isPost(14)){
            getLogger().severe("Your server must be newer than or in 1.14.1 to use this plugin!");
            setEnabled(false);

            return;
        }

        instance = this;

        saveDefaultConfig();
        new ConfigManager(this).migrate();

        scheduler = UniversalScheduler.getScheduler(this);

        PDC_NAMEDVILLAGER = new NamespacedKey(this, "namedvillager");
        try {
            generator = new NameGenerator();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            getLogger().severe("An error occurred while initializing the name generator!");
            setEnabled(false);

            return;
        }

        generator.load();

        getServer().getPluginManager().registerEvents(this, this);

        if(Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            new ProtocolLibCompat().enable();
        }

        // bStats
        if(getConfig().getBoolean("bstats")) {
            Metrics metrics = new Metrics(this, 21553);

            metrics.addCustomChart(new SimplePie("gender", () -> getConfig().getString("gender", "BOTH")));
            metrics.addCustomChart(new SimplePie("api_type", () -> getConfig().getString("api.type", "NONE")));
            metrics.addCustomChart(new SimplePie("api_country_code", () -> getConfig().getString("api.country-code", "NULL")));
        }

        final int SPIGOT_RESOURCE_ID = 124627;
        new UpdateChecker(this, UpdateCheckSource.SPIGET, String.valueOf(SPIGOT_RESOURCE_ID))
                .checkEveryXHours(24)
                .setNotifyOpsOnJoin(true)
                .setDownloadLink(SPIGOT_RESOURCE_ID)
                .checkNow(); // And check right now

        getLogger().info("Enabled " + getName() + "!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled " + getName() + "!");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onVillagerSpawn(CreatureSpawnEvent e){
        LivingEntity entity = e.getEntity();

        if(entity instanceof AbstractVillager || entity instanceof Illager){
            Creature villagerLike = (Creature) entity;

            setVillagerName(villagerLike);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onVillagerProfessionChange(VillagerCareerChangeEvent e){
        setVillagerName(e.getEntity());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChunkLoad(ChunkLoadEvent e){
        List<Creature> collected = Arrays.stream(e.getChunk().getEntities())
                .filter(entity -> (entity instanceof AbstractVillager || entity instanceof Illager) && !entity.getPersistentDataContainer().has(PDC_NAMEDVILLAGER, PersistentDataType.BYTE))
                .map(entity -> (Creature) entity)
                .collect(Collectors.toList());

        for (Creature villagerLike : collected) {
            if (!villagerLike.getPersistentDataContainer().has(PDC_NAMEDVILLAGER, PersistentDataType.BYTE)) {
                setVillagerName(villagerLike);
            }
        }
    }

    public void setVillagerName(Creature abstractVillager){
        getScheduler().runTaskAsynchronously(new UniversalRunnable() {
            @Override
            public void run() {
                String oldname = abstractVillager.getCustomName();

                String finalName = Names.capitalizeEveryWord((oldname != null ? oldname : generator.getRandomName()));

                if(abstractVillager instanceof Illager) {
                    abstractVillager.setCustomName(getIllagerPrefix() + finalName);
                } else {
                    abstractVillager.setCustomName(finalName);
                }

                abstractVillager.getPersistentDataContainer().set(PDC_NAMEDVILLAGER, PersistentDataType.BYTE, (byte) 1);
            }
        });
    }

    private final String illagerPrefix = getConfig().getString("illager-prefix", "");

    private String getIllagerPrefix() {
        if(illagerPrefix == null || illagerPrefix.isEmpty()) {
            return "";
        } else {
            return illagerPrefix + " ";
        }
    }

    public NameGenerator getGenerator() {
        return generator;
    }

    public static TaskScheduler getScheduler() {
        return scheduler;
    }

    public static NamedVillagers getInstance() {
        return instance;
    }
}