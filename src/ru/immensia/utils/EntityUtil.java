package ru.immensia.utils;

import java.util.Collection;
import com.destroystokyo.paper.ParticleBuilder;
import io.papermc.paper.math.Position;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import ru.immensia.Main;
import ru.immensia.utils.strings.TCUtil;


public class EntityUtil {

    public static @Nullable LivingEntity lastDamager(final LivingEntity ent, final boolean owner) {
        final EntityDamageEvent e = ent.getLastDamageCause();
        if (e == null) return null;
        return getDamager(e, owner);
    }

    public static @Nullable LivingEntity getDamager(final EntityDamageEvent e, final boolean owner) {
      final DamageSource ds = e.getDamageSource();
      if (ds.getCausingEntity() instanceof final LivingEntity le) {
            if (le instanceof final Tameable tm && owner) {
                return tm.getOwner() instanceof HumanEntity ? ((HumanEntity) tm.getOwner()) : null;
            } else {
              return le;
            }
      } else if (ds instanceof Projectile prj) {
        final ProjectileSource ps = prj.getShooter();
        if (ps != null) {
          if (ps instanceof LivingEntity le) {
            return le;
          }
        }
        }
        return null;
    }

    private static final float MAX_EXH = 4f;
    public static boolean food(final HumanEntity he, float amt) {
        final float sat = he.getSaturation();
        if (sat != 0f) {
            if (sat >= amt) {
                he.setSaturation(sat - amt);
                return true;
            }
            he.setSaturation(0f);
            amt -= sat;
        }
        final int rem;
        final int food = (int) amt;
        final float exh = (amt - food) * MAX_EXH + he.getExhaustion(); //0 to 4
        if (exh > MAX_EXH) {
            he.setExhaustion(exh - MAX_EXH);
            rem = he.getFoodLevel() - food - 1;
        } else {
            he.setExhaustion(exh);
            rem = he.getFoodLevel() - food;
        }
        if (rem < 0) {
            he.setFoodLevel(0);
            return false;
        }
        he.setFoodLevel(rem);
        return true;
    }

    public static void indicate(final Location at, final String ind, final Player to) {
        final Location elc = to.getEyeLocation();
        final Location loc = new Location(at.getWorld(), 1.4d * at.getX() - 0.4d * elc.getX(),
            at.getY(), 1.4d * at.getZ() - 0.4d * elc.getZ());
        final float size = Math.max((float) elc.distanceSquared(loc) * 0.008f, 1.2f);
        final float dx = 0.5f - Main.srnd.nextFloat(), dz = 0.5f - Main.srnd.nextFloat();
        final float small = size * 0.4f;
        final TextDisplay tds = at.getWorld().spawn(loc, TextDisplay.class, td -> {
            final Transformation tm = td.getTransformation();
            td.setTransformation(new Transformation(new Vector3f(dz * small, small, dx * small),
                tm.getLeftRotation(), new Vector3f(size), tm.getRightRotation()));
            td.setBackgroundColor(Color.fromARGB(0));
            td.setBillboard(Display.Billboard.VERTICAL);
            td.setTextOpacity((byte) 0);
            td.setVisibleByDefault(false);
            td.text(TCUtil.form(ind));
            td.setSeeThrough(true);
            td.setShadowed(true);
            td.setViewRange(200f);
        });

        to.showEntity(Main.plug, tds);
        Main.sync(() -> {
            tds.setInterpolationDelay(0);
            tds.setInterpolationDuration(20);
            final Transformation tm = tds.getTransformation();
            tds.setTransformation(new Transformation(new Vector3f(dx * size, size, dz * size),
                tm.getLeftRotation(), new Vector3f(small), tm.getRightRotation()));
            tds.setTextOpacity((byte) -127);
            Main.sync(() -> tds.remove(), 24);
        }, 2);
    }

    public static void indicate(final Location at, final String ind, final Collection<Player> to) {
        double dst_sq = 0;
        for (final Player pl : to) dst_sq += pl.getEyeLocation().distanceSquared(at);
        final float size = Math.max((float) dst_sq / to.size() * 0.008f, 1.2f);
        final float dx = 0.5f - Main.srnd.nextFloat(), dz = 0.5f - Main.srnd.nextFloat();
        final float small = size * 0.4f;
        final TextDisplay tds = at.getWorld().spawn(at, TextDisplay.class, td -> {
            final Transformation tm = td.getTransformation();
            td.setTransformation(new Transformation(new Vector3f(dz * small, small, dx * small),
                tm.getLeftRotation(), new Vector3f(size), tm.getRightRotation()));
            td.setBackgroundColor(Color.fromARGB(0));
            td.setBillboard(Display.Billboard.VERTICAL);
            td.setTextOpacity((byte) 255);
            td.setVisibleByDefault(false);
            td.text(TCUtil.form(ind));
            td.setSeeThrough(true);
            td.setShadowed(true);
            td.setViewRange(200f);
        });

        for (final Player pl : to) pl.showEntity(Main.plug, tds);
        Main.sync(() -> {
            tds.setInterpolationDelay(0);
            tds.setInterpolationDuration(20);
            final Transformation tm = tds.getTransformation();
            tds.setTransformation(new Transformation(new Vector3f(dx * size, size, dz * size),
                tm.getLeftRotation(), new Vector3f(small), tm.getRightRotation()));
            tds.setTextOpacity((byte) -127);
            Main.sync(() -> tds.remove(), 24);
        }, 2);
    }


    public static void effect(final Entity ent, final Sound snd, final float pt, final Particle pr) {
        final double hd2 = ent.getHeight() * 0.55d;
        final double wd2 = ent.getWidth() * 0.6d;
        final Location loc = center(ent);
        new ParticleBuilder(pr).location(loc).count((int) (hd2 * wd2 * 28d))
            .offset(wd2, hd2, wd2).extra(0d).allPlayers().spawn();
        loc.getWorld().playSound(loc, snd, 1f, pt);
    }

    public static void effect(final Entity ent, final Sound snd, final float pt, final Particle pr, final Object data) {
        final double hd2 = ent.getHeight() * 0.55d;
        final double wd2 = ent.getWidth() * 0.6d;
        final Location loc = center(ent);
        final ParticleBuilder pb = new ParticleBuilder(pr).location(loc).count((int) (hd2 * wd2 * 28d));
        if (data != null && pr.getDataType().isAssignableFrom(data.getClass())) pb.data(data);
        pb.offset(wd2, hd2, wd2).extra(0d).allPlayers().spawn();
        loc.getWorld().playSound(loc, snd, 1f, pt);
    }

    private static final double TR_DST = 4d, TR_DEL = 1.4d;
    private static final int TR_TIME = (int) (TR_DST * TR_DEL);
    public static void moveffect(final Entity ent, final Sound snd, final float pt, final Color color) {
        final double hd2 = ent.getHeight() * 0.75d;
        final double wd2 = ent.getWidth() * 0.75d;
        final Location loc = center(ent);
        final Vector vel = ent.getVelocity();
        final double dY = vel.getY() / (TR_DST * TR_DEL);
        final Vector dir = new Vector(vel.getX(), vel.getY() - Math.abs(dY), vel.getZ()).multiply(TR_DST);
        PartUtil.trail(loc, dir.add(vel.normalize()), (int) (hd2 * wd2 * 40d), Position.fine(wd2, hd2, wd2), color, TR_TIME);
        loc.getWorld().playSound(loc, snd, 1f, pt);
    }

    public static Location center(final Entity ent) {
        return ent instanceof final LivingEntity le ?
            le.getEyeLocation().add(0d, ent.getHeight() * -0.4d, 0d)
            : ent.getLocation().add(0d, ent.getHeight() * 0.5d, 0d);
    }
}
