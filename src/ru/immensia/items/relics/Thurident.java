package ru.immensia.items.relics;

import java.util.Set;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.EntityUtil;
import ru.immensia.utils.locs.LocUtil;

public class Thurident extends SpecialItem {

    private static final Set<DamageType> MELEE = Set.of(DamageType.PLAYER_ATTACK,
        DamageType.MOB_ATTACK, DamageType.MOB_ATTACK_NO_AGGRO);
    private static final double THUND_DIST = 4d;
    private static final double STRIKE_DIST = 2d;
    private static final double ZAP_DMG = 2.5d;
    private static final int FIRE_TICKS = 100;

    public Thurident(final ItemStack it) {
        super(it);
    }

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {
        if (eq != EquipmentSlot.HAND) return;
        final DamageSource ds = e.getDamageSource();
        if (!MELEE.contains(ds.getDamageType())) return;
        if (!(e.getEntity() instanceof final LivingEntity tle)) return;
        if (!(e.getDamager() instanceof final LivingEntity dle)) return;
        if (!dle.isRiptiding()) return;
        strike(EntityUtil.center(tle), dle);
    }

    public void onHit(final ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof final Trident tr)) return;
        tr.getWorld().strikeLightning(EntityUtil.center(tr));
        if (!(e.getHitEntity() instanceof final LivingEntity le)) return;
        if (!le.isInWater() && !le.isInRain() &&
            !tr.getItemStack().containsEnchantment(Enchantment.CHANNELING)) return;
        if (!(tr.getShooter() instanceof final LivingEntity dmgr)) return;
        final int did = dmgr.getEntityId(), tid = le.getEntityId();
        for (final LivingEntity tgt : LocUtil.getChEnts(EntityUtil.center(le), THUND_DIST,
            LivingEntity.class, ent -> ent.getEntityId() != did && ent.getEntityId() != tid)) {
            strike(EntityUtil.center(tgt), dmgr);
        }
    }

    private void strike(final Location loc, final LivingEntity dmgr) {
        final int id = dmgr.getEntityId();
        dmgr.getWorld().strikeLightningEffect(loc);
        final DamageSource nds = DamageSource.builder(DamageType.LIGHTNING_BOLT)
            .withCausingEntity(dmgr).withDirectEntity(dmgr).withDamageLocation(loc).build();
        for (final LivingEntity tgt : LocUtil.getChEnts(loc, STRIKE_DIST,
            LivingEntity.class, ent -> ent.getEntityId() != id)) {
            tgt.damage(ZAP_DMG, nds);
            tgt.setFireTicks(FIRE_TICKS);
        }
    }

    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}