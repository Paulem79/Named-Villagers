package ovh.paulem.namedvillagers.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EntityUtils {
    @Nullable
    public static<T extends Entity> T getByUuid(UUID uniqueId, Class<T> type) {
        for (World world : Bukkit.getWorlds()) {
            for (T entity : world.getEntitiesByClass(type)) {
                if (entity.getUniqueId().equals(uniqueId))
                    return entity;
            }
        }

        return null;
    }
}
