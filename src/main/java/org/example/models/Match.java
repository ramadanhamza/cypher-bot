package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    private String mode;
    private String characterIcon;
    private String mapIcon;
    private String map;
    private String startTime;
    private int kills;
    private int deaths;
    private int assists;
    private String playerName;
    private String playerTag;
    private String gameWon;
    private Color embedColor;
}