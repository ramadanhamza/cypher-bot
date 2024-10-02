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
        guild.upsertCommand("sum", "Gives the sum of two numbers.").addOptions(
                new OptionData(
                        OptionType.INTEGER,
                        "number1",
                        "The first number",
                        true
                ),
                new OptionData(
                        OptionType.INTEGER,
                        "number2",
                        "The second number",
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
