package ru.immensia.items.relics;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
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

public class WardenChest extends SpecialItem {

    private static final int CD = 28;
    private static final int DIST = 8;
    private static final double ARC = 0.8d;
    private static final double DAMAGE = 8d;

    public WardenChest(final ItemStack it) {super(it);}

    private static final Set<DamageType> MELEE = Set.of(DamageType.PLAYER_ATTACK, DamageType.PLAYER_EXPLOSION,
        DamageType.MOB_ATTACK, DamageType.MOB_ATTACK_NO_AGGRO, DamageType.STING, DamageType.MACE_SMASH);
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {
        if (!(e.getEntity() instanceof final LivingEntity tgt)) return;
        final DamageSource ds = e.getDamageSource();
        if (!MELEE.contains(ds.getDamageType())) return;
        if (!(ds.getCausingEntity() instanceof final LivingEntity dmgr)) return;
        if (e.getEntity() instanceof final Player pt) {
            if (pt.hasCooldown(item())) return;
            pt.setCooldown(item(), CD);
        }
        if (eq != EquipmentSlot.CHEST) return;
        final int id = tgt.getEntityId();
        final Location loc = EntityUtil.center(tgt);
        final Collection<LivingEntity> les = getChArcLents(loc,
            le -> le.getEntityId() != id);
        if (les.isEmpty()) return;
        final DamageSource nds = DamageSource.builder(DamageType.SONIC_BOOM)
            .withCausingEntity(tgt).withDirectEntity(tgt).withDamageLocation(loc).build();
        for (final LivingEntity le : les) {
            le.damage(DAMAGE, nds);
        }
        final Vector vc = loc.getDirection();
        for (int i = 0; i != DIST; i++) {
            final double ofs = i * 0.25d;
            new ParticleBuilder(Particle.SONIC_BOOM).location(loc.add(vc))
                .count((i + 1) << 1).offset(ofs, ofs, ofs).receivers(40).spawn();
        }
        tgt.getWorld().playSound(tgt,
            Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1.4f);
    }

    private static Collection<LivingEntity> getChArcLents(final Location loc, final Predicate<LivingEntity> can) {
        final Vector dir = loc.getDirection();
        final double dArc = ARC * ARC;
        return LocUtil.getChEnts(loc, DIST, LivingEntity.class, ent -> {
            return can.test(ent)
                && EntityUtil.center(ent).subtract(loc).toVector()
                .normalize().subtract(dir).lengthSquared() < dArc;
        });
    }

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
