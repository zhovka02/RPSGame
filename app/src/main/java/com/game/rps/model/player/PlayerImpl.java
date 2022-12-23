package com.game.rps.model.player;

import android.os.Parcel;
import android.os.Parcelable;

public class PlayerImpl implements Parcelable, Player {

    public static final PlayerImpl.Creator<PlayerImpl> CREATOR = new PlayerImpl.Creator<PlayerImpl>() {
        public PlayerImpl createFromParcel(Parcel in) {
            return readUser(in);
        }

        public PlayerImpl[] newArray(int size) {
            return new PlayerImpl[size];
        }
    };
    private String name;
    private int wins;
    private int losses;
    private int rounds;

    public PlayerImpl(String name, int wins, int losses, int rounds) {
        this.name = name;
        this.wins = wins;
        this.losses = losses;
        this.rounds = rounds;
    }

    public static PlayerImpl readUser(Parcel aIn) {
        return new PlayerImpl(aIn.readString(), aIn.readInt(), aIn.readInt(), aIn.readInt());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getWins() {
        return this.wins;
    }

    @Override
    public void setWins(int wins) {
        this.wins = wins;
    }

    @Override
    public int getLosses() {
        return this.losses;
    }

    @Override
    public void setLosses(int losses) {
        this.losses = losses;
    }

    @Override
    public int getRounds() {
        return this.rounds;
    }

    @Override
    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.wins);
        dest.writeInt(this.losses);
        dest.writeInt(this.rounds);
    }

}