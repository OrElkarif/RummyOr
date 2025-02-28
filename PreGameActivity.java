package com.example.orproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class PreGameActivity extends AppCompatActivity {
    private RadioGroup radioGroup;
    private Button btnStartGame;
    private String selectedPlayer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_game);

        radioGroup = findViewById(R.id.radioGroupPlayers);
        btnStartGame = findViewById(R.id.btnStartGame);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioPlayer1) {
                selectedPlayer = "player1";
            } else if (checkedId == R.id.radioPlayer2) {
                selectedPlayer = "player2";
            }
        });

        btnStartGame.setOnClickListener(v -> {
            if (selectedPlayer.isEmpty()) {
                Toast.makeText(PreGameActivity.this, "בחר שחקן לפני שמתחילים!", Toast.LENGTH_SHORT).show();
                return;
            }

            FbModule fbModule = new FbModule(null);

            if (selectedPlayer.equals("player1")) {
                // אתחול המשחק רק אם זה שחקן 1
                fbModule.resetGame();
                fbModule.updateTurn("player1"); // שחקן 1 תמיד מתחיל
                Intent intent = new Intent(PreGameActivity.this, Player1Activity.class);
                startActivity(intent);
            } else {
                // שחקן 2 רק מצטרף למשחק קיים
                Intent intent = new Intent(PreGameActivity.this, Player2Activity.class);
                startActivity(intent);
            }
            finish();
        });
    }
}