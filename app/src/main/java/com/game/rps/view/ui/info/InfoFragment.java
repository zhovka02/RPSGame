package com.game.rps.view.ui.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.game.rps.R;
import com.game.rps.databinding.FragmentInfoBinding;
import com.game.rps.model.database.RPSDatabase;
import com.game.rps.model.database.RPSDatabaseImpl;
import com.game.rps.model.player.Player;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        this.binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();

        RPSDatabase rpsDatabase = RPSDatabaseImpl.getInstance(this.getContext());
        this.setPlayerInfo(rpsDatabase, root);
        this.createEditNicknameDialog(rpsDatabase, root);

        return root;
    }


    private void setPlayerInfo(RPSDatabase factory, View root) {
        ((TextView) root.findViewById(R.id.nicknamePlayer_text))
                .setText((factory.getPlayer().getName()));
        ((TextView) root.findViewById(R.id.winsPlayer_text))
                .setText(String.valueOf(factory.getPlayer().getWins()));
        ((TextView) root.findViewById(R.id.losesPlayer_text))
                .setText(String.valueOf(factory.getPlayer().getLosses()));
        ((TextView) root.findViewById(R.id.roundsPlayer_text))
                .setText(String.valueOf(factory.getPlayer().getRounds()));
    }

    private void createEditNicknameDialog(RPSDatabase factory, View root) {
        Button button = (Button) root.findViewById(R.id.buttonEditNickname);
        button.setOnClickListener(arg0 -> {
            LayoutInflater li = LayoutInflater.from(this.getContext());
            View promptsView = li.inflate(R.layout.promts, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this.requireContext());
            alertDialogBuilder.setView(promptsView);
            final EditText userInput = (EditText) promptsView
                    .findViewById(R.id.editTextDialogUserInput);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Ok",
                            (dialog, id) -> {
                                Player player = factory.getPlayer();
                                player.setName(userInput.getText().toString());
                                factory.updatePlayer(player);
                                ((TextView) root.findViewById(R.id.nicknamePlayer_text))
                                        .setText((factory.getPlayer().getName()));
                            })
                    .setNegativeButton("Cancel",
                            (dialog, id) -> dialog.cancel());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }
}