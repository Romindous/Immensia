package ru.immensia.entities;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import com.google.common.collect.Multimap;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.Weapon;
import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;
import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.DamageTypeKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.immensia.Main;
import ru.immensia.boot.IStrap;
import ru.immensia.utils.ItemUtil;
import ru.immensia.utils.EntityUtil;
import ru.immensia.utils.StringUtil;
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
            DamageTypeKeys.MOB_ATTACK, DamageTypeKeys.MOB_ATTACK_NO_AGGRO, DamageTypeKeys.MOB_PROJECTILE, DamageTypeKeys.PLAYER_ATTACK,
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

            if (dmgrPl.getAttackCooldown() != 1f || !dmgrPl.isSprinting()
                || !DUAL_HIT.contains(hand.getType().asItemType())) return;

            final ItemStack ofh = inv.getItemInOffHand();
            if (!DUAL_HIT.contains(ofh.getType().asItemType())) return;

            Main.sync(() -> {
                final ItemStack noh = inv.getItemInOffHand();
                if (dmgrPl.isValid() && target.isValid() && noh.equals(ofh)) {
                    final ItemStack it = inv.getItemInMainHand().clone();
                    target.setNoDamageTicks(-1);
                    dmgrPl.addPotionEffect(HASTE);
                    inv.setItemInMainHand(ofh);
                    dmgrPl.setSprinting(false);
                    dmgrPl.attack(target);
                    inv.setItemInOffHand(inv.getItemInMainHand());
                    inv.setItemInMainHand(it);
                    dmgrPl.removePotionEffect(HASTE.getType());
                    Nms.swing(dmgrPl, EquipmentSlot.OFF_HAND);
                }
            }, DHIT_CLD);
            return;
        }

        if (target instanceof Mob || target instanceof ArmorStand) {// # v M
            final ItemStack shd = target.getEquipment().getItemInOffHand();
            if (ItemUtil.is(shd, ItemType.SHIELD) && Main.srnd.nextBoolean()) {
                target.getWorld().playSound(target.getLocation(),
                    Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                e.setDamage(0);
                e.setCancelled(true);
                return;
            }

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

                if (dmgrPl.getAttackCooldown() != 1f || !dmgrPl.isSprinting()
                    || !DUAL_HIT.contains(hand.getType().asItemType())) return;

                final ItemStack ofh = inv.getItemInOffHand();
                if (ItemUtil.isBlank(ofh, false)
                    || !DUAL_HIT.contains(ofh.getType().asItemType())) return;

                Main.sync(() -> {
                    final ItemStack noh = inv.getItemInOffHand();
                    if (dmgrPl.isValid() && target.isValid() && noh.equals(ofh)) {
                        final ItemStack it = inv.getItemInMainHand().clone();
                        target.setNoDamageTicks(-1);
                        dmgrPl.addPotionEffect(HASTE);
                        inv.setItemInMainHand(ofh);
                        dmgrPl.setSprinting(false);
                        dmgrPl.attack(target);
                        inv.setItemInOffHand(inv.getItemInMainHand());
                        inv.setItemInMainHand(it);
                        dmgrPl.removePotionEffect(HASTE.getType());
                        Nms.swing(dmgrPl, EquipmentSlot.OFF_HAND);
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
    @EventHandler
    public void onSmith(final PrepareSmithingEvent e) {
        final SmithingInventory ci = e.getInventory();
        final ItemStack it = e.getResult();
        if (!ItemUtil.isBlank(it, false)) {
            final ItemStack tr = ci.getInputTemplate();
            if (tr == null || ItemType.NETHERITE_UPGRADE_SMITHING_TEMPLATE
                .equals(tr.getType().asItemType())) return;
            final Material mt = it.getType();
            final EquipmentSlot es = mt.getEquipmentSlot();
            final EquipmentSlotGroup esg = es.getGroup();
            final Multimap<Attribute, AttributeModifier> amt = mt.getDefaultAttributeModifiers(es);
            final ItemMeta im = it.getItemMeta();
            im.removeAttributeModifier(es);
            double arm = 0d;
            for (final AttributeModifier am : amt.get(Attribute.ARMOR)) {
                switch (am.getOperation()) {
                    case ADD_NUMBER:
                        arm += am.getAmount();
                        break;
                    case ADD_SCALAR:
                        arm *= am.getAmount();
                        break;
                    case MULTIPLY_SCALAR_1:
                        arm *= (1d + am.getAmount());
                        break;
                }
            }
            double ath = 0d;
            for (final AttributeModifier am : amt.get(Attribute.ARMOR_TOUGHNESS)) {
                switch (am.getOperation()) {
                    case ADD_NUMBER:
                        ath += am.getAmount();
                        break;
                    case ADD_SCALAR:
                        ath *= am.getAmount();
                        break;
                    case MULTIPLY_SCALAR_1:
                        ath *= (1d + am.getAmount());
                        break;
                }
            }
            double akb = 0d;
            for (final AttributeModifier am : amt.get(Attribute.KNOCKBACK_RESISTANCE)) {
                switch (am.getOperation()) {
                    case ADD_NUMBER:
                        akb += am.getAmount();
                        break;
                    case ADD_SCALAR:
                        akb *= am.getAmount();
                        break;
                    case MULTIPLY_SCALAR_1:
                        akb *= (1d + am.getAmount());
                        break;
                }
            }

            final ItemStack add = ci.getInputMineral();
            im.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(NamespacedKey.minecraft("armor_defense"),
                arm * (1d + ItemUtil.getTrimMod(add, Attribute.ARMOR)), AttributeModifier.Operation.ADD_NUMBER, esg));

            im.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(NamespacedKey.minecraft("armor_toughness"),
                ath * (1d + ItemUtil.getTrimMod(add, Attribute.ARMOR_TOUGHNESS)), AttributeModifier.Operation.ADD_NUMBER, esg));

            im.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(NamespacedKey.minecraft("armor_knockback_resist"),
                akb * (1d + ItemUtil.getTrimMod(add, Attribute.KNOCKBACK_RESISTANCE)), AttributeModifier.Operation.ADD_NUMBER, esg));

            addAttr(im, Attribute.MAX_HEALTH, add, "armor_max_health", esg);
            addAttr(im, Attribute.SCALE, add, "armor_scale", esg);
            addAttr(im, Attribute.GRAVITY, add, "armor_gravity", esg);
            addAttr(im, Attribute.ATTACK_DAMAGE, add, "armor_attack_damage", esg);
            addAttr(im, Attribute.ATTACK_KNOCKBACK, add, "armor_attack_knockback", esg);
            addAttr(im, Attribute.ATTACK_SPEED, add, "armor_attack_speed", esg);
            addAttr(im, Attribute.MOVEMENT_SPEED, add, "armor_move_speed", esg);
            addAttr(im, Attribute.SNEAKING_SPEED, add, "armor_sneak_speed", esg);
            addAttr(im, Attribute.WATER_MOVEMENT_EFFICIENCY, add, "armor_water_speed", esg);
            addAttr(im, Attribute.JUMP_STRENGTH, add, "armor_jump_strength", esg);
            addAttr(im, Attribute.BLOCK_INTERACTION_RANGE, add, "armor_range_block", esg);
            addAttr(im, Attribute.ENTITY_INTERACTION_RANGE, add, "armor_range_entity", esg);
            addAttr(im, Attribute.BLOCK_BREAK_SPEED, add, "armor_break_speed", esg);

            it.setItemMeta(im);
            e.setResult(it);
        }
    }

    private static void addAttr(final ItemMeta im, final Attribute at, final ItemStack in, final String name, final EquipmentSlotGroup esg) {
        final double mod = ItemUtil.getTrimMod(in, at); if (mod == 0d) return;
        im.addAttributeModifier(at, new AttributeModifier(NamespacedKey.minecraft(name),
            mod, AttributeModifier.Operation.MULTIPLY_SCALAR_1, esg));
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
}
