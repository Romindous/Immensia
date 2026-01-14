package ru.immensia;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import com.destroystokyo.paper.ParticleBuilder;
import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.Fireworks;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.Ageable;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.immensia.boot.IStrap;
import ru.immensia.entities.mobs.DragonBoss;
import ru.immensia.items.ItemBuilder;
import ru.immensia.items.SpecialItem;
import ru.immensia.items.crafts.CraftManager;
import ru.immensia.utils.BlockUtil;
import ru.immensia.utils.ClassUtil;
import ru.immensia.utils.EntityUtil;
import ru.immensia.utils.ItemUtil;
import ru.immensia.utils.locs.BVec;
import ru.immensia.utils.locs.LocUtil;
import ru.immensia.utils.strings.TCUtil;
import ru.immensia.utils.versions.Nms;

public class MainLis implements Listener {

    private static final int BREED_LIGHT = 12;
    private static final int FARM_DST = 28;
    private static final int FARM_MAX = 8;
    private static final ItemStack bnr;
    private static final Map<ItemType, Integer> stacks;

    static {
        stacks = new HashMap<>();
        stacks.put(ItemType.POTION, 16);
        stacks.put(ItemType.SPLASH_POTION, 16);
        stacks.put(ItemType.LINGERING_POTION, 16);
        stacks.put(ItemType.TOTEM_OF_UNDYING, 4);
        bnr = ItemType.WHITE_BANNER.createItemStack();
        final BannerMeta bm = (BannerMeta) bnr.getItemMeta();
        bm.displayName(Component.text("Зловещий Флаг", TextColor.color(0xbb4422)));
        bm.addPattern(new Pattern(DyeColor.CYAN, PatternType.RHOMBUS));
        bm.addPattern(new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_BOTTOM));
        bm.addPattern(new Pattern(DyeColor.GRAY, PatternType.STRIPE_CENTER));
        bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE));
        bm.addPattern(new Pattern(DyeColor.LIGHT_GRAY, PatternType.HALF_HORIZONTAL));
        bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.BORDER));
        bm.addPattern(new Pattern(DyeColor.LIGHT_GRAY, PatternType.CIRCLE));
        bnr.setItemMeta(bm);
    }

    @EventHandler
    public void onLogin(final PlayerJoinEvent e) {
        CraftManager.discRecs(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDth(final PlayerDeathEvent e) {
        final Player p = e.getPlayer();
        final Location loc = p.getLocation();
        p.sendMessage(TCUtil.form(Main.PREFIX + "You died at:\n§7(§a"
            + loc.getBlockX() + "§7, §a" + loc.getBlockY() + "§7, §a" + loc.getBlockZ() + "§7)"));
        e.setDroppedExp(p.calculateTotalExperiencePoints() >> 1);
    }

	/*@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInput(final PlayerInputEvent e) {
//		if (!e.getInput().isJump()) return;
		final SpecialItem si = SpecialItem.get(e.getPlayer()
			.getInventory().getBoots());
		if (Main.relics.JUMPY_BOOTS.equals(si)) {
			Main.relics.JUMPY_BOOTS.onJump(e);
		}
	}

	@EventHandler
	public void onDrop(final BlockDropItemEvent e) {
		final SpecialItem si = SpecialItem.get(e.getPlayer()
			.getInventory().getItemInMainHand());
		if (Main.relics.SMELT_PICK.equals(si)) {
			Main.relics.SMELT_PICK.onDrop(e);
		}
	}

	@EventHandler
	public void onSneak(final PlayerToggleSneakEvent e) {
		final SpecialItem si = SpecialItem.get(e.getPlayer()
			.getInventory().getLeggings());
		if (Main.relics.SNEAK_PANTS.equals(si)) {
			Main.relics.SNEAK_PANTS.onSneak(e);
		}
	}

	@EventHandler
	public void onBreak(final BlockBreakEvent e) {
		final Player pl = e.getPlayer();
		final ItemStack it = pl.getInventory().getItemInMainHand();
        final Block b = e.getBlock();
        if (!b.isPreferredTool(it)) return;
        if (Ostrov.wg && !WGhook.canBuild(pl, b.getLocation())) return;
		switch (SpecialItem.get(e.getPlayer()
			.getInventory().getItemInMainHand())) {
			case final AreaPick ap -> ap.onBreak(e);
			case final TreeAxe ta -> ta.onBreak(e);
			case null, default -> {}
		}
	}*/

    @EventHandler
    public void onHit(final ProjectileHitEvent e) {
        final SpecialItem si;
        switch (e.getEntity()) {
            /*case final Trident tp:
                si = SpecialItem.get(tp.getItemStack());
                if (Main.relics.THURIDENT.equals(si)) {
                    Main.relics.THURIDENT.onHit(e);
                }
                break;
            case final AbstractArrow ar:
                si = SpecialItem.get(ar.getWeapon());
                if (Main.relics.CROSS_WEB.equals(si)) {
                    Main.relics.CROSS_WEB.onHit(e);
                }
                break;*/
            case final DragonFireball ignored when
                e.getHitEntity() instanceof EnderDragon:
                e.getEntity().remove();
                e.setCancelled(true);
                break;
            default:
                break;
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onDmg(final EntityExplodeEvent e) {
        if (e.getEntity() instanceof final EnderCrystal ec) {
            DragonBoss.spawnChild(EntityUtil.center(ec));
        }
    }

    private static final Set<DamageType> TICKED = Set.of(DamageType.ON_FIRE,
        DamageType.FREEZE, DamageType.WITHER, DamageType.INDIRECT_MAGIC);
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onDth(final EntityDeathEvent e) {
        if (!(e.getEntity() instanceof final Mob mb)) return;
        if (mb.getCanPickupItems()) return;

        LivingEntity klr = EntityUtil.lastDamager(mb, true);
        if (klr == null) {
            final EntityDamageEvent lde = mb.getLastDamageCause();
            if (lde == null || !TICKED.contains(lde.getDamageSource().getDamageType())) {
                e.getDrops().clear();
                e.setDroppedExp(0);
                return;
            }
            final Player kpl = mb.getKiller();
            final LivingEntity tgt = mb.getTarget();
            klr = kpl != null && tgt instanceof final Player tpl
                && tpl.getEntityId() == kpl.getEntityId() ? tpl : null;
            if (klr == null) {
                e.getDrops().clear();
                e.setDroppedExp(0);
                return;
            }
        }

        switch (klr.getType()) {
            case FROG, CREEPER, WITHER, SKELETON, STRAY, PARCHED, BOGGED:
                break;
            case PLAYER:
                /*switch (mb.getType()) {
                    case ENDERMAN, ZOMBIFIED_PIGLIN, CAVE_SPIDER, SILVERFISH:
                        e.setDroppedExp(0);
                        break;
                    default:
                        break;
                }*/
                if (mb instanceof Raider && ((Raider) mb).isPatrolLeader()) {
                    if (klr instanceof Player) {
                        final Advancement adv = Bukkit.getAdvancement(NamespacedKey.minecraft("adventure/voluntary_exile"));
                        if (adv == null)
                            Main.log_warn("Incorrect advancement " + Key.key("adventure/voluntary_exile").asMinimalString());
                        else {
                            final AdvancementProgress adp = ((Player) klr).getAdvancementProgress(adv);
                            for (final String cr : adv.getCriteria()) adp.awardCriteria(cr);
                        }
                    }
                }
                final AttributeInstance hp = mb.getAttribute(Attribute.MAX_HEALTH);
                if (hp == null) break;
                e.setDroppedExp((int) (e.getDroppedExp() * hp.getValue() / hp.getBaseValue()));
                break;
            default:
                e.getDrops().clear();
                e.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onVill(final VillagerAcquireTradeEvent e) {
        final MerchantRecipe mr = e.getRecipe();
        final ItemStack rs = mr.getResult();
        modEnch(rs, DataComponentTypes.ENCHANTMENTS);
        modEnch(rs, DataComponentTypes.STORED_ENCHANTMENTS);
        final MerchantRecipe nmr = new MerchantRecipe(rs, mr.getUses(), mr.getMaxUses(), mr.hasExperienceReward(), mr.getVillagerExperience(),
            mr.getPriceMultiplier() * 2f, mr.getDemand(), mr.getSpecialPrice(), mr.shouldIgnoreDiscounts());
        nmr.setIngredients(mr.getIngredients());
        e.setRecipe(nmr);
    }

    private static void modEnch(final ItemStack it, final DataComponentType.Valued<ItemEnchantments> data) {
        final ItemEnchantments ies = it.getData(data);
        if (ies == null) return;
        final Map<Enchantment, Integer> enchs = new HashMap<>();
        final Integer lvl = ies.enchantments().get(Enchantment.MENDING);
        if (lvl != null) enchs.put(Enchantment.SWIFT_SNEAK, lvl);
        for (final Entry<Enchantment, Integer> en : ies.enchantments().entrySet()) {
            enchs.put(en.getKey(), Math.min((en.getKey().getMaxLevel() >> 1) + 1, en.getValue()));
        }
        it.setData(data, ItemEnchantments.itemEnchantments(enchs));
    }

    @EventHandler
    public void onRegen(final EntityRegainHealthEvent e) {
        if (e.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN
            && e.getEntityType() == EntityType.ALLAY) e.setAmount(0);
    }

    @EventHandler
    public void onInvClick(final InventoryClickEvent e) {
        if (e.getResult() == Result.DENY || e.isCancelled()) return;
        final Inventory ci = e.getClickedInventory();
        if (ci == null) return;

        final String title = TCUtil.strip(e.getView().title());
        if (ci.getSize() == CraftManager.MENU_SIZE && title.startsWith(CraftManager.MENU_TITLE)) {
            if (CraftManager.clickEditor(e.getWhoClicked(), ci,
                title.substring(title.lastIndexOf(' ') + 1), e.getSlot())) {
                e.setResult(Result.DENY);
            }
            return;
        }

        final ItemStack curr = e.getCurrentItem();
        if (curr == null) return;
        final ItemType tp = curr.getType().asItemType();
        final Integer mxs = stacks.get(tp);
        if (mxs != null) {
            final ItemMeta im = curr.getItemMeta();
            if (!im.hasMaxStackSize() || im.getMaxStackSize() != mxs) {
                final Inventory top = e.getView().getTopInventory();
                for (final ItemStack i : top) {
                    if (ItemUtil.is(i, tp)) {
                        maxStack(i, mxs);
                    }
                }
                final Inventory bot = e.getView().getBottomInventory();
                for (final ItemStack i : bot) {
                    if (ItemUtil.is(i, tp)) {
                        maxStack(i, mxs);
                    }
                }
            }
        }

        if (ci instanceof final GrindstoneInventory ginv) {
            if (tp == ENCH_TYPE && e.getSlot() == 2) {
                e.setCancelled(true);
                final Player pl = (Player) e.getWhoClicked();
                ItemUtil.giveItemsTo(pl, curr);
                pl.getWorld().playSound(pl, Sound.BLOCK_GRINDSTONE_USE, 1f, 1f);
                for (final ItemStack i : ginv) {
                    if (i == null) continue;
                    i.setAmount(ItemUtil.is(i, BOOK_TYPE) ? i.getAmount() - 1 : 0);
                }
                return;
            }
            final HumanEntity he = e.getWhoClicked();
            final ItemStack crs = he.getItemOnCursor();
            if (!ItemUtil.is(crs, BOOK_TYPE)) return;
            e.setCancelled(true);
            he.setItemOnCursor(curr);
            e.setCurrentItem(crs);
        }
    }

    private boolean maxStack(final ItemStack it, final int mxs) {
        final Integer msd = it.getData(DataComponentTypes.MAX_STACK_SIZE);
        if (msd == null || msd == mxs) return false;
        it.setData(DataComponentTypes.MAX_STACK_SIZE, mxs);
        return true;
    }

    private static final ItemType ENCH_TYPE = ItemType.ENCHANTED_BOOK;
    private static final ItemType BOOK_TYPE = ItemType.BOOK;

    @EventHandler
    public void onPreGrind(final PrepareGrindstoneEvent e) {
        final GrindstoneInventory ginv = e.getInventory();
        if (!ItemUtil.isBlank(e.getResult(), false)) return;
        final ItemStack upi = ginv.getUpperItem();
        final ItemStack lwi = ginv.getLowerItem();
        if (ItemUtil.is(upi, BOOK_TYPE)) {//up is book
            if (!canStrip(lwi)) return;
            final ItemStack ri = ENCH_TYPE.createItemStack();
            ri.setData(DataComponentTypes.STORED_ENCHANTMENTS,
                lwi.getData(DataComponentTypes.ENCHANTMENTS));
            e.setResult(ri);
        } else if (ItemUtil.is(lwi, BOOK_TYPE)) {//down is book
            if (!canStrip(upi)) return;
            final ItemStack ri = ENCH_TYPE.createItemStack();
            ri.setData(DataComponentTypes.STORED_ENCHANTMENTS,
                upi.getData(DataComponentTypes.ENCHANTMENTS));
            e.setResult(ri);
        }
    }

    private static boolean canStrip(final ItemStack it) {
        if (ItemUtil.isBlank(it, false)) return false;
        final ItemEnchantments ies = it.getData(DataComponentTypes.ENCHANTMENTS);
        return ies != null && !ies.enchantments().isEmpty()
            && Integer.valueOf(0).equals(it.getData(DataComponentTypes.DAMAGE));
    }

    private static final int OMEN_TIME = 1000;
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onIntr(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        switch (e.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK:
                final ItemStack it = e.getItem();
                if (ItemUtil.isBlank(it, false)) break;
                switch (it.getType()) {
                    case GOAT_HORN:
                        if (p.hasCooldown(it)) break;
                        final PotionEffect oml = p.getPotionEffect(PotionEffectType.BAD_OMEN);
                        p.removePotionEffect(PotionEffectType.BAD_OMEN);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.BAD_OMEN, 1000,
                            oml == null ? 0 : oml.getAmplifier() + 1, true, true, true));
                        p.getInventory().setItem(e.getHand(), it.subtract());
                        break;
                    case DIAMOND_HOE, IRON_HOE, WOODEN_HOE,
                         GOLDEN_HOE, NETHERITE_HOE, STONE_HOE:
                        final Block b = e.getClickedBlock();
                        if (b != null && b.getBlockData() instanceof Ageable) {
                            final Ageable ag = (Ageable) b.getBlockData().clone();
                            if (ag.getAge() == ag.getMaximumAge()) {
                                b.breakNaturally(it);
                                ag.setAge(0);
                                b.setBlockData(ag, false);
                                p.swingHand(e.getHand());
                                p.damageItemStack(e.getHand(), 1);
                            }
                        }
                        break;
                    default:
                        break;
                }
        }
    }

    @EventHandler
    public void onAnvil(final PrepareAnvilEvent e) {
        final ItemStack it = e.getResult();
        if (ItemUtil.isBlank(it, false)) return;
        if (!it.hasData(DataComponentTypes.REPAIR_COST)) return;
        it.resetData(DataComponentTypes.REPAIR_COST);
        e.setResult(it);
    }

    @EventHandler
    public void onFlow(final BlockFromToEvent e) {
        if (!BlockUtil.is(e.getBlock(), BlockType.LAVA)) return;
        e.setCancelled(e.getFace() != BlockFace.DOWN
            && !e.getToBlock().getRelative(BlockFace.DOWN).getType().isSolid());
    }

    @EventHandler
    public void onTrans(final EntityTransformEvent e) {
        if (e.getTransformReason() != EntityTransformEvent.TransformReason.CURED
            || !(e.getTransformedEntity() instanceof final Villager vg)) return;
        for (final Entry<UUID, Reputation> en : vg.getReputations().entrySet()) {
            final Reputation rp = en.getValue();
            rp.setReputation(ReputationType.MAJOR_POSITIVE, 0);
            en.setValue(rp);
        }
    }

    private static final int SPAWN_DST = 120, DST_DEL = 8;

    @EventHandler
    public void onSpawn(final CreatureSpawnEvent e) {
        final LivingEntity cr = e.getEntity();
        final EntityEquipment eq = cr.getEquipment();
        if (!(cr instanceof Mob)) return;
        switch (e.getSpawnReason()) {
            case SPELL, SPAWNER, TRIAL_SPAWNER: return;
        }
        for (final EquipmentSlot es : EquipmentSlot.values()) {
            eq.setItem(es, Main.air);
            eq.setDropChance(es, 0.1f);
        }

        final BVec loc = BVec.of(e.getLocation());
        final int dstSq = loc.distAbs(cr.getWorld().getSpawnLocation()) >> (DST_DEL
            - (cr.getWorld().getEnvironment() == World.Environment.NETHER ? 3 : 0));
        final Player near = LocUtil.getClsChEnt(loc, SPAWN_DST, Player.class, null);
        if (near == null) {
            cr.remove();
            return;
        }
//        near.sendMessage("d=" + dstSq + " w=" + (dstSq >> WEAR_DEL));
        final boolean inVeh = cr.isInsideVehicle() || Main.srnd.nextBoolean();
        switch (cr.getType()) {
            case WARDEN:
                ((Warden) cr).setAnger(near, 120);
                cr.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(10d);
                cr.getAttribute(Attribute.MAX_HEALTH).setBaseValue(80d);
                break;
            case DROWNED, GIANT, HUSK, ZOMBIE, ZOMBIE_VILLAGER:
                wearChance(cr, dstSq, new ItemType[] {ItemType.LEATHER_BOOTS, ItemType.LEATHER_LEGGINGS, ItemType.LEATHER_CHESTPLATE, ItemType.LEATHER_HELMET, ItemType.WOODEN_AXE, inVeh ? ItemType.WOODEN_SPEAR : ItemType.WOODEN_SWORD},
                    new ItemType[] {ItemType.COPPER_BOOTS, ItemType.COPPER_LEGGINGS, ItemType.COPPER_CHESTPLATE, ItemType.COPPER_HELMET, ItemType.COPPER_AXE, inVeh ? ItemType.COPPER_SPEAR : ItemType.COPPER_SWORD},
                    new ItemType[] {ItemType.IRON_BOOTS, ItemType.IRON_LEGGINGS, ItemType.IRON_CHESTPLATE, ItemType.IRON_HELMET, ItemType.IRON_AXE, inVeh ? ItemType.IRON_SPEAR : ItemType.IRON_SWORD, ItemType.SHIELD},
                    new ItemType[] {ItemType.DIAMOND_BOOTS, ItemType.DIAMOND_LEGGINGS, ItemType.DIAMOND_CHESTPLATE, ItemType.DIAMOND_HELMET, ItemType.DIAMOND_AXE, inVeh ? ItemType.DIAMOND_SPEAR : ItemType.DIAMOND_SWORD, ItemType.SHIELD});
                break;
            case PIGLIN:
                wearChance(cr, dstSq, new ItemType[] {ItemType.GOLDEN_BOOTS, ItemType.LEATHER_LEGGINGS, ItemType.GOLDEN_CHESTPLATE, ItemType.LEATHER_HELMET, ItemType.CROSSBOW, ItemType.GOLDEN_SWORD},
                    new ItemType[] {ItemType.GOLDEN_BOOTS, ItemType.GOLDEN_LEGGINGS, ItemType.GOLDEN_CHESTPLATE, ItemType.GOLDEN_HELMET, ItemType.CROSSBOW, inVeh ? ItemType.GOLDEN_SPEAR : ItemType.GOLDEN_SWORD},
                    new ItemType[] {ItemType.NETHERITE_BOOTS, ItemType.GOLDEN_LEGGINGS, ItemType.NETHERITE_CHESTPLATE, ItemType.GOLDEN_HELMET, inVeh ? ItemType.GOLDEN_SPEAR : ItemType.NETHERITE_SWORD, ItemType.CROSSBOW, ItemType.SHIELD},
                    new ItemType[] {ItemType.NETHERITE_BOOTS, ItemType.NETHERITE_LEGGINGS, ItemType.NETHERITE_CHESTPLATE, ItemType.NETHERITE_HELMET, inVeh ? ItemType.NETHERITE_SPEAR : ItemType.NETHERITE_SWORD, ItemType.CROSSBOW, ItemType.SHIELD});
                break;
            case SKELETON, STRAY, PARCHED, BOGGED:
                wearChance(cr, dstSq, new ItemType[] {ItemType.LEATHER_BOOTS, ItemType.LEATHER_LEGGINGS, ItemType.LEATHER_CHESTPLATE, ItemType.LEATHER_HELMET, ItemType.WOODEN_SWORD},
                    new ItemType[] {ItemType.CHAINMAIL_BOOTS, ItemType.CHAINMAIL_LEGGINGS, ItemType.CHAINMAIL_CHESTPLATE, ItemType.CHAINMAIL_HELMET, ItemType.BOW, ItemType.STONE_SWORD},
                    new ItemType[] {ItemType.IRON_BOOTS, ItemType.IRON_LEGGINGS, ItemType.IRON_CHESTPLATE, ItemType.IRON_HELMET, ItemType.BOW, ItemType.IRON_SWORD, ItemType.SHIELD},
                    new ItemType[] {ItemType.DIAMOND_BOOTS, ItemType.DIAMOND_LEGGINGS, ItemType.DIAMOND_CHESTPLATE, ItemType.DIAMOND_HELMET, ItemType.BOW, ItemType.DIAMOND_SWORD, ItemType.SHIELD});
                if (ItemUtil.isBlank(cr.getEquipment().getItemInMainHand(), false)) cr.getEquipment().setItemInMainHand(ItemType.BOW.createItemStack());
                break;
            case WITHER_SKELETON:
                wearChance(cr, dstSq, new ItemType[] {ItemType.GOLDEN_BOOTS, ItemType.GOLDEN_LEGGINGS, ItemType.GOLDEN_CHESTPLATE, ItemType.GOLDEN_HELMET, ItemType.BOW, ItemType.GOLDEN_SWORD},
                    new ItemType[] {ItemType.GOLDEN_BOOTS, ItemType.NETHERITE_LEGGINGS, ItemType.GOLDEN_CHESTPLATE, ItemType.NETHERITE_HELMET, ItemType.BOW, ItemType.NETHERITE_SWORD},
                    new ItemType[] {ItemType.NETHERITE_BOOTS, ItemType.GOLDEN_LEGGINGS, ItemType.NETHERITE_CHESTPLATE, ItemType.GOLDEN_HELMET, ItemType.BOW, ItemType.GOLDEN_SWORD, ItemType.SHIELD},
                    new ItemType[] {ItemType.NETHERITE_BOOTS, ItemType.NETHERITE_LEGGINGS, ItemType.NETHERITE_CHESTPLATE, ItemType.NETHERITE_HELMET, ItemType.BOW, ItemType.NETHERITE_SWORD, ItemType.SHIELD});
                break;
            case VINDICATOR, VEX, PIGLIN_BRUTE:
                wearChance(cr, dstSq, new ItemType[] {ItemType.STONE_AXE, ItemType.GOLDEN_AXE},
                    new ItemType[] {ItemType.GOLDEN_AXE, ItemType.IRON_AXE},
                    new ItemType[] {ItemType.IRON_AXE, ItemType.DIAMOND_AXE},
                    new ItemType[] {ItemType.DIAMOND_AXE, ItemType.NETHERITE_AXE});
                break;
            case ILLUSIONER:
                cr.getEquipment().setItemInMainHand(ItemType.BOW.createItemStack());
                break;
            case PILLAGER:
                cr.getEquipment().setItemInMainHand(ItemType.CROSSBOW.createItemStack());
                break;
            case AXOLOTL, BLAZE, BREEZE, CAVE_SPIDER, CREEPER, ELDER_GUARDIAN,
                 ENDERMITE, EVOKER, GHAST, GUARDIAN, HOGLIN, IRON_GOLEM, MAGMA_CUBE,
                 POLAR_BEAR, RAVAGER, SHULKER, SILVERFISH, SKELETON_HORSE, CAMEL_HUSK,
                 SLIME, SNOW_GOLEM, SPIDER, WITCH, WOLF, ZOGLIN, ZOMBIE_HORSE:
                break;
            case PHANTOM:
                ((Phantom) cr).setSize(Main.srnd.nextInt(4));
                break;
            case VILLAGER:
                if (e.getSpawnReason() != SpawnReason.BREEDING ||
                    e.getLocation().getBlock().getLightFromSky() > BREED_LIGHT) break;
                e.setCancelled(true);
                cr.remove();
                return;
            case ZOMBIFIED_PIGLIN:
                wearChance(cr, dstSq, new ItemType[] {ItemType.LEATHER_BOOTS, ItemType.LEATHER_LEGGINGS, ItemType.LEATHER_CHESTPLATE, ItemType.LEATHER_HELMET, ItemType.GOLDEN_SWORD},
                    new ItemType[] {ItemType.GOLDEN_BOOTS, ItemType.LEATHER_LEGGINGS, ItemType.GOLDEN_CHESTPLATE, ItemType.LEATHER_HELMET, ItemType.GOLDEN_SWORD, ItemType.GOLDEN_SPEAR},
                    new ItemType[] {ItemType.GOLDEN_BOOTS, ItemType.GOLDEN_LEGGINGS, ItemType.GOLDEN_CHESTPLATE, ItemType.GOLDEN_HELMET, ItemType.NETHERITE_SWORD, ItemType.GOLDEN_SPEAR, ItemType.SHIELD},
                    new ItemType[] {ItemType.NETHERITE_BOOTS, ItemType.GOLDEN_LEGGINGS, ItemType.NETHERITE_CHESTPLATE, ItemType.GOLDEN_HELMET, ItemType.NETHERITE_AXE, ItemType.NETHERITE_SPEAR, ItemType.SHIELD});
                break;
            case HAPPY_GHAST:
                modGhast((HappyGhast) cr);
                return;
            case ENDERMAN:
                cr.setCanPickupItems(false);
                if (e.getSpawnReason() != SpawnReason.NATURAL) break;
                if (LocUtil.getChEnts(loc, FARM_DST,
                    cr.getClass(), null).size() < FARM_MAX) break;
                e.setCancelled(true);
                cr.remove();
                return;
            default:
                return;
        }

        if (cr instanceof Raider) {
            final boolean pl = ((Raider) cr).isPatrolLeader() && e.getSpawnReason() != SpawnReason.RAID;
            if (pl) {
                cr.getEquipment().setHelmet(bnr, false);
                cr.getEquipment().setHelmetDropChance(1f);
            } else {
                ((Raider) cr).setPatrolLeader(false);
            }
        }

        final AttributeInstance hp = cr.getAttribute(Attribute.MAX_HEALTH);
        if (hp != null) {
            hp.addModifier(new AttributeModifier(IStrap.key("hp_buff"),
                0.1d * dstSq, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
            cr.setHealth(hp.getValue());
        }
        final AttributeInstance dmg = cr.getAttribute(Attribute.ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(dmg.getBaseValue() * (0.06d * dstSq + 1d));
        final AttributeInstance spd = cr.getAttribute(Attribute.MOVEMENT_SPEED);
        if (spd != null) spd.setBaseValue(spd.getBaseValue() * (0.01d * dstSq + 1d));
        final AttributeInstance flw = cr.getAttribute(Attribute.FOLLOW_RANGE);
        if (flw != null) flw.setBaseValue(flw.getBaseValue() * (0.1d * dstSq + 1d));
        final AttributeInstance wtr = cr.getAttribute(Attribute.WATER_MOVEMENT_EFFICIENCY);
        if (wtr != null) wtr.setBaseValue(wtr.getBaseValue() * (0.1d * dstSq + 1d));
        final AttributeInstance scl = cr.getAttribute(Attribute.SCALE);
        if (scl != null) scl.setBaseValue(scl.getBaseValue()
            * (0.01d * dstSq - 0.1d) * Main.srnd.nextFloat() + 1d);
    }

    private void modGhast(final HappyGhast gh) {
        final AttributeInstance spd = gh.getAttribute(Attribute.MOVEMENT_SPEED);
        if (spd != null) spd.setBaseValue(spd.getBaseValue() * 1.4d);
        final AttributeInstance fly = gh.getAttribute(Attribute.FLYING_SPEED);
        if (fly != null) fly.setBaseValue(fly.getBaseValue() * 1.4d);
        final AttributeInstance eff = gh.getAttribute(Attribute.MOVEMENT_EFFICIENCY);
        if (eff != null) eff.setBaseValue(eff.getBaseValue() * 1.4d);
    }

    private static final Enchantment[] WEAPON = {Enchantment.FLAME, Enchantment.POWER, Enchantment.PUNCH,
            Enchantment.KNOCKBACK, Enchantment.SHARPNESS, Enchantment.UNBREAKING, Enchantment.FIRE_ASPECT},
        ARMOR = {Enchantment.UNBREAKING, Enchantment.PROTECTION, Enchantment.THORNS,
            Enchantment.PROJECTILE_PROTECTION, Enchantment.DEPTH_STRIDER};

    private static final int WEAR_DEL = 1;
    private static void wearChance(final LivingEntity le, final int dstSq, final ItemType[]... mts) {
        final int wear = dstSq >> WEAR_DEL;
        if (wear == 0) return;
        final EntityEquipment eq = le.getEquipment();
        for (final ItemType mt : mts[Math.min(mts.length - 1, Main.srnd.nextInt(wear))]) {
            if (Main.srnd.nextBoolean()) continue;
            final Equippable es = mt.getDefaultData(DataComponentTypes.EQUIPPABLE);
            final EquipmentSlot slot = es == null ? EquipmentSlot.HAND : es.slot();
            eq.setItem(slot, enchanted(mt, slot, wear), false);
        }
    }

    private static final int ENCH_DEL = 1;
    private static ItemStack enchanted(final ItemType mt, final EquipmentSlot slot, final int wear) {
        final ItemStack it = mt.createItemStack();
        final Enchantment[] enchs;
        if (slot.isHand()) enchs = WEAPON;
        else if (slot.isArmor()) enchs = ARMOR;
        else return it;
        final int ench = wear >> ENCH_DEL;
        if (ench == 0) return it;
        ClassUtil.shuffle(enchs);
        final ItemBuilder ib = new ItemBuilder(it);
        for (int i = Math.min(ench, enchs.length - 1) - 1; i >= 0; i--) {
            if (Main.srnd.nextBoolean() || !enchs[i].canEnchantItem(it)) continue;
            ib.enchant(enchs[i], Main.srnd.nextInt(ench) + 1);
        }
        return ib.build();
    }

    /*private static final int chSt = 10;
    private void wearChance(final LivingEntity le, final int dstSq, final ItemType[]... mts) {
        if (dstSq == 0) return;
        for (int i = mts.length - 1; i >= 0; i--) {
            if (Main.srnd.nextInt(chSt << i) > dstSq) continue;
            final EntityEquipment eq = le.getEquipment();
            for (final ItemType mt : mts[i]) {
                if (Main.srnd.nextBoolean()) continue;
                final Equippable es = mt.getDefaultData(DataComponentTypes.EQUIPPABLE);
                eq.setItem(es == null ? EquipmentSlot.HAND : es.slot(), mt.createItemStack(), false);
            }
            return;
        }
    }*/

    @EventHandler
    public void onSleep(final PlayerBedEnterEvent e) {
        final Player pl = e.getPlayer();
        if (((pl.getWorld().getFullTime() / 24000l) & 3) == 0) {
            pl.sendMessage(TCUtil.form(Main.PREFIX + "<red>You can't sleep every 4th night!"));
            e.setUseBed(Result.DENY);
        }
    }

    @EventHandler
    public void onDragon(final EnderDragonChangePhaseEvent e) {
        DragonBoss.onStage(e);
    }

    public static final double VEL_MUL = 0.025d;
    public static final double POP_MUL = 0.4d;
    public static final double ANGLE = 20d;
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBoost(final PlayerElytraBoostEvent e) {
        final Firework fw = e.getFirework();
        final FireworkMeta fm = fw.getFireworkMeta();
        final int length = fw.getTicksToDetonate();
        final int es = fm.getEffectsSize();
        fw.remove();
        final Player p = e.getPlayer();
        if (!usingFirework(p, es)) return;
        p.playSound(net.kyori.adventure.sound.Sound.sound(Key.key("entity.firework_rocket.launch"),
                net.kyori.adventure.sound.Sound.Source.AMBIENT, 10f, Main.srnd.nextFloat() * 0.2f + 0.8f),
            net.kyori.adventure.sound.Sound.Emitter.self());
        final WeakReference<Player> prf = new WeakReference<>(p);
        new BukkitRunnable() {
            final Vector dif = new Vector(Main.srnd.nextFloat() - 0.5f,
                Main.srnd.nextFloat() - 0.5f, Main.srnd.nextFloat() - 0.5f).multiply(0.8d);
            int tick = 0;
            public void run() {
                final Player pl = prf.get();
                if (pl == null || !pl.isValid()) {
                    cancel();
                    return;
                }

                final Location loc = pl.getEyeLocation().add(dif);
                final Vector dir = loc.getDirection();
                loc.add(dir).add(dir);

                if (!pl.isGliding() || Nms.fastType(pl.getWorld(), BVec.of(loc)).hasCollision()) {
                    if (es != 0) {
                        pl.launchProjectile(Firework.class,
                            new Vector(), f -> f.setFireworkMeta(fm)).detonate();
                    }
                    cancel();
                    return;
                }

                if (tick++ == length) {
                    final int es = fm.getEffectsSize();
                    if (es != 0) {
                        pl.setNoDamageTicks(4);
                        pl.launchProjectile(Firework.class, new Vector(),
                            f -> f.setFireworkMeta(fm)).detonate();
                        pl.setVelocity(pl.getVelocity().add(dir.rotateAroundNonUnitAxis(
                                new Vector(-dir.getZ(), 0d, dir.getX()).normalize(), ANGLE)
                            .multiply((es + 1) * POP_MUL)));
                    }
                    cancel();
                    return;
                }
                new ParticleBuilder(Particle.FIREWORK).location(loc)
                    .receivers(100).count(1).extra(0d).spawn();
                pl.setVelocity(pl.getVelocity().add(dir.multiply(VEL_MUL)));
            }
        }.runTaskTimer(Main.plug, 0, 0);
    }

    private boolean usingFirework(final Player p, final int size) {
        final PlayerInventory inv = p.getInventory();
        final ItemStack hnd = inv.getItemInMainHand();
        final Fireworks fdh = hnd.getData(DataComponentTypes.FIREWORKS);
        if (fdh != null && fdh.effects().size() == size) {
            if (p.getGameMode() != GameMode.CREATIVE)
                inv.setItemInMainHand(hnd.subtract());
            return true;
        }
        final ItemStack ofh = inv.getItemInOffHand();
        final Fireworks fdo = hnd.getData(DataComponentTypes.FIREWORKS);
        if (fdo != null && fdo.effects().size() == size) {
            if (p.getGameMode() != GameMode.CREATIVE)
                inv.setItemInOffHand(ofh.subtract());
            return true;
        }
        return false;
    }

    private static final double THRESH = 0.1d;
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onGlide(final EntityToggleGlideEvent e) {
        final Entity ent = e.getEntity();
        final Vector vec = ent.getVelocity();
        if (vec.getX() * vec.getX() + vec.getZ() * vec.getZ() < THRESH) return;
        ent.setVelocity(vec.multiply(THRESH));
    }
}