package ru.immensia.events;

import org.bukkit.Keyed;

public abstract class Event /*implements Keyed*/ {/*

    public static final Map<Key, Event> VALUES = new HashMap<>();
    public static final NamedTextColor NMCLR = NamedTextColor.LIGHT_PURPLE;
    public static final Color COLOR = Color.FUCHSIA;
    public static final String CLR = "<pink>";

    private static final Path SCHEMS = Path.of(Main.plug.getDataPath().toString(), "schems");

    private final NamespacedKey key = IStrap.key(this.getClass().getSimpleName());

    protected final Schematic schem;
    protected final RollTree drops;

    protected Event(final RollTree drops) {
        this.drops = drops;
        final Path pth = Path.of(SCHEMS.toString(), schem() + Schematic.DEF_EXT);
        if (!Files.exists(pth) || !Files.isRegularFile(pth)) {
            Main.log_err("No schem found for event " + key.value());
            this.schem = null;
            return;
        }
        this.schem = new Schematic(Bukkit.getConsoleSender(), pth.toFile(), false);
        if (VALUES.put(key, this) != null) {
            Main.log_warn("Event " + key.value()
                + " is already registered!");
        }
    }

    public static Event get(final Key key) {
        return VALUES.get(key);
    }

    public abstract Progress setup(final BVec loc);

    public abstract void start(final Progress prg);

    public abstract void secTick(final Progress prg);

    public abstract void end(final Progress prg);

    public abstract String schem();

    public abstract String name();

    public abstract String desc();

    public abstract int waitTime();
    public abstract int eventTime();

    public abstract int inside();
    public abstract int outside();

    public NamespacedKey getKey() {
        return this.key;
    }
*/}
