package ru.immensia.items;

import java.util.*;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.*;
import org.bukkit.util.Vector;
import ru.immensia.Main;
import ru.immensia.boot.IStrap;
import ru.immensia.utils.ItemUtil;
import ru.immensia.utils.colors.TCUtil;


public class ItemManager implements Listener {

    public ItemManager() {
        reload();
    }

    public void reload() {
        HandlerList.unregisterAll(this);

        Bukkit.getPluginManager().registerEvents(this, Main.plug);
        Main.log("§2Предметы включены!");
    }

    public void onDisable() {
        Main.log("§6Предметы выключены!");
    }

    private static final Set<DamageType> DESTROY = Set.of(DamageType.OUT_OF_WORLD, DamageType.OUTSIDE_BORDER);
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRemove(final EntityRemoveEvent e) {
        if (e.getEntity() instanceof final Item ie
            && ie.getLocation().getBlockY() < ie.getWorld().getMinHeight()) {
            final SpecialItem si = SpecialItem.get(ie.getItemStack());
            if (si != null) si.destroy();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent e) {
        process(e.getEntity(), new Processor() {
            public void onGroup(final EquipmentSlot[] ess, final ItemGroup cm) {cm.onDefense(ess, e);}
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onDefense(es, e);}
        });
        if (e instanceof final EntityDamageByEntityEvent ee) {
            process(e.getDamageSource().getCausingEntity(), new Processor() {
                public void onGroup(final EquipmentSlot[] ess, final ItemGroup cm) {cm.onAttack(ess, ee);}
                public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onAttack(es, ee);}
            });
        }

        if (e.getEntity() instanceof final Item ie) {
            final SpecialItem si = SpecialItem.get(ie.getItemStack());
            if (si != null) {
                if (DESTROY.contains(e.getDamageSource().getDamageType())) {
                    si.destroy();
                    return;
                }
                e.setDamage(0d);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent e) {
        process(e.getPlayer(), new Processor() {
            public void onGroup(final EquipmentSlot[] ess, final ItemGroup cm) {cm.onInteract(ess, e);}
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onInteract(es, e);}
        });

        final SpecialItem si = SpecialItem.get(e.getItem());
        if (si == null || e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        e.setUseInteractedBlock(Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(final ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof final LivingEntity le)) return;
        process(le, new Processor() {
            public void onGroup(final EquipmentSlot[] ess, final ItemGroup cm) {cm.onShoot(ess, e);}
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onShoot(es, e);}
        });
    }

    protected static void process(final Entity ent, final Processor pc) {
        if (!(ent instanceof final LivingEntity le)) return;
        final HashMap<ItemGroup, List<EquipmentSlot>> cmp = new HashMap<>();
        final EntityEquipment eq = le.getEquipment();
        if (eq == null) return;
        for (final EquipmentSlot es : EquipmentSlot.values()) {
            if (!le.canUseEquipmentSlot(es)) continue;
            final ItemStack is = eq.getItem(es);
            if (SpecialItem.exist) {
                final SpecialItem spi = SpecialItem.get(is);
                if (spi != null) {
                    pc.onSpec(es, spi);
                    continue;
                }
            }
            if (!ItemGroup.exist || ItemUtil.isBlank(is, true)) continue;
            final ItemGroup cm = ItemGroup.get(is);
            if (cm == null) continue;
            final List<EquipmentSlot> ess = cmp.get(cm);
            if (ess == null) {
                final List<EquipmentSlot> nes = new ArrayList<>();
                nes.add(es);
                cmp.put(cm, nes);
            } else {
                ess.add(es);
            }
        }
        if (!ItemGroup.exist) return;
        for (final Map.Entry<ItemGroup, List<EquipmentSlot>> en : cmp.entrySet()) {
            pc.onGroup(en.getValue().toArray(new EquipmentSlot[0]), en.getKey());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(final EntityDropItemEvent e) {
        onDrop(e.getEntity(), e.getItemDrop());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(final PlayerDropItemEvent e) {
        onDrop(e.getPlayer(), e.getItemDrop());
    }

    private static void onDrop(final Entity ent, final Item drop) {
        final ItemStack it = drop.getItemStack();
        final SpecialItem si = SpecialItem.get(it);
        if (si == null) return;
        if (!si.crafted()) {
            drop.remove();
            si.info("DROP: Uncrafted item removed!");
            return;
        }

        if (si.own() instanceof LivingEntity le
            && le.getUniqueId() != ent.getUniqueId()) {
            drop.remove();
            si.info("DROP: Duplicate item removed!");
            return;
        }

        if (si.dropped()) {
            if (!(si.own() instanceof final Item ii)) {
                drop.remove();
                si.info("DROP: Undropped item removed!");
                return;
            }
            ii.remove();
            si.info("DROP: Duplicate item removed!");
        }

        si.info("DROP: Dropped item!");
        si.apply(drop);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPick(final EntityPickupItemEvent e) {
        final Item drop = e.getItem();
        final ItemStack it = drop.getItemStack();
        final SpecialItem si = SpecialItem.get(it);
        if (si == null) return;
        if (!si.crafted()) {
            drop.remove();
            e.setCancelled(true);
            si.info("PICK: Uncrafted item removed!");
            return;
        }

        if (!si.dropped()) {
            drop.remove();
            e.setCancelled(true);
            si.info("PICK: Undropped item removed!");
            return;
        }

        if (e.getEntityType() != EntityType.PLAYER) {
            drop.setPickupDelay(20);
            e.setCancelled(true);
            return;
        }

        if (si.own() instanceof final Item ii
            && ii.getEntityId() != drop.getEntityId()) {
            drop.remove();
            e.setCancelled(true);
            si.info("PICK: Duplicate item removed!");
            return;
        }

        si.obtain(e.getEntity(), it);
        si.info("PICK: " + e.getEntity().getName() + " picked up item!");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(final EntityTeleportEvent e) {
        if (e.getTo() == null) return;
        onLoad(new EntitiesLoadEvent(e.getTo().getChunk(), List.of(e.getEntity())));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoad(final EntitiesLoadEvent e) {
        for (final Entity en : e.getEntities()) {
            if (!(en instanceof final Item it)) continue;
            final SpecialItem si = SpecialItem.get(it.getItemStack());
            if (si == null) continue;

            if (!si.crafted()) {
                it.remove();
                si.info("LOAD: Uncrafted item removed!");
                continue;
            }

            if (si.own() instanceof final Item ii) {
                if (!si.dropped() || ii.getEntityId() != it.getEntityId()) {
                    it.remove();
                    si.info("LOAD: Duplicate item removed!");
                    continue;
                }
            } else if (!si.dropped()) {
                it.remove();
                si.info("LOAD: Undropped item removed!");
                continue;
            }

            si.info("LOAD: Loaded in item!");
            if (si.loc() == null) continue;
            final World w = si.loc().w();
            if (w == null) continue;
            final Location loc = si.loc().center(w);
            si.apply(it);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUnLoad(final EntitiesUnloadEvent e) {
        for (final Entity en : e.getEntities()) {
            if (!(en instanceof final Item it)) continue;
            final SpecialItem si = SpecialItem.get(it.getItemStack());
            if (si == null) continue;

            if (!si.crafted()) {
                it.remove();
                si.info("UNL: Uncrafted item removed!");
                continue;
            }

            if (si.own() instanceof final Item ii) {
                if (!si.dropped() || ii.getEntityId() != it.getEntityId()) {
                    it.remove();
                    si.info("UNL: Duplicate item removed!");
                    continue;
                }
            } else if (!si.dropped()) {
                it.remove();
                si.info("UNL: Undropped item removed!");
                continue;
            }

            it.setVelocity(new Vector());
            si.loc(it.getLocation());
            si.save(it.getItemStack());
            si.info("UNL: Unloaded item out!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onAtEntity(final PlayerInteractAtEntityEvent e) {
        onEntity(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntity(final PlayerInteractEntityEvent e) {
        switch (e.getRightClicked().getType()) {
            case GLOW_ITEM_FRAME, ITEM_FRAME, ARMOR_STAND, ALLAY, COPPER_GOLEM: break;
            default: return;
        }
        final SpecialItem si = SpecialItem.get(e.getPlayer()
            .getInventory().getItem(e.getHand()));
        if (si == null) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onHopper(final InventoryPickupItemEvent e) {
        final SpecialItem si = SpecialItem.get(e.getItem().getItemStack());
        if (si == null) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onIBreak(final PlayerItemBreakEvent e) {
        final SpecialItem si = SpecialItem.get(e.getBrokenItem());
        if (si == null) return;
        si.destroy();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onIGrind(final PrepareGrindstoneEvent e) {
        final GrindstoneInventory inv = e.getInventory();
        final ItemStack upi = inv.getUpperItem();
        final ItemStack lwi = inv.getLowerItem();
        final SpecialItem upsi = SpecialItem.get(upi);
        final SpecialItem lwsi = SpecialItem.get(lwi);
        if (upsi == null && lwsi == null) return;
        if (!ItemUtil.compare(upi, lwi, ItemUtil.Stat.TYPE)) return;
        e.setResult(ItemUtil.air);
    }

    private static final Set<ItemType> BUNDLES = IStrap.getAll(ItemTypeTagKeys.BUNDLES);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onClick(final InventoryClickEvent e) {
        if (e.getSlotType() == InventoryType.SlotType.RESULT) {
            if (!(e.getClickedInventory() instanceof CraftingInventory)) return;
            final ItemStack fin = e.getCurrentItem();
            final SpecialItem si = SpecialItem.get(fin);
            if (si == null) return;
            if (si.crafted()) {
                e.setResult(Event.Result.DENY);
                e.setCurrentItem(ItemUtil.air);
                for (final HumanEntity he : e.getViewers()) {
                    he.sendMessage(TCUtil.form(Main.PREFIX + "<red>This relic is already crafted!"));
                }
                return;
            }
            si.info("CRAFT: " + e.getWhoClicked().getName() + " crafted item!");
            si.obtain(e.getWhoClicked(), fin);
            return;
        }
        final HumanEntity he = e.getWhoClicked();
        final ItemStack it = switch (e.getClick()) {
            case NUMBER_KEY -> he.getInventory().getItem(e.getHotbarButton());
            case SWAP_OFFHAND -> he.getInventory().getItemInOffHand();
            default -> e.getCurrentItem();
        };
        final ItemStack cr = e.getCursor();
        /*Ostrov.log_bools("try", e.getClick().isLeftClick(), ItemUtil.is(cr, ItemType.BUNDLE), ItemUtil.is(it, ItemType.BUNDLE),
            it != null, cr.hasData(DataComponentTypes.DAMAGE), it.hasData(DataComponentTypes.DAMAGE));*/
        if (e.getClick().isLeftClick() && it != null && (cr.hasData(DataComponentTypes.DAMAGE) || it.hasData(DataComponentTypes.DAMAGE))
            && (BUNDLES.contains(cr.getType().asItemType()) || BUNDLES.contains(it.getType().asItemType()))) {
            he.sendMessage(TCUtil.form(Main.PREFIX + "<red>You can't put this in a bundle!"));
            e.setResult(Event.Result.DENY);
            return;
        }
        final Inventory inv = e.getView().getTopInventory();
        switch (inv.getType()) {
            case PLAYER, CREATIVE, CRAFTING, ENCHANTING, ANVIL, GRINDSTONE: return;
        }
        if (SpecialItem.get(it) != null) {
            he.sendMessage(TCUtil
                .form(Main.PREFIX + "<red>You can't move relics around!"));
            e.setResult(Event.Result.DENY);
        }
    }

    protected interface Processor extends GroupProc, SpecProc {}
    public interface GroupProc {
        void onGroup(final EquipmentSlot[] ess, final ItemGroup cm);
    }
    public interface SpecProc {
        void onSpec(final EquipmentSlot es, final SpecialItem si);
    }

    public static boolean isCustom(final ItemStack it) {
        return it != null && ((ItemGroup.exist && ItemGroup.get(it) != null)
            || (SpecialItem.exist && SpecialItem.get(it) != null));
    }
}
