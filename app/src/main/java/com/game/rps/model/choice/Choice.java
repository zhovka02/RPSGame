package com.game.rps.model.choice;


public enum Choice {
    ROCK("rock"),
    SCISSORS("scissors"),
    PAPER("paper");

    private final String value;

    Choice(String s) {
        this.value = s;
    }

    public String getName() {
        return this.value;
    }



}
