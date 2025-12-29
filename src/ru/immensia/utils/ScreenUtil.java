package ru.immensia.utils;

import java.time.Duration;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import ru.immensia.utils.colors.TCUtil;

public class ScreenUtil {

    public static void sendTitleDirect(final Player p, final String title, final String subtitle) {
        sendTitleDirect(p, title, subtitle, 20, 40, 20);
    }

    public static void sendTitleDirect(final Player p, final String title, final String subtitle, final int fadein, final int stay, final int fadeout) {
        final Title.Times times = Title.Times.times(Duration.ofMillis(fadein * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeout * 50L));
        p.showTitle(Title.title(TCUtil.form(title), TCUtil.form(subtitle), times));
    }

    public static void sendActionBarDirect(final Player p, final String text) {
        if (p != null) {
            p.sendActionBar(TCUtil.form(text));
        }
    }

    public static void sendTabList(final Player p, final String header, final String footer) {
        p.sendPlayerListHeaderAndFooter(TCUtil.form(header), TCUtil.form(footer));
    }
}
