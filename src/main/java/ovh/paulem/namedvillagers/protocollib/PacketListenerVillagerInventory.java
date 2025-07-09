package ovh.paulem.namedvillagers.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import ovh.paulem.namedvillagers.NamedVillagers;
import ovh.paulem.namedvillagers.utils.EntityUtils;
import ovh.paulem.namedvillagers.utils.Names;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PacketListenerVillagerInventory extends PacketAdapter {
    public PacketListenerVillagerInventory() {
        super(NamedVillagers.getInstance(), PacketType.Play.Server.OPEN_WINDOW);
    }

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

        AbstractVillager entity = EntityUtils.getByUuid(UUID.fromString(uuid), AbstractVillager.class);

        if(entity == null || entity.getCustomName() == null) return;

        if(entity instanceof WanderingTrader) {
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(
                    NamedVillagers.getInstance().getConfig().getString("interface-name", "%n the %p")
                            .replace("%n", entity.getCustomName())
                            .replace("%p", Names.capitalizeEveryWord("Wandering Trader"))
            ));
        } else if(entity instanceof Villager) {
            Villager villager = (Villager) entity;

            Villager.Profession profession = villager.getProfession();
            String professionName = profession.name();

            if(profession == Villager.Profession.NITWIT || profession == Villager.Profession.NONE){
                professionName = "Idiot";
            }

            packet.getChatComponents().write(0, WrappedChatComponent.fromText(
                    NamedVillagers.getInstance().getConfig().getString("interface-name", "%n the %p")
                            .replace("%n", villager.getCustomName())
                            .replace("%p", Names.capitalizeEveryWord(professionName))
            ));
        }
    }
}
