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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.EntityUtil;

public class HasteSword extends SpecialItem {

    private static final int HASTE_TIME = 48;
        private static final int MAX_AMP = 5;
    private static final Set<DamageType> MELEE = Set.of(DamageType.PLAYER_ATTACK,
        DamageType.MOB_ATTACK, DamageType.MOB_ATTACK_NO_AGGRO);

    public HasteSword(final ItemStack it) {
        super(it);
    }

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {
        if (!eq.isHand()) return;
        final DamageSource ds = e.getDamageSource();
        if (!MELEE.contains(ds.getDamageType())) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        if (!(e.getDamager() instanceof final LivingEntity dle)) return;
        final PotionEffect pe = dle.getPotionEffect(PotionEffectType.HASTE);
        final int amp;
        if (pe != null) {
            amp = pe.getAmplifier() - (pe.getAmplifier() + 1) / MAX_AMP + 1;
            dle.removePotionEffect(PotionEffectType.HASTE);
        } else amp = 0;
        dle.addPotionEffect(new PotionEffect(PotionEffectType.HASTE,
            HASTE_TIME, amp, true, false, true));
        if (amp < MAX_AMP) EntityUtil.effect(dle, Sound.BLOCK_CONDUIT_AMBIENT,
            amp * 0.5f, Particle.DRIPPING_LAVA);
    }
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
