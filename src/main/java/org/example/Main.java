package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.example.commands.Profile;
import org.example.commands.Sum;

public class Main {

    public static void main(String[] args) {
        JDA jda = JDABuilder.createDefault(Token.DISCORD_TOKEN).build();
        jda.addEventListener(new Listeners());
        jda.addEventListener(new Sum());
        jda.addEventListener(new Profile());
    }
}