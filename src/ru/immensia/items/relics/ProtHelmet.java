package ru.immensia.items.relics;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.EntityUtil;
import ru.immensia.utils.locs.LocUtil;

public class ProtHelmet extends SpecialItem {

    private static final int CD = 28;

    public ProtHelmet(final ItemStack it) {super(it);}

    private static final Set<DamageType> MELEE = Set.of(DamageType.MOB_PROJECTILE,
        DamageType.MOB_ATTACK, DamageType.STING, DamageType.MACE_SMASH);
    private static final Set<DamageType> BLOCK = Set.of(DamageType.MACE_SMASH, DamageType.PLAYER_EXPLOSION);
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {
        if (!(e instanceof final EntityDamageByEntityEvent ee)) return;
        final DamageType dt = e.getDamageSource().getDamageType();
        if (dt != DamageType.MACE_SMASH && !ee.isCritical()) return;
        if (e.getEntity() instanceof final Player tgt) {
            if (tgt.hasCooldown(item())) return;
            tgt.setCooldown(item(), CD);
        }
        e.setDamage(0d);
        EntityUtil.effect(e.getEntity(), Sound.ENTITY_TURTLE_EGG_CRACK,
            0.8f, Particle.ITEM, item());
    }

    protected static Collection<LivingEntity> getChArcLents(final Location loc,
        final double dst, final double arc, final Predicate<LivingEntity> can) {
        final Vector dir = loc.getDirection();
        final double dArc = arc * arc;
        return LocUtil.getChEnts(loc, dst, LivingEntity.class, ent -> {
            return can.test(ent)
                && EntityUtil.center(ent).subtract(loc).toVector()
                .normalize().subtract(dir).lengthSquared() < dArc;
        });
    }

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
