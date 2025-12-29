package ru.immensia.items.relics;

import java.util.Set;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.EntityUtil;

public class VampSword extends SpecialItem {

    private static final double VAMP_MUL = 0.2d;
    private static final Set<DamageType> MELEE = Set.of(DamageType.PLAYER_ATTACK, DamageType.MOB_ATTACK);

    public VampSword(final ItemStack it) {super(it);}

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {
        if (eq != EquipmentSlot.HAND) return;
        final DamageSource ds = e.getDamageSource();
        if (e.getDamage() < 2d) return;
        if (!MELEE.contains(ds.getDamageType())) return;
        if (!(e.getEntity() instanceof final LivingEntity tle)) return;
        if (!(e.getDamager() instanceof final LivingEntity dle)) return;
        dle.heal(e.getDamage() * VAMP_MUL, EntityRegainHealthEvent.RegainReason.MAGIC);
        EntityUtil.effect(tle, Sound.BLOCK_WEEPING_VINES_FALL, 1f, Particle.DAMAGE_INDICATOR);
    }
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
