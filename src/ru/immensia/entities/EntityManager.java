package ru.immensia.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.immensia.Main;
import ru.immensia.utils.locs.BVec;

public class EntityManager implements Listener {

    protected static BukkitTask task = null;
    protected static final HashMap<String, CustomEntity> custom = new HashMap<>();
    protected static final List<CustomEntity> spawns = new ArrayList<>();

    public EntityManager() {
        reload();
    }

    public static void register(final CustomEntity ce) {
        custom.put(ce.key.value(), ce);
        if (ce.spawner() != null) spawns.add(ce);
    }

    public void reload() {
        if (task != null) task.cancel();
        HandlerList.unregisterAll(this);

        Main.log("§2Сущности включены!");
        Bukkit.getPluginManager().registerEvents(this, Main.plug);
        task = new BukkitRunnable() {
            @Override
            public void run() {
                final Collection<? extends Player> pls = Bukkit.getOnlinePlayers();
                if (pls.isEmpty()) return;
                final ArrayList<BVec> locs = new ArrayList<>(pls.size());
                for (final Player p : pls) locs.add(BVec.of(p.getLocation()));

                for (final CustomEntity ce : spawns) {
                    if (ce.cd < 0) continue;
                    if (ce.cd == 0) {
                        ce.cd = ce.spawnCd();
                        for (final BVec lc : locs) {
                            ce.spawner().trySpawn(lc, ce.getEntClass(), ce::apply);
                        }
                        continue;
                    }
                    ce.cd--;
                }
            }
        }.runTaskTimer(Main.plug, 1, 1);
    }

    public void onDisable() {
        Main.log("§6Сущности выключены!");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(final CreatureSpawnEvent e) {
        final Entity ent = e.getEntity();
        final CustomEntity he = CustomEntity.get(ent);
        if (he != null) return;
        for (final CustomEntity ce : custom.values()) {
            if (ce.getEntClass().isInstance(ent)
                && ce.canBe(ent, e.getSpawnReason())) {
                ce.apply(ent);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent e) {
        final CustomEntity he = CustomEntity.get(e.getEntity());
        if (he != null) he.onHurt(e);
        final Entity dmgr = e.getDamageSource().getCausingEntity();
        if (dmgr != null && e instanceof EntityDamageByEntityEvent) {
            final CustomEntity de = CustomEntity.get(dmgr);
            if (de != null) de.onAttack((EntityDamageByEntityEvent) e);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(final EntityDeathEvent e) {
        final CustomEntity he = CustomEntity.get(e.getEntity());
        if (he != null) he.onDeath(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTarget(final EntityTargetEvent e) {
        final CustomEntity he = CustomEntity.get(e.getEntity());
        if (he != null) he.onTarget(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShoot(final ProjectileLaunchEvent e) {
        final CustomEntity he = CustomEntity.get(e.getEntity());
        if (he != null) he.onShoot(e);
    }
}
