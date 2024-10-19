package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.example.commands.MatchHistory;
import org.example.commands.Profile;

public class Main {

    public static void main(String[] args) {

        JDA jda = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN")).build();
        jda.addEventListener(new Listeners());
        jda.addEventListener(new Profile());
        jda.addEventListener(new MatchHistory());
    }
}