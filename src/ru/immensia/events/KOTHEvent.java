package ru.immensia.events;

public class KOTHEvent extends Event {/*

    protected KOTHEvent(final RollTree drops) {
        super(drops);
    }

    public Progress setup(final BVec loc) {
        return new KOTHProg(Main.world, loc);
    }

    public void start(final Progress prg) {}

    public void secTick(final Progress prg) {
        if (!(prg instanceof KOTHProg dp)) return;
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            final Integer pt = dp.points.get(pl.getUniqueId());
            if (pt == null || !pl.isGlowing()) {
                if (!pl.isValid() || !dp.isInside(pl)) continue;
                Nms.colorGlow(pl, Event.NMCLR, false);
                dp.addPoints(pl, 1);
                //TODO sound
                continue;
            }
            if (!pl.isValid() || !dp.isInside(pl)) {
                pl.setGlowing(false);
                //TODO sound
                continue;
            }
            dp.addPoints(pl, 1);
        }
    }

    public void end(final Progress prg) {}

    public String schem() {
        return "koth";
    }

    public String name() {
        return "Царь Горы";
    }

    public String desc() {
        return "Удерживай верхнюю точку!";
    }

    public int waitTime() {
        return 30 * 60;
    }

    public int eventTime() {
        return 10 * 60;
    }

    public int inside() {
        return 8;
    }

    public int outside() {
        return 40;
    }

    public class KOTHProg extends Progress {

        private static final int MAX_RANK = 3;

        private final HashMap<UUID, Integer> points = new HashMap<>();
        private final UUID[] ranks = new UUID[MAX_RANK];

        protected KOTHProg(final World w, final BVec loc) {
            super(w, loc);
        }

        protected Event event() {
            return KOTHEvent.this;
        }

        protected String stats() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Очки Удержания:");
            for (int i = 0; i < ranks.length; i++) {
                final UUID id = ranks[i];
                if (id == null) {
                    sb.append("\n<dark_gray>- ...");
                    break;
                }
                final Player pl = Bukkit.getPlayer(id);
                if (pl == null) continue;
                sb.append("\n<gray>- ").append(placeClr(i)).append(pl.getName())
                    .append(": ").append(points.getOrDefault(id, 0));
            }
            return sb.toString();
        }

        protected void addPoints(final Player pl, final int amt) {
            if (amt < 1) return;
            final UUID id = pl.getUniqueId();
            final Integer total = points.getOrDefault(id, 0) + amt;
            points.put(id, total);

            int rank = 0;
            for (; rank != ranks.length; rank++) {
                if (id.equals(ranks[rank])) break;
                if (ranks[rank] == null) {
                    ranks[rank] = id;
                    break;
                }
            }

            for (rank--; rank >= 0; rank--) {
                if (points.getOrDefault(ranks[rank], 0) >= total) break;
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
            final TextDisplay tds = disRef.get();
            if (tds != null) tds.remove();
        }
    }
*/}
