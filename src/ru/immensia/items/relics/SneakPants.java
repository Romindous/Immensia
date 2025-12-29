package ru.immensia.items.relics;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.immensia.Main;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.EntityUtil;

public class SneakPants extends SpecialItem {

    private static final int CD = 200;
    public SneakPants(final ItemStack it) {super(it);}

    public void onSneak(final PlayerToggleSneakEvent e) {
        final Player pl = e.getPlayer();
        if (pl.hasCooldown(item())) {
            if (!e.isSneaking()) {
                if (!pl.isVisibleByDefault()) EntityUtil.effect(pl,
                    Sound.ENTITY_BREEZE_WIND_BURST, 0.6f, Particle.CLOUD);
                pl.setVisibleByDefault(true);
            }
            return;
        }
        if (e.isSneaking()) {
            EntityUtil.effect(pl, Sound.ENTITY_BREEZE_WIND_BURST, 0.6f, Particle.LARGE_SMOKE);
            pl.setVisibleByDefault(false);
            pl.showEntity(Main.plug, pl);
            pl.setCooldown(item(), CD);
        } else {
            if (!pl.isVisibleByDefault()) EntityUtil.effect(pl,
                Sound.ENTITY_BREEZE_WIND_BURST, 0.6f, Particle.CLOUD);
            pl.setVisibleByDefault(true);
        }
    }

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {}
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
