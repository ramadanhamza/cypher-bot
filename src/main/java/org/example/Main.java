package org.example;

import com.github.ygimenez.exception.InvalidHandlerException;
import com.github.ygimenez.model.PaginatorBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.example.commands.MatchHistory;
import com.github.ygimenez.method.Pages;
import org.example.commands.Profile;

public class Main {

    public static void main(String[] args) throws InvalidHandlerException {

        JDA jda = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN")).build();
        Pages.activate(PaginatorBuilder.createSimplePaginator(jda));
        jda.addEventListener(new Listeners());
        jda.addEventListener(new Profile());
        jda.addEventListener(new MatchHistory());
    }
}