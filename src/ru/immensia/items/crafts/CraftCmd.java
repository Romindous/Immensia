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
import org.bukkit.inventory.Recipe;
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
                return Crafts.crafts.keySet().stream()
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
                        final Recipe rec = Bukkit.getRecipe(IStrap.key(nm));
                        if (rec == null) {
                            pl.sendMessage("§cThere's no such recipe!");
                            yield 0;
                        }
                        Crafts.openEditor(pl, nm, rec);
                        yield Command.SINGLE_SUCCESS;
                    }
                    case "remove" -> {
                        if (!cs.isOp()) {
                            pl.sendMessage("§cOnly for OPs!");
                            yield 0;
                        }

                        nm = Resolver.string(cntx, name);
                        final YamlConfiguration craftConfig = YamlConfiguration.loadConfiguration(Crafts.DEF_FILE.toFile());
                        if (!craftConfig.getKeys(false).contains(nm)) {
                            pl.sendMessage("§cThere's no such recipe!");
                            yield 0;
                        }
                        craftConfig.set(nm, null);
                        Bukkit.removeRecipe(new NamespacedKey(IStrap.space, nm));
                        Crafts.rmvRecipe(new NamespacedKey(IStrap.space, nm));
                        pl.sendMessage("§7Recipe §e" + nm + " §7is removed!");
                        try {
                            craftConfig.save(Crafts.DEF_FILE.toFile());
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
