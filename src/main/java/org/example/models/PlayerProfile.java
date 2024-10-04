package org.example.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {
    private String region;
    private String name;
    private String tag;
    private int accountLevel;
    private String highestRank;
    private String highestRankAct;
    private String currentRank;
    private String cardIcon;
    private String rankIcon;

}
