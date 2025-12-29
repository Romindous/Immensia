package ru.immensia.events;

public class DropEvent extends Event {/*

    protected DropEvent(final RollTree drops) {
        super(drops);
    }

    public Progress setup(final BVec loc) {
        return new DropProg(Main.world, loc);
    }

    public void start(final Progress prg) {}

    private static final int CHANCE = 4;
    private static final LocFinder.Check[] CHECKS = new LocFinder.Check[]{
        (LocFinder.TypeCheck) (bt, y) -> bt.isSolid(),
        (LocFinder.TypeCheck) (bt, y) -> LocUtil.isPassable(bt),
        (LocFinder.TypeCheck) (bt, y) -> LocUtil.isPassable(bt),
        (LocFinder.TypeCheck) (bt, y) -> LocUtil.isPassable(bt)};
    public void secTick(final Progress prg) {
        if (!(prg instanceof DropProg dp)) return;
        dp.cycle();

        if (Main.srnd.nextInt(CHANCE) == 0) return;
        final int dst = prg.event().inside();
        final BVec loc = new LocFinder(prg.loc.add(Main.srnd.nextInt(dst << 1) - dst,
            100, Main.srnd.nextInt(dst << 1) - dst), CHECKS).find(LocFinder.DYrect.DOWN, 1, 1);
        if (loc == null) return;
        dp.drop(loc);
    }

    public void end(final Progress prg) {
        if (!(prg instanceof DropProg dp)) return;
        for (final BVec cl : dp.conts) {
            final Block cb = cl.block(dp.world);
            if (cb.getType() != DropProg.CONT.getMaterial()) continue;
            cb.breakNaturally(true);
        }
    }

    public String schem() {
        return "drop";
    }

    public String name() {
        return "Сброс Ящиков";
    }

    public String desc() {
        return "Собирай лут с падающих ящиков!";
    }

    public int waitTime() {
        return 30 * 60;
    }

    public int eventTime() {
        return 5 * 60;
    }

    public int inside() {
        return 28;
    }

    public int outside() {
        return 40;
    }

    public record Drop(BlockDisplay[] bds, BVec loc, int finTime) {}

    public class DropProg extends Progress {

        private static final int FALL_TIME = 15;
        private static final int FALL_DST = 80;
        public static final BlockData CONT = BlockType.BARREL.createBlockData();

        private final List<Drop> animates = new ArrayList<>();
        private final Set<BVec> conts = new HashSet<>();

        protected DropProg(final World w, final BVec loc) {
            super(w, loc);
        }

        protected Event event() {
            return DropEvent.this;
        }

        protected String stats() {
            return "";
        }

        private static final BlockData CHAIN = BlockType.COPPER_CHAIN.createBlockData();
        private static final BlockData CARPET = BlockType.ORANGE_CARPET.createBlockData();
        protected void drop(final BVec dlc) {
            final int time = Main.srnd.nextInt(FALL_TIME) + FALL_TIME;
            animates.add(new Drop(new BlockDisplay[] {
                disBlock(dlc, CONT, 0f, 0f, 0f, time),
                disBlock(dlc.add(0, 1, 0), CHAIN, 0f, 0f, 0f, time),
                disBlock(dlc.add(0, 2, 0), CHAIN, 0f, 0f, 0f, time),
                disBlock(dlc.add(0, 3, 0), CARPET, 0f, 0.1f, 0f, time),
                disBlock(dlc.add(1, 3, 0), CARPET, 0f, 0.1f, -0.1f, time),
                disBlock(dlc.add(1, 3, 1), CARPET, 0.1f, -0.1f, -0.1f, time),
                disBlock(dlc.add(0, 3, 1), CARPET, 0.1f, 0.1f, 0f, time),
                disBlock(dlc.add(-1, 3, 1), CARPET, 0.1f, -0.1f, 0.1f, time),
                disBlock(dlc.add(-1, 3, 0), CARPET, 0f, -0.1f, 0.1f, time),
                disBlock(dlc.add(-1, 3, -1), CARPET, -0.1f, -0.1f, 0.1f, time),
                disBlock(dlc.add(0, 3, -1), CARPET, -0.1f, 0.1f, 0f, time),
                disBlock(dlc.add(1, 3, -1), CARPET, -0.1f, 0.1f, -0.1f, time)
            }, dlc, Timer.secTime() + time));
        }

        private BlockDisplay disBlock(final BVec dlc, final BlockData data,
            final float rX, final float dY, final float rZ, final int time) {
            final BlockDisplay bd = world.spawn(dlc.center(world), BlockDisplay.class, b -> {
                final Transformation tr = b.getTransformation();
                final Transformation ntr = new Transformation(new Vector3f(0f, dY + FALL_DST, 0f),
                    tr.getLeftRotation(), tr.getScale(), new Quaternionf(rX, 0f, rZ, 0f));
                b.setBillboard(Display.Billboard.FIXED);
                b.setTransformation(ntr);
                b.setViewRange(100f);
                b.setBlock(data);
            });
            bd.setInterpolationDelay(0);
            bd.setInterpolationDuration(time);
            final Transformation tr = bd.getTransformation();
            final Transformation ntr = new Transformation(new Vector3f(0f, dY, 0f),
                tr.getLeftRotation(), tr.getScale(), new Quaternionf(rX, 0f, rZ, 0f));
            bd.setTransformation(ntr);
            bd.setGlowColorOverride(Event.COLOR);
            bd.setGlowing(true);
            return bd;
        }

        protected void cycle() {
            for (final Iterator<Drop> it = animates.iterator(); it.hasNext();) {
                final Drop dr = it.next();
                final int sec = Timer.secTime();
                if (sec < dr.finTime) continue;
                for (final BlockDisplay bd : dr.bds) bd.remove();
                cont(dr.loc);
            }
        }

        private static final int FILL = 8;
        private void cont(final BVec loc) {
            final Block bl = loc.block(world);
            bl.setBlockData(CONT, true);
            if (!(bl.getState() instanceof final Container cn)) return;
            final Inventory inv = cn.getInventory();
            final ItemStack[] its = new ItemStack[inv.getSize()];
            for (int i = 0; i != FILL; i++)
                its[i] = drops.genRoll(ItemStack.class);
            inv.setContents(ClassUtil.shuffle(its));
            cn.update();
            conts.add(loc);
            world.spawn(loc.center(world), Firework.class, fw -> {
                final FireworkMeta fm = fw.getFireworkMeta();
                fm.addEffect(FW_EFFECT);
                fw.setFireworkMeta(fm);
            }).detonate();
            //TODO sound
        }

        protected List<Player> winners() {
            return List.of();
        }

        public void crash() {
            for (final BVec cl : conts) {
                final Block cb = cl.block(world);
                if (cb.getType() != DropProg.CONT.getMaterial()) continue;
                cb.setBlockData(BlockUtil.air, false);
            }
            for (final Drop dr : animates) {
                for (final BlockDisplay bd : dr.bds) {
                    bd.remove();
                }
            }
            final TextDisplay tds = disRef.get();
            if (tds != null) tds.remove();
        }
    }
*/}
