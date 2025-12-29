package ru.immensia.items.relics;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.EntityUtil;
import ru.immensia.utils.locs.LocUtil;

public class AYEMace extends SpecialItem {

    private static final double MACE_DST = 3.6d;

    public AYEMace(final ItemStack it) {super(it);}

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {
        if (eq != EquipmentSlot.HAND) return;
        final DamageSource ds = e.getDamageSource();
        if (ds.getDamageType() != DamageType.MACE_SMASH) return;
        final double dmg = e.getDamage();
        final Entity tgt = e.getEntity();
        final int tgtId = tgt.getEntityId();
        final Entity dmgr = e.getDamager();
        final int dmgrId = dmgr.getEntityId();
        final DamageSource nds = DamageSource.builder(DamageType.PLAYER_EXPLOSION)
            .withCausingEntity(dmgr).withDirectEntity(dmgr).build();
        for (final LivingEntity le : LocUtil.getChEnts(EntityUtil.center(tgt), MACE_DST, LivingEntity.class,
            ent -> ent.getEntityId() != tgtId && ent.getEntityId() != dmgrId)) { le.damage(dmg, nds);
            EntityUtil.effect(le, Sound.BLOCK_DEEPSLATE_TILES_BREAK, 0.8f,
                Particle.DUST_PILLAR, le.getWorld().getBlockData(le.getLocation().add(0d,-0.4d, 0d)));
        }
    }
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
