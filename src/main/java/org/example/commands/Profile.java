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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Profile extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (!event.getName().equals("profile")) return;

        String region = event.getOption("region").getAsString();
        String name = event.getOption("name").getAsString();
        String tag = event.getOption("tag").getAsString();

        String url = "https://api.henrikdev.xyz/valorant/v1/mmr-history/" + region + "/" + name + "/" + tag;

        String seasonsUrl = "https://valorant-api.com/v1/seasons";

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

                String playerName = json.getString("name");
                String playerTag = json.getString("tag");
                JSONArray data = json.getJSONArray("data");
                JSONObject recentData = data.getJSONObject(0);
                String seasonId = recentData.getString("season_id");

                String seasonsUrl = "https://valorant-api.com/v1/seasons";

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(seasonsUrl)
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

                        JSONArray data = json.getJSONArray("data");
                        JSONObject currentAct = data.getJSONObject(data.length() - 1);
                        String currentActId = currentAct.getString("uuid");
                        JSONArray currentActSeasons = new JSONArray();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject season = data.getJSONObject(i);

                            if (!season.isNull("parentUuid") && season.getString("parentUuid").equals(currentActId)) {
                                currentActSeasons.put(season);
                            }
                        }

                        LocalDateTime currentTime = LocalDateTime.now(ZoneOffset.UTC);

                        for (int i = 0; i < currentActSeasons.length(); i++) {

                            JSONObject currentActSeason = currentActSeasons.getJSONObject(i);

                            String startTimeStr = currentActSeason.getString("startTime");
                            String endTimeStr = currentActSeason.getString("endTime");

                            LocalDateTime startTime = LocalDateTime.parse(startTimeStr.replace("Z", ""), DateTimeFormatter.ISO_DATE_TIME);
                            LocalDateTime endTime = LocalDateTime.parse(endTimeStr.replace("Z", ""), DateTimeFormatter.ISO_DATE_TIME);

                            if (currentTime.isEqual(startTime) || currentTime.isEqual(endTime) || (currentTime.isAfter(startTime) && currentTime.isBefore(endTime))) {
                                String currentSeasonId = currentActSeason.getString("uuid");
                                String rank = (!Objects.equals(seasonId, currentSeasonId)) ? "Unranked" : recentData.getString("currenttierpatched");
                                event.reply("Player: " + playerName + "#" + playerTag + "\nRegion: " + region + "\nRank: " + rank).queue();
                                return;
                            }
                        }
                    }
                });
            }
        });

    }
}
