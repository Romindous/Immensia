package ru.immensia.items.relics;

import java.util.Set;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.EntityUtil;

public class FreezeAxe extends SpecialItem {

    private static final double FREEZE_MUL = 6d;
    private static final Set<DamageType> MELEE = Set.of(DamageType.PLAYER_ATTACK, DamageType.MOB_ATTACK);

    public FreezeAxe(final ItemStack it) {super(it);}

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {
        if (eq != EquipmentSlot.HAND) return;
        final DamageSource ds = e.getDamageSource();
        if (!MELEE.contains(ds.getDamageType())) return;
        if (!e.isCritical()) return;
        if (!(e.getEntity() instanceof final LivingEntity tle)) return;
        tle.setFreezeTicks((int) (e.getDamage() * FREEZE_MUL) + tle.getFreezeTicks());
        EntityUtil.effect(tle, Sound.ENTITY_PLAYER_HURT_FREEZE, 1.4f, Particle.CLOUD);
    }
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
