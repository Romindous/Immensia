package ru.immensia.events;

public class GatherEvent extends Event {/*

    public static final NamespacedKey REC_KEY = IStrap.key("gather");
    public static final Set<ItemType> REMOVED = Set.of(ItemType.BARRIER, ItemType.BEDROCK, ItemType.DEBUG_STICK,
        ItemType.COMMAND_BLOCK, ItemType.CHAIN_COMMAND_BLOCK, ItemType.REPEATING_COMMAND_BLOCK, ItemType.LIGHT,
        ItemType.COMMAND_BLOCK_MINECART, ItemType.VAULT, ItemType.TRIAL_SPAWNER, ItemType.SPAWNER);
    public static final ItemType[] ITEMS;

    static {
        final List<ItemType> its = IStrap.getAll(RegistryKey.ITEM);
        its.removeIf(REMOVED::contains);
        ITEMS = its.toArray(new ItemType[0]);
    }

    protected GatherEvent(final RollTree drops) {
        super(drops);
    }

    public Progress setup(final BVec loc) {
        return new GathProg(Main.world, loc);
    }

    public void start(final Progress prg) {
        if (!(prg instanceof final GatherEvent.GathProg gp)) return;
        gp.spawn();
    }

    public void secTick(final Progress prg) {
        if (!(prg instanceof final GatherEvent.GathProg gp)) return;
        if (gp.vill() == null) {
            gp.spawn(); return;
        }
        final Player pl = gp.grf.get();
        if (pl == null) return;
        if (!prg.isInside(pl)) {
            pl.sendMessage(TCUtil.form(Main.PREFIX + "Ты за пределами зоны сдачи!"));
            gp.grf = new WeakReference<>(null);
            return;
        }
        if (!gp.collect(pl)) {
            pl.sendMessage(TCUtil.form(Main.PREFIX + "Все ресурсы сданы сборщику!"));
            gp.grf = new WeakReference<>(null);
            return;
        }
        gp.addRec(pl, 1);
        //TODO sound
    }

    public void end(final Progress prg) {
        if (!(prg instanceof final GatherEvent.GathProg gp)) return;
        final AbstractVillager vl = gp.vrf.get();
        if (vl != null) vl.remove();
    }

    public String schem() {
        return "gather";
    }

    public String name() {
        return "Сдача Ресурсов";
    }

    public String desc() {
        return "Принеси ресурсы сборщику!";
    }

    public int waitTime() {
        return 30 * 60;
    }

    public int eventTime() {
        return 30 * 60;
    }

    public int inside() {
        return 8;
    }

    public int outside() {
        return 40;
    }

    public class GathProg extends Progress {

        private static final int MAX_RANK = 3;

        public final ItemType type;
        private final Set<DataComponentType> ddt;
        private final HashMap<UUID, Integer> recs = new HashMap<>();
        private final UUID[] ranks = new UUID[MAX_RANK];

        private WeakReference<AbstractVillager> vrf;
        private WeakReference<Player> grf;

        protected GathProg(final World w, final BVec loc) {
            super(w, loc);
            this.type = randItem();
            this.ddt = type.getDefaultDataTypes();
            this.vrf = new WeakReference<>(null);
            this.grf = new WeakReference<>(null);
        }

        private ItemType randItem() {
            return ClassUtil.rndElmt(ITEMS);
        }

        protected Event event() {
            return GatherEvent.this;
        }

        protected String stats() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Ресурсов Собрано:");
            for (int i = 0; i < ranks.length; i++) {
                final UUID id = ranks[i];
                if (id == null) {
                    sb.append("\n<dark_gray>- ...");
                    break;
                }
                final Player pl = Bukkit.getPlayer(id);
                if (pl == null) continue;
                sb.append("\n<gray>- ").append(placeClr(i)).append(pl.getName())
                    .append(": ").append(recs.getOrDefault(id, 0));
            }
            return sb.toString();
        }

        protected @Nullable AbstractVillager vill() {
            return vrf.get();
        }

        public void spawn() {
            this.vrf = new WeakReference<>(world.spawn(loc.center(world), WanderingTrader.class, wt -> {
                wt.customName(TCUtil.form("ПКМ - Сдать <lang:item.minecraft." + type.key().value() + ">"));
                wt.setCustomNameVisible(true);
                wt.setInvulnerable(true);
                wt.setCollidable(false);
                wt.setAI(false);
                Nms.colorGlow(wt, Event.NMCLR, false);
            }));
        }

        public boolean collect(final Player pl) {
            for (final ItemStack it : pl.getInventory()) {
                if (!ItemUtil.is(it, type)) continue;
                if (ddt.size() < it.getDataTypes().size()) continue;
                it.subtract();
                //TODO sound
                return true;
            }
            return false;
        }

        public void from(final Player pl) {
            grf = new WeakReference<>(pl);
        }

        protected void addRec(final Player pl, final int amt) {
            if (amt < 1) return;
            final UUID id = pl.getUniqueId();
            final Integer total = recs.getOrDefault(id, 0) + amt;
            recs.put(id, total);

            int rank = 0;
            for (; rank != ranks.length; rank++) {
                if (id.equals(ranks[rank])) break;
                if (ranks[rank] == null) {
                    ranks[rank] = id;
                    break;
                }
            }

            for (rank--; rank >= 0; rank--) {
                if (recs.getOrDefault(ranks[rank], 0) >= total) break;
                if (rank + 1 < ranks.length) ranks[rank + 1] = ranks[rank];
                ranks[rank] = id;
            }
            updateDis();
        }

        protected List<Player> winners() {
            final List<Player> pls = new ArrayList<>(ranks.length);
            for (final UUID rn : ranks) {
                if (rn == null) break;
                final Player pl = Bukkit.getPlayer(rn);
                if (pl != null) pls.add(pl);
            }
            return pls;
        }

        public void crash() {
            final AbstractVillager vl = vrf.get();
            if (vl != null) vl.remove();
            final TextDisplay tds = disRef.get();
            if (tds != null) tds.remove();
        }
    }
*/}
