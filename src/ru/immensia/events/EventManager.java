package ru.immensia.events;

public class EventManager /*extends BukkitRunnable implements Listener*/ {/*

    private static final int MAX_DIST = 4000;
    private static final int PAUSE_SEC = 6 * 60 * 60;
//    private static int total_sec = Main.srnd.nextInt(PAUSE_SEC);

    public static Progress prog;

    protected static final RollTree DROPS = RollTree.of("event")
        .add(RollTree.of("event_epic")
            .add(new ItemRoll("debree", ItemType.ANCIENT_DEBRIS.createItemStack()))
            .add(new ItemRoll("notch", ItemType.ENCHANTED_GOLDEN_APPLE.createItemStack()))
            .add(new ItemRoll("core", ItemType.HEAVY_CORE.createItemStack()))
            .add(new ItemRoll("heart", ItemType.HEART_OF_THE_SEA.createItemStack()))
            .add(new ItemRoll("nautilus", ItemType.NAUTILUS_SHELL.createItemStack()))
            .add(new ItemRoll("totem", ItemType.TOTEM_OF_UNDYING.createItemStack()))
            .add(new ItemRoll("sniffer", ItemType.SNIFFER_EGG.createItemStack()))
            .build(1), 1)
        .add(RollTree.of("event_rare")
            .add(new ItemRoll("diamond", ItemType.DIAMOND.createItemStack(), 1, 2))
            .add(new ItemRoll("amerald", ItemType.EMERALD.createItemStack(), 1, 2))
            .add(new ItemRoll("exp", ItemType.EXPERIENCE_BOTTLE.createItemStack(), 1, 2))
            .add(new ItemRoll("tutel", ItemType.TURTLE_SCUTE.createItemStack(), 1, 2))
            .add(new ItemRoll("gapple", ItemType.GOLDEN_APPLE.createItemStack(), 1, 2))
            .add(new ItemRoll("shulker", ItemType.SHULKER_SHELL.createItemStack(), 1, 2))
            .add(new ItemRoll("end_eye", ItemType.ENDER_EYE.createItemStack(), 1, 2))
            .add(new ItemRoll("tear", ItemType.GHAST_TEAR.createItemStack(), 1, 2))
            .add(new ItemRoll("gcarrot", ItemType.GOLDEN_CARROT.createItemStack(), 1, 2))
            .add(new ItemRoll("sculk", ItemType.ECHO_SHARD.createItemStack(), 1, 2))
            .add(new ItemRoll("tflower", ItemType.TORCHFLOWER_SEEDS.createItemStack(), 1, 2))
            .add(new ItemRoll("pitchpod", ItemType.PITCHER_POD.createItemStack(), 1, 2))
            .add(new ItemRoll("upgrade", ItemType.NETHERITE_UPGRADE_SMITHING_TEMPLATE.createItemStack(), 1, 2))
            .add(new ItemRoll("pitchpod", ItemType.PITCHER_POD.createItemStack(), 1, 2))
            .add(new ItemRoll("om_key", ItemType.OMINOUS_TRIAL_KEY.createItemStack(), 1, 2))
            .add(new ItemRoll("wskull", ItemType.WITHER_SKELETON_SKULL.createItemStack(), 1, 2))
            .add(new ItemRoll("mending", new ItemBuilder(ItemType.ENCHANTED_BOOK)
                .enchant(Enchantment.MENDING, 1, true).build()))
            .add(new ItemRoll("looting", new ItemBuilder(ItemType.ENCHANTED_BOOK)
                .enchant(Enchantment.LOOTING, 2, true).build()))
            .add(new ItemRoll("protect", new ItemBuilder(ItemType.ENCHANTED_BOOK)
                .enchant(Enchantment.PROTECTION, 3, true).build()))
            .add(new ItemRoll("unbreak", new ItemBuilder(ItemType.ENCHANTED_BOOK)
                .enchant(Enchantment.UNBREAKING, 2, true).build()))
            .build(1), 2)
        .add(RollTree.of("event_norm")
            .add(new ItemRoll("lapis", ItemType.LAPIS_LAZULI.createItemStack(), 2, 4))
            .add(new ItemRoll("gold", ItemType.RAW_GOLD.createItemStack(), 2, 4))
            .add(new ItemRoll("iron", ItemType.IRON_INGOT.createItemStack(), 2, 4))
            .add(new ItemRoll("cobweb", ItemType.COBWEB.createItemStack(), 2, 4))
            .add(new ItemRoll("f_a_s", ItemType.FLINT_AND_STEEL.createItemStack()))
            .add(new ItemRoll("shield", ItemType.SHIELD.createItemStack()))
            .add(new ItemRoll("charge", ItemType.WIND_CHARGE.createItemStack(), 2, 4))
            .add(new ItemRoll("ameth", ItemType.AMETHYST_SHARD.createItemStack(), 2, 4))
            .add(new ItemRoll("prystal", ItemType.PRISMARINE_CRYSTALS.createItemStack(), 2, 4))
            .add(new ItemRoll("pshard", ItemType.PRISMARINE_SHARD.createItemStack(), 2, 4))
            .add(new ItemRoll("carrot", ItemType.CARROT.createItemStack(), 2, 4))
            .add(new ItemRoll("potato", ItemType.POTATO.createItemStack(), 2, 4))
            .add(new ItemRoll("pork", ItemType.COOKED_PORKCHOP.createItemStack(), 2, 4))
            .add(new ItemRoll("beef", ItemType.COOKED_BEEF.createItemStack(), 2, 4))
            .add(new ItemRoll("rabbit", ItemType.COOKED_RABBIT.createItemStack(), 2, 4))
            .add(new ItemRoll("phantom", ItemType.PHANTOM_MEMBRANE.createItemStack(), 2, 4))
            .add(new ItemRoll("copper", ItemType.COPPER_INGOT.createItemStack(), 2, 4))
            .add(new ItemRoll("book", ItemType.BOOK.createItemStack(), 2, 4))
            .add(new ItemRoll("chrus", ItemType.CHORUS_FRUIT.createItemStack(), 2, 4))
            .add(new ItemRoll("name_tag", ItemType.NAME_TAG.createItemStack(), 2, 4))
            .add(new ItemRoll("powder", ItemType.GUNPOWDER.createItemStack(), 2, 4))
            .add(new ItemRoll("chrus", ItemType.CHORUS_FRUIT.createItemStack(), 2, 4))
            .add(new ItemRoll("tr_key", ItemType.TRIAL_KEY.createItemStack(), 2, 4))
            .add(new ItemRoll("salmon", ItemType.COOKED_SALMON.createItemStack(), 2, 4))
            .add(new ItemRoll("cod", ItemType.COOKED_COD.createItemStack(), 2, 4))
            .add(new ItemRoll("honey", ItemType.HONEY_BOTTLE.createItemStack(), 2, 4))
            .add(new ItemRoll("cod", ItemType.RESIN_CLUMP.createItemStack(), 2, 4))
            .add(new ItemRoll("slime", ItemType.SLIME_BALL.createItemStack(), 2, 4))
            .add(new ItemRoll("quartz", ItemType.QUARTZ.createItemStack(), 2, 4))
            .add(new ItemRoll("milk", ItemType.MILK_BUCKET.createItemStack()))
            .build(1), 4)
        .build(1);
    public EventManager() {
        new GatherEvent(DROPS);
        new DropEvent(DROPS);
        new SlayEvent(DROPS);
        new KOTHEvent(DROPS);
        new CTFEvent(DROPS);
        runTaskTimer(Main.plug, 20l, 20l);
    }

    public void run() {
        //TODO finish
        *//*final int cnt = total_sec / PAUSE_SEC;
        total_sec++;
        if (cnt < 1) return;

        if (prog == null) {
            if (total_sec / PAUSE_SEC == cnt) return;
            final BVec loc = new LocFinder(BVec.of(Main.world, NumUtil.randInt(Main.LAND_DIST, MAX_DIST),
                Main.world.getMaxHeight(), NumUtil.randInt(Main.LAND_DIST, MAX_DIST))).find(LocFinder.DYrect.DOWN, 3, -5);
            if (loc == null) {
                total_sec--;
                return;
            }
            final Event ev = choose();
            if (ev == null) {
                Main.log_warn("No events found, skipping this cycle!");
                return;
            }
            prog = ev.setup(loc);
            return;
        }

        prog.tick();*//*
    }

    private static final Event[] emt = new Event[0];
    private Event choose() {
        final Event[] evs = Event.VALUES.values().toArray(emt);
        if (evs.length == 0) return null;
        return ClassUtil.rndElmt(evs);
    }

    @EventHandler
    public void onDeath(final EntityDeathEvent e) {
        final LivingEntity tgt = e.getEntity();
        switch (prog) {
            case final SlayEvent.SlayProg mk:
                if (!tgt.getPersistentDataContainer().has(SlayEvent.MOB_KEY)) return;
                if (!(EntityUtil.lastDamager(tgt, true) instanceof final Player pl)) return;
                mk.addKill(pl, 1);
                return;
            case null, default: break;
        }
    }

    @EventHandler
    public void onClick(final PlayerInteractEntityEvent e) {
        final Player pl = e.getPlayer();
        if (!(prog instanceof final GatherEvent.GathProg gp) || !gp.isInside(pl)) return;
        final AbstractVillager av = gp.vill();
        if (av == null || av.getEntityId() != e.getRightClicked().getEntityId()) return;
        e.setCancelled(true);
        if (!gp.collect(pl)) {
            pl.sendMessage(TCUtil.form(Main.PREFIX
                + "<gold>У тебя нету <yellow><lang:item.minecraft."
                + gp.type.key().value() + "><gold>!"));
            return;
        }
        pl.sendMessage(TCUtil.form(Main.PREFIX
            + "<apple>Ты в процессе сдачи <green><lang:item.minecraft."
            + gp.type.key().value() + "><apple>..."));
        gp.addRec(pl, 1);
        gp.from(pl);
        //TODO sound
    }

    @EventHandler
    public void onBreak(final BlockBreakEvent e) {
        final Player pl = e.getPlayer();
        if (prog == null || !prog.isOutside(pl)) return;
        e.setCancelled(true);
        pl.sendMessage(TCUtil.form(Main.PREFIX + "<red>Нельзя ломать блоки вблизи события!"));
    }

    @EventHandler
    public void onPlace(final BlockPlaceEvent e) {
        final Player pl = e.getPlayer();
        if (prog == null || !prog.isOutside(pl)) return;
        e.setBuild(false);
        pl.sendMessage(TCUtil.form(Main.PREFIX + "<red>Нельзя ставить блоки вблизи события!"));
    }
*/}
