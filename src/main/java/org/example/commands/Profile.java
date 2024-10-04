package org.example.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.models.PlayerProfile;
import org.example.callbacks.FetchCurrentRankCallback;
import org.example.callbacks.FetchHighestRankCallback;
import org.example.callbacks.FetchProfileInfoCallback;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Profile extends ListenerAdapter {

    private final OkHttpClient client = new OkHttpClient();

    public void fetchCurrentRank(String region, String name, String tag, @NotNull SlashCommandInteractionEvent event, FetchCurrentRankCallback callback) {

        String url = "https://api.henrikdev.xyz/valorant/v1/mmr-history/" + region + "/" + name + "/" + tag;

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
                JSONObject recentData = data.getJSONObject(0);
                String seasonId = recentData.getString("season_id");

                String seasonsUrl = "https://valorant-api.com/v1/seasons";

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
                                String rankIcon = (!Objects.equals(seasonId, currentSeasonId)) ? null : recentData.getJSONObject("images")
                                                                                                                .getString("small");
                                callback.onSuccess(rank, rankIcon);
                                return;
                            }
                        }
                    }
                });
            }
        });
    }

    public void fetchHighestRank(String region, String name, String tag, @NotNull SlashCommandInteractionEvent event, FetchHighestRankCallback callback) {
        String url = "https://api.henrikdev.xyz/valorant/v2/mmr/" + region + "/" + name + "/" + tag;

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
                    if (response.code() == 404) {
                        callback.onFailure("Player profile could not be found");
                        return;
                    }
                    callback.onFailure("Error: API request failed");
                    return;
                }

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                JSONObject data = json.getJSONObject("data");
                JSONObject highestRankObject = data.getJSONObject("highest_rank");
                String highestRankStr = highestRankObject.getString("patched_tier");

                callback.onSuccess(highestRankStr);
            }
        });
    }

    public void fetchProfileInfo(String name, String tag, @NotNull SlashCommandInteractionEvent event, FetchProfileInfoCallback callback ) {

        String url = "https://api.henrikdev.xyz/valorant/v1/account/" + name + "/" + tag;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", System.getenv("VALORANT_API_KEY"))
                .build();

        List<String> profileInfo = new ArrayList<String>();

        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onFailure("Failed to retrieve data from the Valorant API");
            }

            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    callback.onFailure("Error: API request failed");
                    return;
                }

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                JSONObject data = json.getJSONObject("data");

                String name = data.getString("name");
                String tag = data.getString("tag");
                String region = data.getString("region");
                int accountLevel = data.getInt("account_level");
                String cardIcon = data.getJSONObject("card").getString("small");

                callback.onSuccess(name, tag, region, accountLevel, cardIcon);
            }

        });
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (!event.getName().equals("profile")) return;

        String region = event.getOption("region").getAsString();
        String name = event.getOption("name").getAsString();
        String tag = event.getOption("tag").getAsString();

        PlayerProfile.PlayerProfileBuilder profileBuilder = PlayerProfile.builder();

        fetchHighestRank(region, name, tag, event, new FetchHighestRankCallback() {
            @Override
            public void onSuccess(String highestRank) {
                profileBuilder.highestRank(highestRank);

                fetchProfileInfo(name, tag, event, new FetchProfileInfoCallback() {
                    @Override
                    public void onSuccess(String name, String tag, String region, int accountLevel, String cardIcon) {
                        profileBuilder.name(name)
                                .tag(tag)
                                .region(region.toUpperCase())
                                .accountLevel(accountLevel)
                                .cardIcon(cardIcon);

                        fetchCurrentRank(region, name, tag, event, new FetchCurrentRankCallback() {
                            @Override
                            public void onSuccess(String currentRank, String rankIcon) {
                                profileBuilder.currentRank(currentRank)
                                        .rankIcon(rankIcon);
                                PlayerProfile profile = profileBuilder.build();

                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setColor(Color.LIGHT_GRAY)
                                        .setThumbnail(profile.getCardIcon())
                                        .setThumbnail(profile.getCardIcon())
                                        .setTitle(profile.getName() + "#" + profile.getTag() + "'s profile")
                                        .addField("\u200B", "", false)
                                        .addField("Region", profile.getRegion(), false)
                                        .addField("Level", Integer.toString(profile.getAccountLevel()), false)
                                        .addField("Highest Rank", profile.getHighestRank(), false)
                                        .addField("Current Rank", profile.getCurrentRank(), true)
                                        .setImage(profile.getRankIcon());

                                MessageEmbed embed = embedBuilder.build();

                                event.replyEmbeds(embed).queue();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                event.reply(errorMessage).queue();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        event.reply(errorMessage).queue();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                event.reply(errorMessage).queue();
            }
        });
    }
}
