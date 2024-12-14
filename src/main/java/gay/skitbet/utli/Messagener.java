package gay.skitbet.utli;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class Messagener {
    public static final TextColor BLUE_COLOR = TextColor.color(61, 68, 212);
    public static final TextColor ORANGE_COLOR = TextColor.color(232, 175, 53);

    public static void info(Audience audience, String message) {
        info(audience, Component.text(message));
    }

    public static void info(Audience audience, Component message) {
        audience.sendMessage(Component.text("! ", BLUE_COLOR)
                .append(message.color(NamedTextColor.GRAY)));
    }

}
