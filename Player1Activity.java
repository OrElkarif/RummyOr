package com.example.orproject;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class Player1Activity extends AppCompatActivity {
    private BoardGame boardGame;
    private FbModule fbModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        LinearLayout ll = findViewById(R.id.ll);
        boardGame = new BoardGame(this, true); // true for Player1
        ll.addView(boardGame);

        fbModule = new FbModule(new FbModule.GameStateListener() {
            @Override
            public void onPlayer1CardsChanged(ArrayList<Card> cards) {
                boardGame.updatePlayerCards(cards);
            }

            @Override
            public void onPlayer2CardsChanged(ArrayList<Card> cards) {
                // עדכון מספר הקלפים של היריב
                boardGame.updatePlayerCards(cards);
            }

            @Override
            public void onPacketChanged(ArrayList<Card> cards) {
                boardGame.updatePacket(cards);
            }

            @Override
            public void onTurnChanged(String currentPlayer) {
                boardGame.setTurn(currentPlayer.equals("player1"));
            }

            @Override
            public void onPlayerScoreUpdated(String player, int score) {
                if (player.equals("player1")) {
                    boardGame.updateScore(score);
                }
            }
        });
    }
}