package ru.immensia.entities.mobs;

import java.util.Collection;
import java.util.EnumSet;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.immensia.Main;
import ru.immensia.boot.IStrap;
import ru.immensia.entities.CustomEntity;
import ru.immensia.entities.Mobs;
import ru.immensia.utils.EntityUtil;
import ru.immensia.utils.NumUtil;
import ru.immensia.utils.locs.AreaSpawner;
import ru.immensia.utils.locs.BVec;
import ru.immensia.utils.locs.LocFinder;
import ru.immensia.utils.locs.LocUtil;
import ru.immensia.utils.strings.TCUtil;

public class WitherBoss extends CustomEntity {

    public static final AreaSpawner MINI_AS = new AreaSpawner() {
        private static final LocFinder.Check[] MINI_CHECK = {
            (LocFinder.TypeCheck) (bt, y) -> bt.isSolid(),
            (LocFinder.TypeCheck) (bt, y) -> LocUtil.isPassable(bt),
            (LocFinder.TypeCheck) (bt, y) -> LocUtil.isPassable(bt),
            (LocFinder.TypeCheck) (bt, y) -> LocUtil.isPassable(bt)
        };

        protected int radius() {return 8;}
        protected int offset() {return 2;}
        protected int yDst() {return 1;}

        protected LocFinder.Check[] checks() {
            return MINI_CHECK;
        }

        public SpawnCondition condition(final BVec loc) {
            return new SpawnCondition(1, CreatureSpawnEvent.SpawnReason.SPELL);
        }
    };

    protected AreaSpawner spawner() {
        return null;
    }

    protected Class<? extends LivingEntity> getEntClass() {
        return Wither.class;
    }

    protected int spawnCd() {
        return -1;
    }

    protected boolean canBe(final Entity ent, final CreatureSpawnEvent.SpawnReason rsn) {
        return true;
    }

    protected void modify(final Entity ent) {
        if (!(ent instanceof final Wither mb)) return;
        Bukkit.getMobGoals().addGoal(mb, 0, new Goal<>() {

            private static final GoalKey<Wither> KEY = GoalKey.of(Wither.class, IStrap.key("wither"));
            private static final EnumSet<GoalType> TYPES = EnumSet.noneOf(GoalType.class);

            private static final int STUCK_TICKS = 100;

            private int tick;
            private int locStamp;
            private BVec lastLoc;

            public boolean shouldActivate() {
                return true;
            }

            public void tick() {
                if (!mb.isValid() || mb.getInvulnerableTicks() != 0 || (tick++ & 7) != 0) return;
                if (lastLoc == null) {
                    lastLoc = BVec.of(mb.getLocation());
                    return;
                }
                final BVec curr = BVec.of(mb.getLocation());
                if (lastLoc.distAbs(curr) == 0) {
                    if (tick - locStamp > STUCK_TICKS)
                        warpToPl(curr);
                    return;
                }
                lastLoc = curr;
                locStamp = tick;
            }

            private static final int PL_SEARCH = 40;

            private void warpToPl(final BVec curr) {
                final Player pl = LocUtil.getClsChEnt(curr, PL_SEARCH, Player.class, null);
                if (pl == null) return;
                final Location loc = pl.getLocation().add(NumUtil.rndSignNum(4, 8),
                    0d, NumUtil.rndSignNum(4, 8));
                mb.teleport(loc);
                mb.getWorld().createExplosion(mb, 4f, true, true);
                EntityUtil.effect(mb, Sound.ENTITY_WITHER_BREAK_BLOCK, 0.6f, Particle.SQUID_INK);
            }

            @Override
            public GoalKey<Wither> getKey() {
                return KEY;
            }

            @Override
            public EnumSet<GoalType> getTypes() {
                return TYPES;
            }
        });
        setCustoms(mb);
    }

    private static void setCustoms(final Wither mb) {
        mb.customName(TCUtil.form("§8§lThe Withering Remnant"));
        mb.getBossBar().setTitle("§8§lThe Withering Remnant");
        mb.getBossBar().setStyle(BarStyle.SEGMENTED_6);
        mb.getBossBar().setColor(BarColor.WHITE);
    }

    public static final PotionEffect INF_EFF = new PotionEffect(PotionEffectType.INFESTED,
        1000, 1, true, false, true);
    private static final double VAMP_MUL = 0.4d;
    @Override
    protected void onAttack(final EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof LivingEntity le)) return;
        if (!(e.getDamageSource().getCausingEntity() instanceof Mob mb)) return;
        le.removePotionEffect(PotionEffectType.INFESTED);
        le.addPotionEffect(INF_EFF);
        mb.heal(e.getDamage() * VAMP_MUL,
            EntityRegainHealthEvent.RegainReason.MAGIC_REGEN);
        EntityUtil.effect(mb, Sound.BLOCK_SOUL_SAND_BREAK,
            0.6f, Particle.DAMAGE_INDICATOR);
        if (le instanceof AbstractSkeleton) {
            e.setDamage(0d);
            e.setCancelled(true);
        }
    }

    private static final int MINI_DST = 20;

    @Override
    protected void onHurt(final EntityDamageEvent e) {
        if (!(e.getEntity() instanceof final Wither mb)) return;
        if (!(e.getDamageSource().getCausingEntity() instanceof final LivingEntity dmgr)) return;
        final Collection<WitherSkeleton> minis = LocUtil.getChEnts(mb.getLocation(),
            MINI_DST, WitherSkeleton.class, null);
        final int msz = minis.size();
        if (msz != 0 && Main.srnd.nextInt(msz) != 0) return;
        for (final WitherSkeleton ws : MINI_AS.trySpawn(BVec
            .of(mb.getLocation()), WitherSkeleton.class, null)) {
            EntityUtil.effect(ws, Sound.ENTITY_WITHER_SKELETON_DEATH,
                1.4f, Particle.LARGE_SMOKE);
            if (!dmgr.isInvulnerable()) ws.setTarget(dmgr);
            if (!mb.isCharged() && Main.srnd.nextBoolean()) continue;
            final EntityEquipment eq = ws.getEquipment();
            eq.setItemInMainHand(ItemType.BOW.createItemStack());
            eq.setItemInMainHandDropChance(0f);
        }
        if (e.getDamageSource().getDamageType() == DamageType.MACE_SMASH)
            e.setDamage(Math.min(e.getDamage(), Mobs.MAX_MACE_DMG));
    }

    @Override
    protected void onDeath(final EntityDeathEvent e) {
        e.setDroppedExp(e.getDroppedExp() * 10);
    }

    @Override
    protected void onTarget(final EntityTargetEvent e) {

    }

    @Override
    protected void onShoot(final ProjectileLaunchEvent e) {

    }
}
