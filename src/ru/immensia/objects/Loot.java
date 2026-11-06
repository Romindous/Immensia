package ru.immensia.objects;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.immensia.utils.locs.BVec;

public class Loot {

    public static final Map<BVec, Loot> LOCS = new HashMap<>();

    private final ItemStack[] items;

    public Loot(final Player pl) {
        this.items = pl.getInventory().getContents();
    }
}