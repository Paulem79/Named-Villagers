package ovh.paulem.namedvillagers.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class EntityUtilities {
    public static<T extends Entity> T getEntityByUniqueId(UUID uniqueId, Class<T> type) {
        for (World world : Bukkit.getWorlds()) {
            for (T entity : world.getEntitiesByClass(type)) {
                if (entity.getUniqueId().equals(uniqueId))
                    return entity;
            }
        }

        return null;
    }
}
