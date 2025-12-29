package ru.immensia.events;

public abstract class Progress {/*
    private static final BossBar.Overlay OV_WAIT = BossBar.Overlay.PROGRESS;
    private static final BossBar.Color CLR_WAIT = BossBar.Color.PURPLE;
    private static final BossBar.Overlay OV_EVENT = BossBar.Overlay.NOTCHED_20;
    private static final BossBar.Color CLR_EVENT = BossBar.Color.PINK;
//    private static final StringUtil.Split SPLIT = StringUtil.Split.MEDIUM;
    private static final int DIS_SIZE = 20;
    private static final int DIS_DY = 20;
    private static final int SIZE_DEL = 12;

    protected static final FireworkEffect FW_EFFECT = FireworkEffect.builder()
        .with(FireworkEffect.Type.STAR).withColor(Event.COLOR).trail(true).flicker(true).build();
    protected static final BossBar bar = BossBar
        .bossBar(TCUtil.form(""), 0f, CLR_WAIT, OV_WAIT);

    protected final WeakReference<TextDisplay> disRef;
    protected final World world;
    protected final int save;
    protected final BVec loc;
    protected boolean wait;
    protected int time;

    protected Progress(final World w, final BVec loc) {
        this.loc = loc;
        this.world = w;
        this.wait = true;
        final Event ev = event();
        this.time = ev.waitTime();
        this.disRef = new WeakReference<>(spawnDis());
        final Schematic sch = ev.schem;
        if (sch == null) save = -1;
        else {
            save = SaveMaticManager.save(sch.getCuboid().allign(loc), Main.world);
            sch.paste(Bukkit.getConsoleSender(), loc.w(w), false);
        }
        bar.name(TCUtil.form(TCUtil.A + ev.name() + TCUtil.P
            + " начнется через " + TCUtil.A + TimeUtil.secondToTime(time) + TCUtil.P + "! " + TCUtil.N
            + "(" + TCUtil.P + loc.x + " " + loc.y + " " + loc.z + TCUtil.N + ")"))
            .progress(0f).color(CLR_WAIT).overlay(OV_WAIT);
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            pl.showBossBar(bar);
            pl.sendMessage(TCUtil.form(Main.PREFIX + "Событие " + Event.CLR + ev.name() + TCUtil.N
                + " начнется на " + TCUtil.P + "(" + loc.x + ", " + loc.y + ", " + loc.z + ")\n"
                + TCUtil.N + "через " + (time / 60) + " мин. Цель События:\n" + TCUtil.P + ev.desc()));
            ScreenUtil.sendTitleDirect(pl, "", TCUtil.N +  Event.CLR + ev.name()
                + TCUtil.N + " начнется через " + (time / 60) + " мин!");
        }
    }

    private TextDisplay spawnDis() {
        return Main.world.spawn(loc.add(0, DIS_DY, 0)
            .center(Main.world), TextDisplay.class, t -> {
            t.text(TCUtil.form(TCUtil.P + "Начало через " + TCUtil.A
                + TimeUtil.secondToTime(time) + TCUtil.P + "!\n"));
            t.setBillboard(Display.Billboard.CENTER);
            t.setSeeThrough(true);
            t.setShadowed(true);
            t.setPersistent(true);
            final Transformation tr = t.getTransformation();
            t.setTransformation(new Transformation(tr.getTranslation(),
                tr.getLeftRotation(), new Vector3f(DIS_SIZE), tr.getRightRotation()));
            t.setInterpolationDuration(0);
            t.setInterpolationDelay(0);
            t.setViewRange(200f);
        });
    }

    protected abstract Event event();

    public void tick() {
        final Event ev = event();
        if (wait) {//waiting
            final int max = ev.waitTime();
            if (time-- == 0) {//start
                ev.start(this);
                for (final Player pl : Bukkit.getOnlinePlayers()) {
                    pl.sendMessage(TCUtil.form(Main.PREFIX + "Событие " + Event.CLR + ev.name() + TCUtil.N
                        + " началось на " + TCUtil.P + "(" + loc.x + ", " + loc.y + ", " + loc.z + ")\n"
                        + TCUtil.N + "Цель События:\n" + TCUtil.P + ev.desc()));
                    ScreenUtil.sendTitleDirect(pl, "", TCUtil.N + "Событие " + Event.CLR + ev.name() + TCUtil.N + " началось!");
                }
                bar.name(TCUtil.form(TCUtil.P + "Прогресс События " + TCUtil.A + ev.name() + TCUtil.P
                        + ": " + TCUtil.A + TimeUtil.secondToTime(time) + TCUtil.P + "! " + TCUtil.N
                        + "(" + TCUtil.P + loc.x + " " + loc.y + " " + loc.z + TCUtil.N + ")"))
                    .progress(1f).color(CLR_EVENT).overlay(OV_EVENT);
                time = ev.eventTime();
                updateDis();
                return;
            }
            //waiting tick
            bar.name(TCUtil.form(TCUtil.A + ev.name() + TCUtil.P
                    + " начнется через " + TCUtil.A + TimeUtil.secondToTime(time) + TCUtil.P + "! " + TCUtil.N
                    + "(" + TCUtil.P + loc.x + " " + loc.y + " " + loc.z + TCUtil.N + ")"))
                .progress((float) (max - time) / (float) max);
            updateDis();
            return;
        }
        //event
        if (time-- == 0) {//event end
            ev.end(this);
            final List<Player> wns = winners();
            final int wsz = wns.size();
            for (int i = 0; i != wsz; i++) {
                final Player pl = wns.get(i);
                if (!pl.isValid()) continue;
                final Location loc = EntityUtil.center(pl);
                world.spawn(loc, Firework.class, fw -> {
                    final FireworkMeta fm = fw.getFireworkMeta();
                    fm.addEffect(FW_EFFECT);
                    fw.setFireworkMeta(fm);
                });
                final int del = SIZE_DEL / wsz;
                for (int j = NumUtil.square(wsz - i) + del; j != 0; j--)
                    for (final ItemStack it : ev.drops.genRolls(ItemStack.class))
                        world.dropItemNaturally(loc, it, ii -> ii.setGlowing(true));
                //TODO sound
            }
            SaveMaticManager.load(save);
            EventManager.prog = null;
            for (final Player pl : Bukkit.getOnlinePlayers()) {
                if (loc.distSq(pl.getLocation()) < NumUtil.square(ev.outside()))
                    pl.sendMessage(TCUtil.form(Main.PREFIX + "Событие "
                        + Event.CLR + ev.name() + TCUtil.N + " закончилось!\nПобедители:" + stats()));
                else pl.sendMessage(TCUtil.form(Main.PREFIX + "Событие "
                        + Event.CLR + ev.name() + TCUtil.N + " закончилось!"));
                pl.hideBossBar(bar);
                //TODO sound
            }
            final TextDisplay tds = disRef.get();
            if (tds == null) return;
            tds.remove();
            return;
        }
        //event tick
        if (LocUtil.getClsChEnt(loc, ev.outside(), Player.class, null) != null) ev.secTick(this);
        bar.name(TCUtil.form(TCUtil.P + "Прогресс события " + TCUtil.A + ev.name() + TCUtil.P
                + ": " + TCUtil.A + TimeUtil.secondToTime(time) + TCUtil.P + "! " + TCUtil.N
                + "(" + TCUtil.P + loc.x + " " + loc.y + " " + loc.z + TCUtil.N + ")"))
            .progress((float) time / (float) ev.eventTime());
        updateDis();
    }

    public void updateDis() {
        final TextDisplay tds = disRef.get();
        if (tds == null) {
            Main.log_warn("Event display is null!");
            return;
        }
        if (wait) {
            tds.text(TCUtil.form(TCUtil.P + "Начало через " + TCUtil.A
                + TimeUtil.secondToTime(time) + TCUtil.P + "!\n"));
            return;
        }
        tds.text(TCUtil.form(Event.CLR + event().name() + "\n"
            + TCUtil.N + event().desc() + "\n" + TCUtil.A + stats()));
    }

    public boolean isInside(final LivingEntity le) {
        return BVec.of(le.getLocation()).distSq(loc)
            < NumUtil.square(event().inside());
    }

    public boolean isOutside(final LivingEntity le) {
        return BVec.of(le.getLocation()).distSq(loc)
            < NumUtil.square(event().outside());
    }

    protected abstract List<Player> winners();

    protected abstract String stats();

    public abstract void crash();

    protected static String placeClr(final int n) {
        return switch (n) {
            case 0 -> "<gold>";
            case 1 -> "<mithril>";
            case 2 -> "<amber>";
            default -> "<gray>";
        };
    }
*/}
