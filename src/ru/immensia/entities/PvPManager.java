package ru.immensia.entities;

import javax.annotation.Nullable;
import java.util.*;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;
import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.DamageTypeKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.immensia.Main;
import ru.immensia.boot.IStrap;
import ru.immensia.utils.EntityUtil;
import ru.immensia.utils.ItemUtil;
import ru.immensia.utils.strings.StringUtil;
import ru.immensia.utils.versions.Nms;

public class PvPManager implements Listener {

    public static final Set<PotionEffectType> potion_pvp_type = Set.of(PotionEffectType.POISON,
        PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA, PotionEffectType.INSTANT_DAMAGE, PotionEffectType.HUNGER);

    public static final String PVP_NOTIFY = "§cТы в режиме боя!";
    public static final PotionEffect HASTE = new PotionEffect(PotionEffectType.HASTE,
        2, 255, true, false, false);

    public static final Set<ItemType> AXES = IStrap.getAll(ItemTypeTagKeys.AXES);
    public static final Set<ItemType> DUAL_HIT = Set.of(ItemType.DIAMOND_SWORD,
        ItemType.GOLDEN_SWORD, ItemType.IRON_SWORD, ItemType.WOODEN_SWORD, ItemType.COPPER_SWORD,
        ItemType.STONE_SWORD, ItemType.NETHERITE_SWORD, ItemType.TRIDENT);
    public static final Set<ItemType> CAN_BLOCK = Set.of(ItemType.DIAMOND_SWORD,
        ItemType.GOLDEN_SWORD, ItemType.IRON_SWORD, ItemType.WOODEN_SWORD,
        ItemType.COPPER_SWORD, ItemType.STONE_SWORD, ItemType.NETHERITE_SWORD,
        ItemType.NETHERITE_AXE, ItemType.STONE_AXE, ItemType.WOODEN_AXE, ItemType.IRON_AXE,
        ItemType.COPPER_AXE, ItemType.GOLDEN_AXE, ItemType.DIAMOND_AXE);
    public static final List<DamageReduction> BLOCK_REDS = ItemType.SHIELD
        .getDefaultData(DataComponentTypes.BLOCKS_ATTACKS).damageReductions();
    public static final DamageReduction DMG_RED = DamageReduction.damageReduction().type(IStrap.regSetOf(Arrays.asList(DamageTypeKeys.MACE_SMASH,
            DamageTypeKeys.MOB_ATTACK, DamageTypeKeys.MOB_ATTACK_NO_AGGRO, DamageTypeKeys.MOB_PROJECTILE, DamageTypeKeys.PLAYER_ATTACK, DamageTypeKeys.SPEAR,
            DamageTypeKeys.THROWN, DamageTypeKeys.ARROW, DamageTypeKeys.WITHER_SKULL, DamageTypeKeys.WIND_CHARGE), RegistryKey.DAMAGE_TYPE))
        .horizontalBlockingAngle(60).factor(1f).build();
    public static final BlocksAttacks MELEE_BLOCK = BlocksAttacks.blocksAttacks().blockDelaySeconds(0f)
        .disableSound(IStrap.keyOf(Sound.BLOCK_COPPER_BULB_BREAK)).blockSound(IStrap.keyOf(Sound.BLOCK_COPPER_BULB_STEP))
        .disableCooldownScale(1.5f)/*.bypassedBy(RegTag.BYPASSES_WEAPON.tagKey())*/.addDamageReduction(DMG_RED).build();
    //List.of(DamageReduction.damageReduction().horizontalBlockingAngle(90f).base(0f).factor(1f).build())
    public static final float MELEE_BREAK_SEC = 2f;
    //weapons - disable shield if axe || (offhand empty && (run || crit || !shield))
    //weapon block breaks if !shield || axe

    public static final int DHIT_CLD = 4;
    public static final int BLCK_CLD = 0;
    private static final int HIT_DUR = 8;
    private static final int SPEAR_DELAY = 4;
    private static final float MIN_REACH = 0f;
    private static final float HITBOX_BUFF = 0.2f;

    public PvPManager() {
        reload();
    }

    public void reload() {
        Main.log("§2PvP включено!");
        HandlerList.unregisterAll(this);

        Bukkit.getPluginManager().registerEvents(this, Main.plug);
        Main.log("§6Активно улучшенное ПВП!");
    }

    public void onDisable() {
        Main.log("§6PvP выключено!");
    }

    private static final Map<UUID, Float> cooldowns = new HashMap<>();
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void EntityDamageByEntityEvent(final PrePlayerAttackEntityEvent e) {
        if (!e.willAttack()) return;
        final Player damager = e.getPlayer();
        if (!(e.getAttacked() instanceof final LivingEntity target)) return;
        if (!(target instanceof Mob) && !(target instanceof ArmorStand)) return;
        final ItemStack shd = target.getEquipment().getItemInOffHand();
        if (ItemUtil.is(shd, ItemType.SHIELD)) {
            target.getWorld().playSound(EntityUtil.center(target),
                Sound.ITEM_SHIELD_BLOCK, 0.8f, 0.8f);
            if (damager.getAttackCooldown() == 1f) {
                EntityUtil.effect(target, Sound.ITEM_SHIELD_BREAK, 0.8f,
                    Particle.ITEM, ItemType.SHIELD.createItemStack());
                target.getEquipment().setItemInOffHand(null);
            }
            e.setCancelled(true);
            return;
        }
        cooldowns.put(damager.getUniqueId(),
            damager.getAttackCooldown());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void EntityDamageByEntityEvent(final EntityDamageByEntityEvent e) {
        if (!e.getEntityType().isAlive()) return; //не обрабатывать урон рамкам, опыту и провее

        switch (e.getCause()) {
            case ENTITY_ATTACK:
            case ENTITY_EXPLOSION:
            case ENTITY_SWEEP_ATTACK:
            case MAGIC:
            case PROJECTILE:
            case CRAMMING:
            case SUICIDE:
                break;
            default:
                return;
        }

        if (e.getDamage() == 0d) {
            e.setCancelled(true);
            return;
        }

        final LivingEntity damager = EntityUtil.getDamager(e, false);
        if (damager == null) return;

        if (damager.getEntityId() == e.getEntity().getEntityId()) {
            e.setCancelled(true);
            return;
        }

        final int lvl;
        switch (e.getDamager()) {
            case final Trident tr:
                final ItemStack tit = tr.getItemStack();
                lvl = tit.getEnchantmentLevel(Enchantment.IMPALING);
                if (lvl == 0 || (!tr.isInRain() && !tr.isInWater())) break;
                e.setDamage(lvl * 2.5d + e.getDamage());
                break;
            case final LivingEntity le:
                if (le.getEquipment() == null) break;
                final ItemStack mhd = le.getEquipment().getItemInMainHand();
                lvl = mhd.getEnchantmentLevel(Enchantment.IMPALING);
                if (lvl == 0 || (!le.isInRain() && !le.isInWater())) break;
                e.setDamage(lvl * 2.5d + e.getDamage());
                break;
            default:
                break;
        }

        final LivingEntity target = (LivingEntity) e.getEntity();
        if (target.getType() == EntityType.PLAYER) {//# v P
            //P v P
            if (!(damager instanceof final Player dmgrPl)) return;
            final PlayerInventory inv = dmgrPl.getInventory();
            final ItemStack hand = inv.getItemInMainHand();
            final Weapon wpn = hand.getData(DataComponentTypes.WEAPON);
            if (wpn != null && wpn.disableBlockingForSeconds() != MELEE_BREAK_SEC) {
                hand.setData(DataComponentTypes.WEAPON, Weapon.weapon()
                    .itemDamagePerAttack(wpn.itemDamagePerAttack())
                    .disableBlockingForSeconds(MELEE_BREAK_SEC).build());
                inv.setItemInMainHand(hand);
            }

            Main.sync(() -> EntityUtil.indicate(target.getEyeLocation(), (e.isCritical() ? "<red>✘" : "<gold>")
                + StringUtil.toSigFigs(e.getFinalDamage(), (byte) 1), dmgrPl), 1);

            if (!isFullHit(damager) || !dmgrPl.isSprinting()
                || !DUAL_HIT.contains(hand.getType().asItemType())) return;

            final ItemStack ofh = inv.getItemInOffHand();
            if (!DUAL_HIT.contains(ofh.getType().asItemType())) return;

            Main.sync(() -> {
                final ItemStack noh = inv.getItemInOffHand();
                if (dmgrPl.isValid() && target.isValid() && noh.equals(ofh)) {
                    target.setNoDamageTicks(-1);
                    final int cdi = Nms.getFullCD(dmgrPl);
                    inv.setItemInOffHand(inv.getItemInMainHand());
                    inv.setItemInMainHand(ofh);
                    Main.sync(() -> {
                        final int cdn = Nms.getFullCD(dmgrPl);
                        Nms.setCD(dmgrPl, Math.min((float) cdi / (float) cdn, 0.9f));
                        dmgrPl.attack(target);
                        dmgrPl.setSprinting(false);
                        final ItemStack nmh = inv.getItemInMainHand().clone();
                        inv.setItemInMainHand(inv.getItemInOffHand());
                        inv.setItemInOffHand(nmh);
                        Nms.swing(dmgrPl, EquipmentSlot.OFF_HAND);
                    }, 1);
                }
            }, DHIT_CLD);
            return;
        }

        if (target instanceof Mob || target instanceof ArmorStand) {// # v M
            if (damager instanceof final Player dmgrPl) {// P v M
                final PlayerInventory inv = dmgrPl.getInventory();
                final ItemStack hand = inv.getItemInMainHand();
                final Weapon wpn = hand.getData(DataComponentTypes.WEAPON);
                if (wpn != null && wpn.disableBlockingForSeconds() != MELEE_BREAK_SEC) {
                    hand.setData(DataComponentTypes.WEAPON, Weapon.weapon()
                        .itemDamagePerAttack(wpn.itemDamagePerAttack())
                        .disableBlockingForSeconds(MELEE_BREAK_SEC).build());
                    inv.setItemInMainHand(hand);
                }

                Main.sync(() -> EntityUtil.indicate(target.getEyeLocation(), (e.isCritical() ? "<red>✘" : "<gold>")
                    + StringUtil.toSigFigs(e.getFinalDamage(), (byte) 1), dmgrPl), 1);

                if (!isFullHit(damager) || !dmgrPl.isSprinting()
                    || !DUAL_HIT.contains(hand.getType().asItemType())) return;

                final ItemStack ofh = inv.getItemInOffHand();
                if (ItemUtil.isBlank(ofh, false)
                    || !DUAL_HIT.contains(ofh.getType().asItemType())) return;

                Main.sync(() -> {
                    final ItemStack noh = inv.getItemInOffHand();
                    if (dmgrPl.isValid() && target.isValid() && noh.equals(ofh)) {
                        target.setNoDamageTicks(-1);
                        final int cdi = Nms.getFullCD(dmgrPl);
                        inv.setItemInOffHand(inv.getItemInMainHand());
                        inv.setItemInMainHand(ofh);
                        Main.sync(() -> {
                            final int cdn = Nms.getFullCD(dmgrPl);
                            Nms.setCD(dmgrPl, Math.min((float) cdi / (float) cdn, 0.9f));
                            dmgrPl.attack(target);
                            dmgrPl.setSprinting(false);
                            final ItemStack nmh = inv.getItemInMainHand().clone();
                            inv.setItemInMainHand(inv.getItemInOffHand());
                            inv.setItemInOffHand(nmh);
                            Nms.swing(dmgrPl, EquipmentSlot.OFF_HAND);
                        }, 1);
                    }
                }, DHIT_CLD);
                return;
            }
        }

        if (damager instanceof Mob) {// M v #
            final ItemStack hand = damager.getEquipment().getItemInOffHand();
            final Weapon wpn = hand.getData(DataComponentTypes.WEAPON);
            if (wpn != null && wpn.disableBlockingForSeconds() != MELEE_BREAK_SEC) {
                hand.setData(DataComponentTypes.WEAPON, Weapon.weapon()
                    .itemDamagePerAttack(wpn.itemDamagePerAttack())
                    .disableBlockingForSeconds(MELEE_BREAK_SEC).build());
                damager.getEquipment().setItemInMainHand(hand);
            }
        }
    }

    private boolean isFullHit(final LivingEntity le) {
        final Float cd = cooldowns.get(le.getUniqueId());
        return cd != null && cd == 1f;
    }

    @EventHandler
    public void onSmith(final PrepareSmithingEvent e) {
        final SmithingInventory ci = e.getInventory();
        final ItemStack it = e.getResult();
        if (ItemUtil.isBlank(it, false)) return;
        final ItemStack tr = ci.getInputTemplate();
        if (tr == null || ItemType.NETHERITE_UPGRADE_SMITHING_TEMPLATE
            .equals(tr.getType().asItemType())) return;
        final ItemStack in = ci.getInputMineral();
        e.setResult(ItemUtil.trimMod(it, in == null ? null : in.getType().asItemType()));
    }

    //weapons - disable shield if axe || (offhand empty && (run || crit || !shield))
    //weapon block breaks if !shield || axe
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onCld(final PlayerShieldDisableEvent e) {
        final ItemStack blIt = e.getPlayer().getActiveItem();
        if (!ItemUtil.is(blIt, ItemType.SHIELD)) return;
        switch (e.getDamager()) {
            case Player pl:
//                            pl.sendMessage("getAttackCooldown() = " + pl.getAttackCooldown()); всегда обновляет
                if (AXES.contains(pl.getInventory()
                    .getItemInMainHand().getType().asItemType())) break;
                if (ItemUtil.isBlank(pl.getInventory().getItemInOffHand(), false)
                    && (pl.getFallDistance() != 0 || pl.isSprinting())) break;
                e.setCooldown(0); e.setCancelled(true);
                break;
            case LivingEntity le:
                final EntityEquipment eq = le.getEquipment();
                if (eq == null) break;
                if (AXES.contains(eq.getItemInMainHand()
                    .getType().asItemType())) break;
                if (ItemUtil.isBlank(eq.getItemInOffHand(), false)) break;
                e.setCooldown(0); e.setCancelled(true);
                break;
            default: break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public static void onClick(final PlayerInteractEvent e) {
        if (!e.getAction().isRightClick()) return;
        final ItemStack it = e.getItem();
        final EquipmentSlot hand = e.getHand();
        if (it == null || hand == null
            || !CAN_BLOCK.contains(it.getType().asItemType())) return;
        final PlayerInventory inv = e.getPlayer().getInventory();
        if (ItemUtil.is(inv.getItemInOffHand(), ItemType.SHIELD)) {
            if (!it.hasData(DataComponentTypes.BLOCKS_ATTACKS)) return;
            it.resetData(DataComponentTypes.BLOCKS_ATTACKS);
            inv.setItem(hand, it);
            return;
        }
        if (it.hasData(DataComponentTypes.BLOCKS_ATTACKS)) return;
        it.setData(DataComponentTypes.BLOCKS_ATTACKS, MELEE_BLOCK);
        inv.setItem(hand, it);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void onProj(final ProjectileHitEvent e) {
        //попадание было в живчика
        if (e.getHitEntity() instanceof final LivingEntity target)
            target.setNoDamageTicks(0);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onRes(final EntityResurrectEvent e) {
        if (!e.isCancelled() || (!(e.getEntity() instanceof final Player p))) return;
        final PlayerInventory pi = p.getInventory();
        final int tsl = pi.first(Material.TOTEM_OF_UNDYING);
        if (tsl != -1) {
            pi.getItem(tsl).subtract();
            e.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void onGet(final InventoryClickEvent e) {
        final Inventory inv = e.getClickedInventory();
        if (inv == null || inv.getType() != e.getView().getTopInventory().getType()) return;
        final ItemStack it = updatePvPItem(inv.getItem(e.getSlot()));
        if (it != null) inv.setItem(e.getSlot(), it);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void onGet(final EntityPickupItemEvent e) {
        final Item item = e.getItem();
        final ItemStack it = updatePvPItem(item.getItemStack());
        if (it != null) item.setItemStack(it);
    }

    private static @Nullable ItemStack updatePvPItem(final ItemStack it) {
        if (it == null) return null;
        boolean change = false;
        final SwingAnimation swa = it.getData(DataComponentTypes.SWING_ANIMATION);
        final boolean isStab = ItemUtil.is(it, ItemType.TRIDENT)
            || (swa != null && swa.type() == SwingAnimation.Animation.STAB);
        if (isStab) {
            it.setData(DataComponentTypes.SWING_ANIMATION, SwingAnimation.swingAnimation()
                .type(SwingAnimation.Animation.STAB).duration(HIT_DUR << 1).build());
            final AttackRange arg = ItemType.DIAMOND_SPEAR.getDefaultData(DataComponentTypes.ATTACK_RANGE);
            it.setData(DataComponentTypes.ATTACK_RANGE, AttackRange.attackRange()
                .hitboxMargin(arg.hitboxMargin() + HITBOX_BUFF).mobFactor(arg.mobFactor()).maxReach(arg.maxReach())
                .maxCreativeReach(arg.maxCreativeReach()).minReach(MIN_REACH).minCreativeReach(MIN_REACH).build());
            change = true;
        }
        final KineticWeapon kw = it.getData(DataComponentTypes.KINETIC_WEAPON);
        if (kw != null) {
            it.setData(DataComponentTypes.KINETIC_WEAPON, KineticWeapon.kineticWeapon().delayTicks(SPEAR_DELAY)
                .damageConditions(kw.damageConditions()).dismountConditions(kw.damageConditions()).knockbackConditions(kw.knockbackConditions())
                .forwardMovement(kw.forwardMovement()).damageMultiplier(kw.damageMultiplier()).sound(kw.sound()).hitSound(kw.hitSound()).build());
            change = true;
        }
        final Weapon wpn = it.getData(DataComponentTypes.WEAPON);
        if (wpn != null && wpn.disableBlockingForSeconds() != MELEE_BREAK_SEC) {
            it.setData(DataComponentTypes.WEAPON, Weapon.weapon()
                .itemDamagePerAttack(wpn.itemDamagePerAttack())
                .disableBlockingForSeconds(MELEE_BREAK_SEC).build());
            if (ItemUtil.is(it, ItemType.MACE)) {
                final AttackRange arg = ItemType.MACE.getDefaultData(DataComponentTypes.ATTACK_RANGE);
                it.setData(DataComponentTypes.ATTACK_RANGE, AttackRange.attackRange()
                    .hitboxMargin(arg.hitboxMargin() + HITBOX_BUFF).mobFactor(arg.mobFactor()).maxReach(arg.maxReach())
                    .maxCreativeReach(arg.maxCreativeReach()).minReach(MIN_REACH).minCreativeReach(MIN_REACH).build());
            }
            change = true;
        }
        if (change) {
            return it;
        }
        /*if (swa != null) {
            it.resetData(DataComponentTypes.SWING_ANIMATION);
            return it;
        }*/
        return null;
    }

}
