package ru.immensia.events;

public class CTFEvent extends Event {/*

    protected CTFEvent(final RollTree drops) {
        super(drops);
    }

    public Progress setup(final BVec loc) {
        return new CTFProg(Main.world, loc);
    }

    public void start(final Progress prg) {}

    private static final ItemStack BANNER = ItemType.MAGENTA_BANNER.createItemStack();
    public void secTick(final Progress prg) {
        if (!(prg instanceof CTFProg dp)) return;
        final Player curr = dp.fcr.get();
        if (curr != null) {
            if (curr.isValid() && dp.isOutside(curr)) return;
            dp.loose(curr);
            return;
        }
        if (dp.fds.get() == null) dp.spawn();
        final Player nxt = LocUtil.getClsChEnt(dp.loc,
            inside(), Player.class, Player::isValid);
        if (nxt == null) return;
        dp.hold(nxt);
    }

    public void end(final Progress prg) {}

    public String schem() {
        return "ctf";
    }

    public String name() {
        return "Захват Флага";
    }

    public String desc() {
        return "Продержи флаг до конца!";
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
        return 20;
    }

    public class CTFProg extends Progress {

        private WeakReference<BlockDisplay> fds;
        private WeakReference<Player> fcr;

        protected CTFProg(final World w, final BVec loc) {
            super(w, loc);
            this.fds = new WeakReference<>(null);
            this.fcr = new WeakReference<>(null);
        }

        protected Event event() {
            return CTFEvent.this;
        }

        protected String stats() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Владелец Флага:\n" + Event.CLR);
            final Player pl = fcr.get();
            sb.append(pl == null ? "<dark_gray>-----" : pl.getName());
            return sb.toString();
        }

        private static final BlockData FLAG = BlockType.MAGENTA_BANNER.createBlockData();
        public void spawn() {
            this.fds = new WeakReference<>(world.spawn(loc.center(world), BlockDisplay.class, bd -> {
                bd.customName(TCUtil.form(Event.CLR + "Флаг - подойди и забери его!"));
                bd.setCustomNameVisible(true);
                bd.setInvulnerable(true);
                bd.setBillboard(Display.Billboard.FIXED);
                bd.setBlock(FLAG);
                bd.setGlowColorOverride(Event.COLOR);
                bd.setGlowing(true);
                bd.setViewRange(80f);
            }));
        }

        protected void loose(final Player pl) {
            pl.sendMessage(TCUtil.form(Main.PREFIX
                + Event.CLR + "Флаг сброшен т.к. ты вне зоны события!"));
            pl.setGlowing(false);
            final PlayerInventory inv = pl.getInventory();
            inv.setHelmet(inv.getHelmet());
            pl.updateInventory();
            fcr = new WeakReference<>(null);
            spawn();
            updateDis();
            //TODO sound
        }

        protected void hold(final Player pl) {
            Nms.colorGlow(pl, Event.NMCLR, false);
            Nms.sendFakeEquip(pl, 39, BANNER);
            pl.sendMessage(TCUtil.form(Main.PREFIX
                + Event.CLR + "Флаг у тебя, сохрани его!"));
            final BlockDisplay bd = fds.get();
            if (bd != null) bd.remove();
            fds = new WeakReference<>(null);
            updateDis();
            //TODO sound
        }

        protected List<Player> winners() {
            final Player pl = fcr.get();
            return pl == null ? List.of() : List.of(pl);
        }

        public void crash() {
            final BlockDisplay bd = fds.get();
            if (bd != null) bd.remove();
            final Player cr = fcr.get();
            if (cr != null) cr.remove();
            final TextDisplay tds = disRef.get();
            if (tds != null) tds.remove();
        }
    }
*/}
