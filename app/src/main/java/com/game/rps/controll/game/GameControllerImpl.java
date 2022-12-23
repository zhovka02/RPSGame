package com.game.rps.controll.game;

import android.hardware.SensorEvent;
import android.util.Log;

import com.game.rps.controll.keystroke.KeyStrokeListener;
import com.game.rps.controll.network.BluetoothControllerImpl;
import com.game.rps.controll.shaking.ShakingDetectedListener;
import com.game.rps.model.choice.Choice;
import com.game.rps.model.database.RPSDatabaseImpl;
import com.game.rps.model.player.Player;
import com.game.rps.view.keystroke.KeyStrokeDetector;
import com.game.rps.view.shaking.ShakingDetector;

public class GameControllerImpl implements GameController, KeyStrokeListener, ShakingDetectedListener {
    private static final String TAG = "GameControllerImpl";
    private static GameControllerImpl instance;
    private final GameLogic logic;
    private Player opponent;
    private Choice choice;
    private boolean shaken;
    private GameStateListener listener;
    private State state;

    public GameControllerImpl() {
        KeyStrokeDetector.getInstance().setOnKeyStrokeListener(this);
        ShakingDetector.getInstance().setOnShakeListener(this);
        this.state = State.NOT_CONNECTED;
        this.logic = new GameLogicImpl();
    }

    public static GameController getController() {
        if (instance == null) instance = new GameControllerImpl();
        return instance;
    }

    @Override
    public void setListener(GameStateListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean isReadyToPerformRound() {
        return instance.choice != null && instance.shaken && instance.state == State.CONNECTED;
    }

    @Override
    public synchronized void performRound(Choice opponentChoice) {
        instance.state = instance.logic.judge(instance.choice, opponentChoice);
        Log.d(TAG, "Round Performed");
        if (RPSDatabaseImpl.getInstance() != null) {
            Player player = RPSDatabaseImpl.getInstance().getPlayer();
            switch (instance.state) {
                case WIN:
                    player.setWins(player.getWins() + 1);
                    break;
                case LOSE:
                    player.setLosses(player.getLosses() + 1);
                    break;
            }
            player.setRounds(player.getRounds() + 1);
            RPSDatabaseImpl.getInstance().updatePlayer(player);
        }

        instance.listener.onGameStateChanged(this.state);
    }

    @Override
    public void restart() {
        instance.choice = null;
        instance.shaken = false;
        Log.d(TAG, "Restarted");
        if (BluetoothControllerImpl.getController() != null)
            if (BluetoothControllerImpl.getController().isConnected())
                instance.state = State.CONNECTED;
            else instance.state = State.NOT_CONNECTED;
    }

    @Override
    public synchronized void switchState(State state) {
        instance.state = state;
        if (this.listener != null) instance.listener.onGameStateChanged(state);
    }

    @Override
    public void onKeyDown() {
        instance.choice = Choice.PAPER;
        this.onKey();
    }

    @Override
    public void onKeyUp() {
        instance.choice = Choice.ROCK;
        System.out.println("kek");
        this.onKey();
    }

    @Override
    public void onKeyBoth() {
        instance.choice = Choice.SCISSORS;
        this.onKey();
    }

    private synchronized void onKey() {
        if (this.isReadyToPerformRound()) {
            instance.state = State.CHOSEN;
            Log.d(TAG, "Choice chosen");
            if (BluetoothControllerImpl.getController() != null)
                BluetoothControllerImpl.getController().sendChoiceToOpponent(instance.choice);
            if (instance.listener != null)
                instance.listener.onGameStateChanged(State.CHOSEN);
        }
    }


    @Override
    public void onShakingDetected(SensorEvent event) {
        instance.shaken = true;
    }

    @Override
    public Player getOpponent() {
        return instance.opponent;
    }

    @Override
    public void setOpponent(Player player) {
        instance.opponent = player;
    }

    @Override
    public Choice getChoice() {
        return instance.choice;
    }

    @Override
    public synchronized State getState() {
        return instance.state;
    }
}
