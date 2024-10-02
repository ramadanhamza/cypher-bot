package org.example.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

public class Sum extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("sum")) return;
        OptionMapping number1 = event.getOption("number1");
        int num1 = number1.getAsInt();
        int num2 = event.getOption("number2").getAsInt();
        event.reply(String.valueOf(num1 + num2)).queue();
    }
}