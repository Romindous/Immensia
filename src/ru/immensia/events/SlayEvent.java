package ru.immensia.events;

public class SlayEvent extends Event {/*

    public static final NamespacedKey MOB_KEY = IStrap.key("slay");

    protected SlayEvent(final RollTree drops) {
        super(drops);
    }

    public Progress setup(final BVec loc) {
        return new SlayProg(Main.world, loc);
    }

    public void start(final Progress prg) {}

    private static final int MAX_SPAWN = 2;
    public void secTick(final Progress prg) {
        final int dst = prg.event().inside();
        for (int i = Main.srnd.nextInt(MAX_SPAWN); i != 0; i--) {
            final BVec loc = new LocFinder(prg.loc.add(Main.srnd.nextInt(dst << 1) - dst,
                0, Main.srnd.nextInt(dst << 1) - dst)).find(LocFinder.DYrect.BOTH, 1, 1);
            if (loc == null) continue;
            final Mob mb = prg.world.spawn(loc.center(prg.world), rndMonster());
            mb.getPersistentDataContainer().set(MOB_KEY, PersistentDataType.BOOLEAN, true);
            Nms.colorGlow(mb, Event.NMCLR, true);
            //TODO sound
        }
    }

    private Class<? extends Mob> rndMonster() {
        return switch (Main.srnd.nextInt(20)) {
            case 0 -> Vindicator.class;
            case 1 -> Evoker.class;
            case 2 -> Witch.class;
            case 3, 4 -> Husk.class;
            case 5, 6 -> Bogged.class;
            case 7, 8 -> Stray.class;
            case 9, 10 -> ZombieVillager.class;
            case 11, 12, 13, 14 -> Skeleton.class;
            default -> Zombie.class;
        };
    }

    public void end(final Progress prg) {
        for (final Mob mb : LocUtil.getChEnts(prg.loc, outside(),
            Mob.class, m -> m.getPersistentDataContainer().has(MOB_KEY))) {
            mb.remove();
        }
    }

    public String schem() {
        return "slay";
    }

    public String name() {
        return "Вторжение Нежити";
    }

    public String desc() {
        return "Убей монстров, порождающихся рядом!";
    }

    public int waitTime() {
        return 30 * 60;
    }

    public int eventTime() {
        return 10 * 60;
    }

    public int inside() {
        return 20;
    }

    public int outside() {
        return 40;
    }

    public class SlayProg extends Progress {

        private static final int MAX_RANK = 3;

        private final HashMap<UUID, Integer> kills = new HashMap<>();
        private final UUID[] ranks = new UUID[MAX_RANK];

        protected SlayProg(final World w, final BVec loc) {
            super(w, loc);
        }

        protected Event event() {
            return SlayEvent.this;
        }

        protected String stats() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Монстров Убито:");
            for (int i = 0; i < ranks.length; i++) {
                final UUID id = ranks[i];
                if (id == null) {
                    sb.append("\n<dark_gray>- ...");
                    break;
                }
                final Player pl = Bukkit.getPlayer(id);
                if (pl == null) continue;
                sb.append("\n<gray>- ").append(placeClr(i)).append(pl.getName())
                    .append(": ").append(kills.getOrDefault(id, 0));
            }
            return sb.toString();
        }

        protected void addKill(final Player pl, final int amt) {
            if (amt < 1) return;
            final UUID id = pl.getUniqueId();
            final Integer total = kills.getOrDefault(id, 0) + amt;
            kills.put(id, total);

            int rank = 0;
            for (; rank != ranks.length; rank++) {
                if (id.equals(ranks[rank])) break;
                if (ranks[rank] == null) {
                    ranks[rank] = id;
                    break;
                }
            }

            for (rank--; rank >= 0; rank--) {
                if (kills.getOrDefault(ranks[rank], 0) >= total) break;
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
            for (final Mob mb : LocUtil.getChEnts(loc, outside(),
                Mob.class, m -> m.getPersistentDataContainer().has(MOB_KEY))) {
                mb.remove();
            }
            final TextDisplay tds = disRef.get();
            if (tds == null) return;
            tds.remove();
        }
    }
*/}
