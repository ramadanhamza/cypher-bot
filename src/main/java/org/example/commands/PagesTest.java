package org.example.commands;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.InteractPage;
import com.github.ygimenez.model.Page;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PagesTest extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (!event.getName().equals("pages")) return;

        List<Page> pages = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            pages.add(InteractPage.of("This is entry Nº " + (i + 1)));
        }

        event.getChannel().sendMessage((MessageCreateData) pages.get(0).getContent()).queue(success -> {
            Pages.paginate(success, pages, true);
        });
    }
}
