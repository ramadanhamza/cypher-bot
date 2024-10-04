package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

public class Listeners extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {

        Guild guild = event.getJDA().getGuildById("1132368087665082418");

        guild.getJDA().upsertCommand("matchhistory", "Shows the last 3 games for a valorant account in the given mode.").addOptions(
                new OptionData(
                        OptionType.STRING,
                        "region",
                        "The account's region (AP | BR | EU | KR | LATAM | NA)",
                        true
                ),
                new OptionData(
                        OptionType.STRING,
                        "name",
                        "The account's name",
                        true
                ),
                new OptionData(
                        OptionType.STRING,
                        "tag",
                        "The account's tagline",
                        true
                ),
                new OptionData(
                        OptionType.STRING,
                        "mode",
                        "The game mode (If you want to show all games modes, write : all)",
                        true
                )
        ).queue();

        guild.getJDA().upsertCommand("profile", "Gives a profile overview for a valorant account.").addOptions(
                new OptionData(
                        OptionType.STRING,
                        "region",
                        "The account's region (AP | BR | EU | KR | LATAM | NA)",
                        true
                ),
                new OptionData(
                        OptionType.STRING,
                        "name",
                        "The account's name",
                        true
                ),
                new OptionData(
                        OptionType.STRING,
                        "tag",
                        "The account's tagline",
                        true
                )
        ).queue();
    }





    /*@Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        for (Guild guild: jda.getGuilds()) {
            System.out.println(guild.getName());
        }
    }*/
}
