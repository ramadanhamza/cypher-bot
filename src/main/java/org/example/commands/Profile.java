package org.example.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.Token;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

public class Profile extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (!event.getName().equals("profile")) return;

        String region = event.getOption("region").getAsString();
        String name = event.getOption("name").getAsString();
        String tag = event.getOption("tag").getAsString();

        String url = "https://api.henrikdev.xyz/valorant/v2/mmr/" + region + "/" + name + "/" + tag;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", Token.VALORANT_API_KEY)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                event.reply("Failed to retrieve data from the Valorant API").queue();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    event.reply("Error: API request failed").queue();
                    return;
                }

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                JSONObject data = json.getJSONObject("data");

                String playerName = data.getString("name");
                String playerTag = data.getString("tag");
                String rank = data.getJSONObject("current_data")
                        .getString("currenttierpatched");

                event.reply("Player: " + playerName + "#" + playerTag + "\nRegion: " + region + "\nRank: " + rank).queue();
            }
        });

    }
}
