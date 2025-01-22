package com.example.orproject;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class FbModule {
    private Context context;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference gameStateRef;
    private GameStateListener gameStateListener;

    public interface GameStateListener {
        void onPlayer1CardsChanged(ArrayList<Card> cards);
        void onPlayer2CardsChanged(ArrayList<Card> cards);
        void onPacketChanged(ArrayList<Card> cards);
        void onTurnChanged(String currentPlayer);
    }

    public FbModule(Context context, GameStateListener listener) {
        this.context = context;
        this.gameStateListener = listener;
        firebaseDatabase = FirebaseDatabase.getInstance();
        gameStateRef = firebaseDatabase.getReference("gameState");
        setupListeners();
    }

    private void setupListeners() {
        // Listen for Player 1's cards
        gameStateRef.child("player1Cards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Card> cards = new ArrayList<>();
                for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                    CardData cardData = cardSnapshot.getValue(CardData.class);
                    if (cardData != null) {
                        cards.add(new Card(cardData.category, cardData.name, cardData.id));
                    }
                }
                if (gameStateListener != null) {
                    gameStateListener.onPlayer1CardsChanged(cards);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Listen for Player 2's cards
        gameStateRef.child("player2Cards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Card> cards = new ArrayList<>();
                for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                    CardData cardData = cardSnapshot.getValue(CardData.class);
                    if (cardData != null) {
                        cards.add(new Card(cardData.category, cardData.name, cardData.id));
                    }
                }
                if (gameStateListener != null) {
                    gameStateListener.onPlayer2CardsChanged(cards);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Listen for packet changes
        gameStateRef.child("packet").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Card> cards = new ArrayList<>();
                for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                    CardData cardData = cardSnapshot.getValue(CardData.class);
                    if (cardData != null) {
                        cards.add(new Card(cardData.category, cardData.name, cardData.id));
                    }
                }
                if (gameStateListener != null) {
                    gameStateListener.onPacketChanged(cards);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Listen for turn changes
        gameStateRef.child("currentTurn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentPlayer = snapshot.getValue(String.class);
                if (gameStateListener != null && currentPlayer != null) {
                    gameStateListener.onTurnChanged(currentPlayer);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Helper class for Firebase serialization
    private static class CardData {
        public String category;
        public String name;
        public int id;

        public CardData() {} // Required for Firebase

        public CardData(String category, String name, int id) {
            this.category = category;
            this.name = name;
            this.id = id;
        }
    }

    // Methods to update game state
    public void updatePlayer1Cards(ArrayList<Card> cards) {
        ArrayList<CardData> cardDataList = new ArrayList<>();
        for (Card card : cards) {
            cardDataList.add(new CardData(card.getCatagory(), card.getCardName(), card.getId()));
        }
        gameStateRef.child("player1Cards").setValue(cardDataList);
    }

    public void updatePlayer2Cards(ArrayList<Card> cards) {
        ArrayList<CardData> cardDataList = new ArrayList<>();
        for (Card card : cards) {
            cardDataList.add(new CardData(card.getCatagory(), card.getCardName(), card.getId()));
        }
        gameStateRef.child("player2Cards").setValue(cardDataList);
    }

    public void updatePacket(ArrayList<Card> cards) {
        ArrayList<CardData> cardDataList = new ArrayList<>();
        for (Card card : cards) {
            cardDataList.add(new CardData(card.getCatagory(), card.getCardName(), card.getId()));
        }
        gameStateRef.child("packet").setValue(cardDataList);
    }

    public void updateTurn(String player) {
        gameStateRef.child("currentTurn").setValue(player);
    }

    public void resetGame() {
        gameStateRef.setValue(null);
    }
}