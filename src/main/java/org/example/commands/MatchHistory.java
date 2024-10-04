package org.example.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.callbacks.FetchMatchHistoryCallback;
import org.example.models.Match;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MatchHistory extends ListenerAdapter {

    private final OkHttpClient client = new OkHttpClient();

    public void fetchMatchHistory(String region, String name, String tag, @NotNull SlashCommandInteractionEvent event, FetchMatchHistoryCallback callback) {

        String url = "https://api.henrikdev.xyz/valorant/v1/stored-matches/" + region + "/" + name + "/" + tag;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", System.getenv("VALORANT_API_KEY"))
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onFailure("Failed to retrieve data from the Valorant API");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Error: API request failed");
                    return;
                }

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                JSONArray data = json.getJSONArray("data");

                List<Match> matches = new ArrayList<>();

                for (int i = 0; i < data.length(); i++) {
                    JSONObject matchData = data.getJSONObject(i);
                    JSONObject meta = matchData.getJSONObject("meta");
                    String mode = meta.getString("mode");

                    JSONObject stats = matchData.getJSONObject("stats");
                    JSONObject character = stats.getJSONObject("character");
                    String characterName = character.getString("name");

                    int kills = stats.getInt("kills");
                    int deaths = stats.getInt("deaths");
                    int assists = stats.getInt("assists");
                    int score = stats.getInt("score");

                    Match match = Match.builder()
                            .mode(mode)
                            .characterName(characterName)
                            .kills(kills)
                            .deaths(deaths)
                            .assists(assists)
                            .score(score)
                            .build();

                    matches.add(match);
                }

            }
        });
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (!event.getName().equals("profile")) return;

        String region = event.getOption("region").getAsString();
        String name = event.getOption("name").getAsString();
        String tag = event.getOption("tag").getAsString();

        fetchMatchHistory(region, name, tag, event, new FetchMatchHistoryCallback() {
            @Override
            public void onSuccess(List<Match> matches) {
                Match firstMatch = matches.getFirst();
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.LIGHT_GRAY)
                        .addField("\u200B", "", false)
                        .addField("Kills", Integer.toString(firstMatch.getKills()), false)
                        .addField("Deaths", Integer.toString(firstMatch.getDeaths()), false)
                        .addField("Assists", Integer.toString(firstMatch.getAssists()), false)
                        .addField("Score", Integer.toString(firstMatch.getScore()), false);

                MessageEmbed embed = embedBuilder.build();

                event.replyEmbeds(embed).queue();
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }

}
