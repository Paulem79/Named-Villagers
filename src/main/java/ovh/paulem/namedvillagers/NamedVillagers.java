package ovh.paulem.namedvillagers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bstats.bukkit.Metrics;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.paulem.namedvillagers.generator.NameGenerator;
import ovh.paulem.namedvillagers.utils.EntityUtilities;
import ovh.paulem.namedvillagers.utils.Names;
import ovh.paulem.namedvillagers.utils.Versioning;

import java.io.Reader;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NamedVillagers extends JavaPlugin implements Listener {

    public static NamespacedKey PDC_NAMEDVILLAGER;

    private NameGenerator generator;

    @Override
    public void onEnable() {
        if(!Versioning.isPost(14, 1)){
            getLogger().severe("Your server must be >= 1.14.1 to use this plugin!");
            setEnabled(false);
            return;
        }

        PDC_NAMEDVILLAGER = new NamespacedKey(this, "namedvillager");
        generator = new NameGenerator(this);
        generator.load();

        getServer().getPluginManager().registerEvents(this, this);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.OPEN_WINDOW) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() != PacketType.Play.Server.OPEN_WINDOW) {
                    return;
                }
                PacketContainer packet = event.getPacket();

                Matcher matcher = Pattern.compile("insertion=([^}]+)").matcher(packet.getModifier().read(2).toString());

                String uuid = null;
                if (matcher.find()) {
                    uuid = matcher.group(1);
                }

                if(uuid == null) return;

                Villager villager = EntityUtilities.getEntityByUniqueId(UUID.fromString(uuid), Villager.class);

                if(villager == null || villager.getCustomName() == null) return;

                Villager.Profession profession = villager.getProfession();
                String professionName = profession.name();
                if(profession == Villager.Profession.NITWIT || profession == Villager.Profession.NONE){
                    professionName = "Idiot";
                }

                packet.getChatComponents().write(0, WrappedChatComponent.fromText(
                        villager.getCustomName() + " the " + Names.capitalizeEveryWord(professionName)
                ));
            }
        });

        // bStats
        new Metrics(this, 21553);

        getLogger().info("Enabled " + getName() + "!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled " + getName() + "!");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onVillagerSpawn(CreatureSpawnEvent e){
        if(e.getEntity() instanceof Villager){
            Villager villager = (Villager) e.getEntity();

            villager.getPersistentDataContainer().set(PDC_NAMEDVILLAGER, PersistentDataType.BYTE, (byte) 1);

            setVillagerName(villager);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onVillagerProfessionChange(VillagerCareerChangeEvent e){
        setVillagerName(e.getEntity());
    }

    public void setVillagerName(Villager villager){
        String oldname = villager.getCustomName();

        String finalName = Names.capitalizeEveryWord((oldname != null ? oldname : generator.getRandomName()));
        villager.setCustomName(finalName);
    }
}