package ru.immensia.items.crafts;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import com.mojang.brigadier.Command;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.immensia.Main;
import ru.immensia.boot.IStrap;
import ru.immensia.cmds.OCmdBuilder;
import ru.immensia.cmds.Resolver;

public class CraftCmd {

    public CraftCmd() {
        final String act = "action", name = "name";
        new OCmdBuilder("craft", "/craft edit|remove [имя]")
            .then(Resolver.string(act)).suggest(cntx -> {
                if (!cntx.getSource().getSender().isOp()) {
                    return Set.of();
                }
                return Set.of("edit", "remove");
            }, false).then(Resolver.string(name)).suggest(cntx -> {
                if (!cntx.getSource().getSender().isOp()) {
                    return Set.of();
                }
                return CraftManager.crafts.keySet().stream()
                    .map(NamespacedKey::getKey).collect(Collectors.toSet());
            }, false).run(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eNot a console command!");
                    return 0;
                }

                final String nm;
                return switch (Resolver.string(cntx, act)) {
                    case "edit" -> {
                        if (!cs.isOp()) {
                            pl.sendMessage("§cOnly for OPs!");
                            yield 0;
                        }

                        nm = Resolver.string(cntx, name);
                        final CraftManager.Craft cr = CraftManager.crafts.get(IStrap.key(nm));
                        CraftManager.openEditor(pl, nm, cr == null ? null : cr.rec());
                        yield Command.SINGLE_SUCCESS;
                    }
                    case "remove" -> {
                        if (!cs.isOp()) {
                            pl.sendMessage("§cOnly for OPs!");
                            yield 0;
                        }

                        nm = Resolver.string(cntx, name);
                        final YamlConfiguration craftConfig = YamlConfiguration.loadConfiguration(CraftManager.DEF_FILE.toFile());
                        if (!craftConfig.getKeys(false).contains(nm)) {
                            pl.sendMessage("§cThere's no such recipe!");
                            yield 0;
                        }
                        craftConfig.set(nm, null);
                        Bukkit.removeRecipe(IStrap.key(nm));
                        CraftManager.rmvRecipe(IStrap.key(nm));
                        pl.sendMessage("§7Recipe §e" + nm + " §7is removed!");
                        try {
                            craftConfig.save(CraftManager.DEF_FILE.toFile());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        yield Command.SINGLE_SUCCESS;
                    }
                    default -> {
                        pl.sendMessage("§cWrong command syntax!");
                        yield 0;
                    }
                };
            })
            .description("Recipe editor command")
            .register(Main.mgr);
    }
}
