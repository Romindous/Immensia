package ru.immensia.entities.mobs;

import net.kyori.adventure.util.TriState;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.DragonBattle;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.util.Vector;
import ru.immensia.Main;
import ru.immensia.entities.CustomEntity;
import ru.immensia.entities.Mobs;
import ru.immensia.utils.EntityUtil;
import ru.immensia.utils.NumUtil;
import ru.immensia.utils.colors.TCUtil;
import ru.immensia.utils.locs.AreaSpawner;
import ru.immensia.utils.locs.BVec;
import ru.immensia.utils.locs.LocUtil;

public class DragonBoss extends CustomEntity {

    private static final int MAX_HP = 1000;
    private static final int DFB_DY = 16;
    private static final int HP_DEL = MAX_HP >> 4;
    private static final int PL_DST = 80;

    public DragonBoss() {
        super();
        Bukkit.getScheduler().runTaskTimer(Main.plug, () -> {
            for (final World w : Bukkit.getWorlds()) {
                for (final EnderDragon mb : w.getEntitiesByClass(EnderDragon.class)) {
                    if (!mb.isValid()) continue;
                    switch (mb.getPhase()) {
                        case BREATH_ATTACK, HOVER, SEARCH_FOR_BREATH_ATTACK_TARGET: continue;
                        default: break;
                    }
                    final BVec loc = BVec.of(mb.getLocation());
                    if (Main.srnd.nextInt(((int) mb.getHealth() / HP_DEL) + 2) != 0) break;
                    if (LocUtil.getNearPl(loc, PL_DST, p -> !p.isInvulnerable()) == null) break;
                    final DragonFireball ndf = mb.getWorld().spawn(loc.add(NumUtil.rndSignNum(8, 16),
                            DFB_DY + Main.srnd.nextInt(DFB_DY), NumUtil.rndSignNum(8, 16)).center(mb.getWorld()),
                        DragonFireball.class, CreatureSpawnEvent.SpawnReason.SPELL);
                    final Location flc = ndf.getLocation();
                    ndf.setDirection(new Vector((loc.x - flc.getBlockX()) * (Main.srnd.nextFloat() * 0.1f + 0.1f),
                        -DFB_DY * (Main.srnd.nextFloat() * 0.4f + 0.2f), (loc.z - flc.getBlockZ()) * (Main.srnd.nextFloat() * 0.1f + 0.1f)));
                    ndf.setYield(3f);
                    ndf.setIsIncendiary(true);
                    ndf.setVisualFire(TriState.TRUE);
                    ndf.setShooter(mb);
                    EntityUtil.effect(ndf, Sound.ENTITY_WIND_CHARGE_WIND_BURST,
                        0.6f, Particle.SQUID_INK);
                }
            }
        }, 10, 20);
    }

    protected AreaSpawner spawner() {
        return null;
    }

    protected Class<? extends LivingEntity> getEntClass() {
        return EnderDragon.class;
    }

    protected int spawnCd() {
        return -1;
    }

    protected boolean canBe(final Entity ent, final CreatureSpawnEvent.SpawnReason rsn) {
        return true;
    }

    protected void modify(final Entity ent) {
        if (!(ent instanceof final EnderDragon mb)) return;
        mb.getAttribute(Attribute.MAX_HEALTH).setBaseValue(MAX_HP);
        mb.setHealth(MAX_HP);
    }

    protected void onAttack(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Phantom) {
            e.setCancelled(true);
            e.setDamage(0d);
        }
    }

    protected void onHurt(final EntityDamageEvent e) {
        if (!(e.getEntity() instanceof final EnderDragon dr)) return;
        if (e.getDamageSource().getCausingEntity() == null) return;
        if (dr.getPhase() == EnderDragon.Phase.CIRCLING)
            dr.setPhase(EnderDragon.Phase.STRAFING);
        if (e.getDamageSource().getDamageType() == DamageType.MACE_SMASH)
            e.setDamage(Math.min(e.getDamage(), Mobs.MAX_MACE_DMG));
    }

    protected void onDeath(final EntityDeathEvent e) {
        e.setDroppedExp(e.getDroppedExp() * 10);
    }

    protected void onTarget(final EntityTargetEvent e) {}

    protected void onShoot(final ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof final DragonFireball df)) return;
        df.setYield(3f);
        df.setIsIncendiary(true);
        df.setVisualFire(TriState.TRUE);
    }

    private static final int CHLD_DST = 200;
    private static final int MAX_CHLDS = 8;
    public static void onStage(final EnderDragonChangePhaseEvent e) {
        final EnderDragon drg = e.getEntity();
        final DragonBattle db = drg.getDragonBattle();
        if (db == null) return;
        setCustoms(drg, db);
        switch (e.getNewPhase()) {
            case LEAVE_PORTAL:
                final Location loc = drg.getLocation();
                int chlds = LocUtil.getChEnts(BVec.of(loc), CHLD_DST,
                    EnderDragon.class, null).size();
                for (; chlds < MAX_CHLDS; chlds++) spawnChild(loc);
                break;
            case FLY_TO_PORTAL, BREATH_ATTACK:
                if (drg.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
                    e.setNewPhase(EnderDragon.Phase.CHARGE_PLAYER);
                }
                break;
            case LAND_ON_PORTAL:
                final Location plc = db.getEndPortalLocation();
                if (plc == null) break;
                if (LocUtil.getClsChEnt(plc.add(0.5d, 5d, 0.5d), 1d, EnderCrystal.class, null) == null)
                    plc.getWorld().spawn(plc, EnderCrystal.class).setShowingBottom(true);
                break;
            case DYING:
                for (final Phantom ph : LocUtil.getChEnts(BVec.of(drg.getLocation()),
                    CHLD_DST, Phantom.class, null)) {
                    EntityUtil.effect(ph, Sound.ENTITY_PHANTOM_HURT, 0.6f, Particle.CLOUD);
                    ph.remove();
                }
                break;
        }
    }

    private static void setCustoms(final EnderDragon drg, final DragonBattle db) {
        drg.customName(TCUtil.form("§5§lМать Драконья"));
        db.getBossBar().setTitle("§5§lМать Драконья");
        db.getBossBar().setStyle(BarStyle.SEGMENTED_12);
    }

    private static final double CHLD_SIZE = 1.6d;
    private static final double CHLD_SPEED = 1.6d;
    private static final double CHLD_DMG = 1d;
    private static final double CHLD_KB = 2d;

    public static Phantom spawnChild(final Location loc) {
        final Phantom ch = loc.getWorld().spawn(loc.clone().add(NumUtil.rndSignNum(1, 2),
                Main.srnd.nextInt(8), NumUtil.rndSignNum(1, 2)), Phantom.class,
            CreatureSpawnEvent.SpawnReason.BREEDING, false, ph -> {
                attriMul(ph, Attribute.SCALE, CHLD_SIZE);
                attriMul(ph, Attribute.FLYING_SPEED, CHLD_SPEED);
                attriMul(ph, Attribute.ATTACK_KNOCKBACK, CHLD_KB);
                attriMul(ph, Attribute.ATTACK_DAMAGE, CHLD_DMG);
                ph.setInvisible(true);
            });
        EntityUtil.effect(ch, Sound.ENTITY_PHANTOM_AMBIENT,
            0.8f, Particle.SQUID_INK);
        ch.setMaximumNoDamageTicks(40);
        ch.setNoDamageTicks(40);
        ch.setTarget(LocUtil.getNearPl(BVec.of(loc),
            PL_DST, p -> !p.isInvulnerable()));
        return ch;
    }

    private static void attriMul(final LivingEntity le, final Attribute att, final double mul) {
        final AttributeInstance ai = le.getAttribute(att);
        if (ai != null) {
            ai.setBaseValue(ai.getBaseValue() * mul);
            return;
        }
        le.registerAttribute(att);
        final AttributeInstance nai = le.getAttribute(att);
        if (nai == null) return;
        nai.setBaseValue(nai.getBaseValue() * mul);
    }
}
