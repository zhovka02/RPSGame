package com.game.rps.view.ui.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.game.rps.R;
import com.game.rps.controll.game.GameControllerImpl;
import com.game.rps.controll.game.GameStateListener;
import com.game.rps.controll.game.State;
import com.game.rps.databinding.FragmentGameBinding;

public class GameFragment extends Fragment implements GameStateListener {

    private FragmentGameBinding binding;
    private View root;
    private TextView textView;
    private ImageView imageView;
    private Button btnRestart;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        this.binding = FragmentGameBinding.inflate(inflater, container, false);
        this.root = this.binding.getRoot();
        this.textView = this.root.findViewById(R.id.gameText);
        this.imageView = this.root.findViewById(R.id.gameStateImage);
        this.btnRestart = this.root.findViewById(R.id.restartButton);
        this.btnRestart.setOnClickListener((view) -> this.restart());
        GameControllerImpl.getController().setListener(this);
        this.onGameStateChanged(GameControllerImpl.getController().getState());
        return this.root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

    public void restart() {
        GameControllerImpl.getController().restart();
        this.onGameStateChanged(GameControllerImpl.getController().getState());
    }

    @Override
    public void onGameStateChanged(State state) {
        if (this.getActivity() == null || !this.isAdded()) return;
        this.requireActivity().runOnUiThread(() -> {
            switch (GameControllerImpl.getController().getState()) {
                case NOT_CONNECTED:
                    this.setViews(R.drawable.not_connnected_image, R.string.not_connected, View.GONE);
                    break;
                case CONNECTED:
                    while (GameControllerImpl.getController().getOpponent() == null) {
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    this.setViews(R.drawable.ready_image, String.format(this.root.getResources().getString(R.string.connected),
                            GameControllerImpl.getController().getOpponent().getName()));
                    break;
                case CHOSEN:
                    int drawable = R.drawable.rock;
                    switch (GameControllerImpl.getController().getChoice()) {
                        case ROCK:
                            drawable = R.drawable.rock;
                            break;
                        case SCISSORS:
                            drawable = R.drawable.scissors;
                            break;
                        case PAPER:
                            drawable = R.drawable.paper;
                            break;
                    }
                    this.setViews(drawable, String.format(this.getResources().getString(R.string.chosen),
                            GameControllerImpl.getController().getChoice().getName()));
                    break;
                case WIN:
                    this.setViews(R.drawable.win, R.string.win, View.VISIBLE);
                    break;
                case LOSE:
                    this.setViews(R.drawable.lose, R.string.lose, View.VISIBLE);
                    break;
                case DRAW:
                    this.setViews(R.drawable.draw, R.string.draw, View.VISIBLE);
                    break;
            }
        });
    }

    private void setViews(int drawable, int text, int visibility) {
        this.imageView.setImageResource(drawable);
        this.textView.setText(text);
        this.btnRestart.setVisibility(visibility);
    }

    private void setViews(int drawable, String text) {
        this.imageView.setImageResource(drawable);
        this.textView.setText(text);
        this.btnRestart.setVisibility(View.GONE);
    }
}