package ru.immensia.items.relics;

import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.immensia.items.SpecialItem;

public class Telebow extends SpecialItem {

    public Telebow(final ItemStack it) {super(it);}

    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof final AbstractArrow arr)) return;
        if (!(arr.getShooter() instanceof final LivingEntity le) || !eq.isHand()) return;
        if (le instanceof final Player pl && !pl.isSneaking()) return;
        arr.getShooter().launchProjectile(EnderPearl.class, arr.getVelocity());
        le.getWorld().playSound(le,
            Sound.BLOCK_END_PORTAL_FRAME_FILL, 1f, 0.6f);
        arr.remove();
    }

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {}
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
