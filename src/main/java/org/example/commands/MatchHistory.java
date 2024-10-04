package org.example.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;
import org.example.callbacks.FetchMatchHistoryCallback;
import org.example.models.Match;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;


import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MatchHistory extends ListenerAdapter {

    private final OkHttpClient client = new OkHttpClient();

    public void fetchMatchHistory(String region, String name, String tag, String mode, @NotNull SlashCommandInteractionEvent event, FetchMatchHistoryCallback callback) {

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

                JSONArray matchHistoryData = json.getJSONArray("data");

                JSONArray matchHistoryDataMode = new JSONArray();

                for (int i = 0; i < matchHistoryData.length(); i++) {
                    JSONObject match = matchHistoryData.getJSONObject(i);
                    JSONObject meta = match.getJSONObject("meta");
                    String matchMode = meta.getString("mode");
                    if (matchMode.equalsIgnoreCase(mode) || mode.equalsIgnoreCase("all")) {
                        matchHistoryDataMode.put(match);
                    }
                }

                final int totalMatches = Math.min(3, matchHistoryDataMode.length());

                String agentsUrl = "https://valorant-api.com/v1/agents";

                Request request = new Request.Builder()
                        .url(agentsUrl)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        event.getHook().editOriginal("Failed to retrieve data from the Valorant API").queue();
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            event.getHook().editOriginal("Error: API request failed").queue();
                            return;
                        }

                        List<Match> matches = new ArrayList<>();

                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);

                        JSONArray agentData = json.getJSONArray("data");

                        String mapsUrl = "https://valorant-api.com/v1/maps";

                        Request request = new Request.Builder()
                                .url(mapsUrl)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                event.getHook().editOriginal("Failed to retrieve data from the Valorant API").queue();
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    event.getHook().editOriginal("Error: API request failed").queue();
                                    return;
                                }

                                String responseBody = response.body().string();
                                JSONObject json = new JSONObject(responseBody);

                                JSONArray mapData = json.getJSONArray("data");

                                for (int i = 0; i < totalMatches; i++) {
                                    JSONObject matchData = matchHistoryDataMode.getJSONObject(i);
                                    JSONObject meta = matchData.getJSONObject("meta");
                                    JSONObject stats = matchData.getJSONObject("stats");
                                    JSONObject character = stats.getJSONObject("character");
                                    String characterId = character.getString("id");
                                    String mode = meta.getString("mode");
                                    String startTime = meta.getString("started_at");
                                    JSONObject map = meta.getJSONObject("map");
                                    String mapId = map.getString("id");
                                    String mapName = map.getString("name");

                                    String team = stats.getString("team");
                                    int kills = stats.getInt("kills");
                                    int deaths = stats.getInt("deaths");
                                    int assists = stats.getInt("assists");

                                    JSONObject teams = matchData.getJSONObject("teams");
                                    int red = teams.getInt("red");
                                    int blue = teams.getInt("blue");

                                    String winningTeam = (red > blue) ? "red" : "blue";

                                    boolean gameWon = team.equalsIgnoreCase(winningTeam);

                                    Match.MatchBuilder matchBuilder = Match.builder();

                                    matchBuilder.mode(mode)
                                            .gameWon(gameWon)
                                            .map(mapName)
                                            .startTime(startTime.replace("T", " | ").replaceAll("\\..*", ""))
                                            .kills(kills)
                                            .deaths(deaths)
                                            .assists(assists);

                                    for (int j = 0; j < agentData.length(); j++) {
                                        JSONObject agent = agentData.getJSONObject(j);
                                        if (characterId.equals(agent.getString("uuid"))) {
                                            String characterIcon = agent.getString("displayIconSmall");
                                            matchBuilder.characterIcon(characterIcon);
                                        }
                                    }

                                    for( int j = 0; j < mapData.length(); j++) {
                                        JSONObject mapNode = mapData.getJSONObject(j);
                                        if (mapId.equals(mapNode.getString("uuid"))) {
                                            String mapIcon = mapNode.getString("listViewIcon");
                                            matchBuilder.mapIcon(mapIcon);
                                        }
                                    }

                                    Match match = matchBuilder.build();

                                    matches.add(match);
                                }
                                callback.onSuccess(matches);
                            }

                        });
                    }
                });
            }
        });
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("matchhistory")) return;

        String region = event.getOption("region").getAsString();
        String name = event.getOption("name").getAsString();
        String tag = event.getOption("tag").getAsString();
        String mode = event.getOption("mode").getAsString();

        event.deferReply().queue();

        fetchMatchHistory(region, name, tag, mode, event, new FetchMatchHistoryCallback() {
            @Override
            public void onSuccess(List<Match> matches) {
                List<MessageEmbed> embeds = new ArrayList<>();

                for (Match match : matches) {
                    MessageEmbed embed = new EmbedBuilder()
                            .setColor(match.isGameWon() ? Color.GREEN : Color.RED)
                            .setThumbnail(match.getCharacterIcon())
                            .addField("Player", name + "#" + tag, true)
                            .addField("Mode", match.getMode(), true)
                            .addField(match.isGameWon() ? "WIN" : "LOSS", "", true)
                            .addField("Kills", Integer.toString(match.getKills()), true)
                            .addField("Deaths", Integer.toString(match.getDeaths()), true)
                            .addField("Assists", Integer.toString(match.getAssists()), true)
                            .setImage(match.getMapIcon())
                            .setFooter(match.getMap() + "   -   " + match.getStartTime() + " UTC")
                            .build();

                    embeds.add(embed);
                }

                event.getHook().editOriginalEmbeds(embeds).queue();
            }

            @Override
            public void onFailure(String errorMessage) {
                event.getHook().editOriginal(errorMessage).queue();
            }
        });
    }
}
