package ru.immensia.items.relics;

import java.util.HashMap;
import java.util.Map;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import ru.immensia.items.SpecialItem;

public class SmeltPick extends SpecialItem {

    private static final Map<ItemType, ItemType> smelts = getSmelts();

    private static Map<ItemType, ItemType> getSmelts() {
        final Map<ItemType, ItemType> mp = new HashMap<>();
        Bukkit.recipeIterator().forEachRemaining(r -> {
            if (!(r instanceof final CookingRecipe<?> cr)) return;
            if (!(cr.getInputChoice() instanceof RecipeChoice.MaterialChoice mc)) return;
            for (final Material m : mc.getChoices())
                mp.put(m.asItemType(), cr.getResult().getType().asItemType());
        });
        return mp;
    }

    public SmeltPick(final ItemStack it) {super(it);}

    public void onDrop(final BlockDropItemEvent e) {
        boolean fnd = false;
        for (final Item di : e.getItems()) {
            final ItemStack it = di.getItemStack();
            final ItemType rs = smelts.get(it.getType().asItemType());
            if (rs == null || rs == ItemType.AIR) continue;
            di.setItemStack(rs.createItemStack(it.getAmount()));
            if (!fnd) fnd = true;
        }
        if (!fnd) return;
        final Location blc = e.getBlock().getLocation().toCenterLocation();
        e.getPlayer().playSound(blc, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1f, 2f);
        new ParticleBuilder(Particle.SMALL_FLAME).location(blc).offset(0.4d, 0.4d, 0.4d)
            .receivers(20).extra(0.01d).count(40).spawn();
    }

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {}
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
