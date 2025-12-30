package ru.immensia.utils.strings;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.util.Index;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.intellij.lang.annotations.Subst;


public class TCUtil {

    public static final TextComponent EMPTY;

    private static final Index<DyeColor, TextColor> dyeIx;
    private static final Index<Character, TextColor> chrIx;
    private static final Index<Color, TextColor> clrIx;
    private static final Set<Entry<Character, TextColor>> chrSet;
    private static final MiniMessage msg;
    private static final int tcSize;

    public static final char STYLE = '§';
    //    public static final char form = '᨟';
    //public static final char HEX = '#';
    //public static final char GRAD = '|';

    /**
     * 60% - Neutral color
     */
    public static String N = "§7";
    /**
     * 30% - Primary color
     */
    public static String P = "§7";
    /**
     * 10% - Action color
     */
    public static String A = "§7";

    static {
        EMPTY = Component.empty();
        final Map<TextColor, DyeColor> dyeLs = new HashMap<>();
        final Map<TextColor, Character> chrLs = new HashMap<>();
        final Map<TextColor, Color> clrLs = new HashMap<>();
        dyeLs.put(NamedTextColor.BLACK, DyeColor.BLACK);
        chrLs.put(NamedTextColor.BLACK, '0');//void
        clrLs.put(NamedTextColor.BLACK, Color.BLACK);
        dyeLs.put(NamedTextColor.DARK_BLUE, DyeColor.BLUE);
        chrLs.put(NamedTextColor.DARK_BLUE, '1');//adventure
        clrLs.put(NamedTextColor.DARK_BLUE, Color.NAVY);
        dyeLs.put(NamedTextColor.DARK_GREEN, DyeColor.GREEN);
        chrLs.put(NamedTextColor.DARK_GREEN, '2');//nature
        clrLs.put(NamedTextColor.DARK_GREEN, Color.GREEN);
        dyeLs.put(NamedTextColor.DARK_AQUA, DyeColor.CYAN);
        chrLs.put(NamedTextColor.DARK_AQUA, '3');//wisdom
        clrLs.put(NamedTextColor.DARK_AQUA, Color.TEAL);
        dyeLs.put(NamedTextColor.DARK_RED, DyeColor.BROWN);
        chrLs.put(NamedTextColor.DARK_RED, '4');//war
        clrLs.put(NamedTextColor.DARK_RED, Color.MAROON);
        dyeLs.put(NamedTextColor.DARK_PURPLE, DyeColor.MAGENTA);
        chrLs.put(NamedTextColor.DARK_PURPLE, '5');//royalty
        clrLs.put(NamedTextColor.DARK_PURPLE, Color.PURPLE);
        dyeLs.put(NamedTextColor.GOLD, DyeColor.ORANGE);
        chrLs.put(NamedTextColor.GOLD, '6');//wealth
        clrLs.put(NamedTextColor.GOLD, Color.ORANGE);
        dyeLs.put(NamedTextColor.GRAY, DyeColor.LIGHT_GRAY);
        chrLs.put(NamedTextColor.GRAY, '7');//plain
        clrLs.put(NamedTextColor.GRAY, Color.SILVER);
        dyeLs.put(NamedTextColor.DARK_GRAY, DyeColor.GRAY);
        chrLs.put(NamedTextColor.DARK_GRAY, '8');//shadow
        clrLs.put(NamedTextColor.DARK_GRAY, Color.GRAY);
        dyeLs.put(NamedTextColor.BLUE, DyeColor.PURPLE);
        chrLs.put(NamedTextColor.BLUE, '9');//trust
        clrLs.put(NamedTextColor.BLUE, Color.BLUE);
        dyeLs.put(NamedTextColor.GREEN, DyeColor.LIME);
        chrLs.put(NamedTextColor.GREEN, 'a');//balance
        clrLs.put(NamedTextColor.GREEN, Color.LIME);
        dyeLs.put(NamedTextColor.AQUA, DyeColor.LIGHT_BLUE);
        chrLs.put(NamedTextColor.AQUA, 'b');//spirit
        clrLs.put(NamedTextColor.AQUA, Color.AQUA);
        dyeLs.put(NamedTextColor.RED, DyeColor.RED);
        chrLs.put(NamedTextColor.RED, 'c');//health
        clrLs.put(NamedTextColor.RED, Color.RED);
        dyeLs.put(NamedTextColor.LIGHT_PURPLE, DyeColor.PINK);
        chrLs.put(NamedTextColor.LIGHT_PURPLE, 'd');//magic
        clrLs.put(NamedTextColor.LIGHT_PURPLE, Color.FUCHSIA);
        dyeLs.put(NamedTextColor.YELLOW, DyeColor.YELLOW);
        chrLs.put(NamedTextColor.YELLOW, 'e');//hope
        clrLs.put(NamedTextColor.YELLOW, Color.YELLOW);
        dyeLs.put(NamedTextColor.WHITE, DyeColor.WHITE);
        chrLs.put(NamedTextColor.WHITE, 'f');//confidence
        clrLs.put(NamedTextColor.WHITE, Color.WHITE);

        chrLs.put(CustomTextColor.OLIVE, 'о');//peace
        clrLs.put(CustomTextColor.OLIVE, Color.OLIVE);
        chrLs.put(CustomTextColor.AMBER, 'я');//strength
        clrLs.put(CustomTextColor.AMBER, Color.fromRGB(CustomTextColor.AMBER.value()));
        chrLs.put(CustomTextColor.APPLE, 'с');//growth
        clrLs.put(CustomTextColor.APPLE, Color.fromRGB(CustomTextColor.APPLE.value()));
        chrLs.put(CustomTextColor.BEIGE, 'б');//comfort
        clrLs.put(CustomTextColor.BEIGE, Color.fromRGB(CustomTextColor.BEIGE.value()));
        chrLs.put(CustomTextColor.CARDINAL, 'к');//passion
        clrLs.put(CustomTextColor.CARDINAL, Color.fromRGB(CustomTextColor.CARDINAL.value()));
        chrLs.put(CustomTextColor.INDIGO, 'и');//energy
        clrLs.put(CustomTextColor.INDIGO, Color.fromRGB(CustomTextColor.INDIGO.value()));
        chrLs.put(CustomTextColor.PINK, 'р');//love
        clrLs.put(CustomTextColor.PINK, Color.fromRGB(CustomTextColor.PINK.value()));
        chrLs.put(CustomTextColor.SKY, 'н');//calm
        clrLs.put(CustomTextColor.SKY, Color.fromRGB(CustomTextColor.SKY.value()));
        chrLs.put(CustomTextColor.STALE, 'ч');//future
        clrLs.put(CustomTextColor.STALE, Color.fromRGB(CustomTextColor.STALE.value()));
        chrLs.put(CustomTextColor.MITHRIL, 'м');//durability
        clrLs.put(CustomTextColor.MITHRIL, Color.fromRGB(CustomTextColor.MITHRIL.value()));
        chrLs.put(CustomTextColor.MINT, 'т');//freshness
        clrLs.put(CustomTextColor.MINT, Color.fromRGB(CustomTextColor.MINT.value()));
        chrLs.put(CustomTextColor.LILIAN, 'л');//essential
        clrLs.put(CustomTextColor.LILIAN, Color.fromRGB(CustomTextColor.LILIAN.value()));

        dyeIx = Index.create(tc -> dyeLs.get(tc), dyeLs.keySet().stream().toList());
        chrIx = Index.create(tc -> chrLs.get(tc), chrLs.keySet().stream().toList());
        chrSet = chrIx.keyToValue().entrySet();
        clrIx = Index.create(tc -> clrLs.get(tc), clrLs.keySet().stream().toList());
        tcSize = chrLs.size();

        TagResolver.Builder trb = TagResolver.builder();
        trb = trb.resolver(StandardTags.defaults());
        for (final Entry<String, CustomTextColor> en : CustomTextColor.VALUES.entrySet()) {
            @Subst("")
            final String key = en.getKey();
            trb = trb.resolver(TagResolver.resolver(key,
                Tag.styling(TextColor.color(en.getValue().value()))));
        }
        msg = MiniMessage.builder().tags(trb.build()).build();
        /*msg = MiniMessage.builder().tags(
            trb
                .resolver(StandardTags.defaults())
                .resolver(TagResolver.resolver("amber", Tag.styling(TextColor.color(0xCC8822))))//Янтарный
                .resolver(TagResolver.resolver("apple", Tag.styling(TextColor.color(0x88BB44))))//Салатовый
                .resolver(TagResolver.resolver("beige", Tag.styling(TextColor.color(0xDDCCAA))))//Бежевый
                .resolver(TagResolver.resolver("maroon", Tag.styling(TextColor.color(0xBB2244))))//Кардинный
                .resolver(TagResolver.resolver("indigo", Tag.styling(TextColor.color(0xAAAADD))))//Сиреневый
                .resolver(TagResolver.resolver("olive", Tag.styling(TextColor.color(0xBBDDAA))))//Оливковый
                .resolver(TagResolver.resolver("pink", Tag.styling(TextColor.color(0xDDAABB))))//Малиновый
                .resolver(TagResolver.resolver("sky", Tag.styling(TextColor.color(0xAADDDD))))//Небесный
                .resolver(TagResolver.resolver("stale", Tag.styling(TextColor.color(0x446666))))//Черствый
                .resolver(TagResolver.resolver("mithril", Tag.styling(TextColor.color(0xB0C0C0))))//Мифриловый
                .build()).build();*/
    }

    public static String strip(final String str) {
        return MiniMessage.miniMessage().stripTags(deLegacify(str));
    }

    public static String strip(final @Nullable Component cmp) {
        if (cmp == null) return "";
        final StringBuilder sb = new StringBuilder();
        if (cmp instanceof TextComponent) {
            sb.append(strip(((TextComponent) cmp).content()));
        }
        for (final Component ch : cmp.children()) {
            sb.append(strip(ch));
        }
        return sb.toString();
    }

    private static String deLegacify(final String str) {
        String fin = str;
        for (final Entry<Character, TextColor> en : chrSet) {
            final TextColor tc = en.getValue();
            final String rpl;
            switch (tc) {
                case final NamedTextColor nc:
                    rpl = nc.toString();
                    break;
                case final CustomTextColor cc:
                    rpl = cc.toString();
                    break;
                default:
                    continue;
            }

            fin = fin.replace(STYLE + en.getKey().toString(), "<" + rpl + ">");
        }
        fin = fin.replace(STYLE + "k", "<obf>");
        fin = fin.replace(STYLE + "K", "<obf>");
        fin = fin.replace(STYLE + "l", "<b>");
        fin = fin.replace(STYLE + "L", "<b>");
        fin = fin.replace(STYLE + "m", "<st>");
        fin = fin.replace(STYLE + "M", "<st>");
        fin = fin.replace(STYLE + "n", "<u>");
        fin = fin.replace(STYLE + "N", "<u>");
        fin = fin.replace(STYLE + "o", "<i>");
        fin = fin.replace(STYLE + "O", "<i>");
        fin = fin.replace(STYLE + "r", "<r>");
        fin = fin.replace(STYLE + "R", "<r>");
        for (final Entry<String, CustomTextColor> en : CustomTextColor.VALUES.entrySet()) {
            fin = fin.replace(":" + en.getKey(), ":#" + Integer.toHexString(en.getValue().value()));
        }
        return fin;
    }

    public static Component form(final String str) {
        if (str == null || str.isEmpty()) return EMPTY;
        return msg.deserialize(deLegacify(str)).decorationIfAbsent(
            TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static String deform(final Component cmp) {
        if (cmp == null) return "";
        return msg.serialize(cmp);
    }


    public static String toLegacy(@Nullable String val) {
        if (val == null || val.isEmpty()) return val;
        for (final CustomTextColor ctc : CustomTextColor.values()) {
            val = val.replace(String.valueOf(STYLE) + chrIx.keyOr(ctc, 'f'),
                "<" + ctc.toString() + ">").replace(ctc.toString(), ctc.like());
        }
        for (final NamedTextColor ntc : NamedTextColor.NAMES.values()) {
            val = val.replace("<" + ntc.toString() + ">",
                String.valueOf(STYLE) + chrIx.keyOr(ntc, 'f'));
        }
        val = val.replace("<obf>", STYLE + "k");
        val = val.replace("<b>", STYLE + "l");
        val = val.replace("<st>", STYLE + "m");
        val = val.replace("<u>", STYLE + "n");
        val = val.replace("<i>", STYLE + "o");
        val = val.replace("<r>", STYLE + "r");
        return val;
    }

    public static boolean compare(final Component of, final Component to) {
        return strip(of).equals(strip(to));
    }

    public static String bind(final Input key) {
        return "[<key:key." + key.key + ">]";
    }

    public enum Input {
        FORWARD, BACK, RIGHT, LEFT, DROP, ATTACK, USE,
        JUMP, SPRINT, SNEAK, INVENTORY, ADVANCEMENTS;
        private final String key = name().toLowerCase(Locale.US);
    }
}