package org.example.callbacks;

public interface FetchHighestRankCallback {
    void onSuccess(String highestRank, String highestRankAct);
    void onFailure(String errorMessage);
}
