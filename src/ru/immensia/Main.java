package ru.immensia;

import java.security.SecureRandom;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.immensia.boot.Registries;
import ru.immensia.entities.Mobs;
import ru.immensia.objects.IConfigManager;
import ru.immensia.utils.colors.TCUtil;

public class Main extends JavaPlugin {
    
	public static Main plug;
//	public static Relics relics;
	public static Mobs mobs;
	public static World world;
	public static String PREFIX;
//	public static EventManager events;
	public static Registries registries;
	public static IConfigManager manager;
	public static LifecycleEventManager<Plugin> mgr;

	public static final int LAND_DIST = 2000;
	public static final ItemStack air = new ItemStack(Material.AIR);
	public static final SecureRandom srnd = new SecureRandom();

	private static ComponentLogger logger;

	@Override
	public void onLoad() {
		plug = this;
		mgr = plug.getLifecycleManager();
		logger = getComponentLogger();
		manager = new IConfigManager(plug);
	}

	@Override
	public void onEnable() {
		registries = new Registries();
		//OSTROV stuff
		TCUtil.N = "<mint>";
		TCUtil.P = "<yellow>";
		TCUtil.A = "<dark_green>";

		PREFIX = TCUtil.N + "["+ TCUtil.P + "DA" + TCUtil.N + "] ";
		mobs = new Mobs();
//		relics = new Relics();
		world = Bukkit.getWorlds().getFirst();
		
		getServer().getConsoleSender().sendMessage("§2Daaria is on!");
		
		getServer().getPluginManager().registerEvents(new MainLis(), this);

//		events = new EventManager();
	}

	@Override
	public void onDisable() {
//		if (EventManager.prog != null) EventManager.prog.crash();
		getServer().getConsoleSender().sendMessage("§4Daaria is off!");
	}

	public static void log(String s) {
		logger.info(TCUtil.form(s));
	}

	public static void log_warn(String s) {
		logger.warn(TCUtil.form(s));
	}

	public static void log_err(String s) {
		logger.error(TCUtil.form(s));//Bukkit.getLogger().log(Level.SEVERE, prefixERR+s);
	}

	public static void sync(final Runnable runnable) { //use sync ( ()->{} ,1 );
		if (runnable == null) return;
		//SHUT_DOWN для фикса IllegalPluginAccessException: Plugin attempted to register task while disabled
		if (Bukkit.isPrimaryThread()) runnable.run();
		else Bukkit.getScheduler().runTask(plug, runnable);
	}


	public static void sync(final Runnable runnable, final int delayTicks) { //sync ( ()->{} ,1 );
		if (delayTicks == 0) {
			sync(runnable);
		} else {
			if (runnable == null) return;
			new BukkitRunnable() {
				@Override
				public void run() {
					runnable.run();
				}
			}.runTaskLater(plug, delayTicks);
		}
	}

	public static void async(final Runnable runnable) { //sync ( ()->{} ,1 );
		if (runnable == null) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskAsynchronously(plug);
	}


	public static void async(final Runnable runnable, final int delayTicks) { //sync ( ()->{} ,1 );
		if (delayTicks == 0) {
			async(runnable);
		} else {
			if (runnable == null) return;
			new BukkitRunnable() {
				@Override
				public void run() {
					runnable.run();
				}
			}.runTaskLaterAsynchronously(plug, delayTicks);
		}
	}
}
