package com.example.orproject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.util.ArrayList;

public class BoardGame extends View {
    private boolean isPlayer1;
    private ArrayList<Card> myCards = new ArrayList<>();
    private ArrayList<Card> opponentCards = new ArrayList<>();
    private ArrayList<Card> player1Cards = new ArrayList<>();
    private ArrayList<Card> player2Cards = new ArrayList<>();
    private Packet packet = new Packet();
    private boolean isMyTurn = false;
    private TextView tvOpponentCards;
    private Paint backgroundPaint;
    private Card[] ListOfCards = new Card[36];
    private Deck deck;
    private FbModule fbModule;
    private static final int PACKET_X = 0;
    private static final int PACKET_Y = 0;
    private static final int PACKET_WIDTH = 260;
    private static final int PACKET_HEIGHT = 400;

    public BoardGame(Context context, boolean isPlayer1) {
        super(context);
        this.isPlayer1 = isPlayer1;
        this.fbModule = new FbModule(null);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#D6EAF8"));
        backgroundPaint.setStyle(Paint.Style.FILL);

        if (context instanceof GameActivity) {
            tvOpponentCards = ((GameActivity) context).findViewById(R.id.tvAgainst);
            updateOpponentCardCount();
        }

        initializeGame();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && isMyTurn) {
            float touchX = event.getX();
            float touchY = event.getY();

            // Check if packet was touched
            if (isPacketTouched(touchX, touchY)) {
                handlePacketTouch();
                return true;
            }

            // Check if any card was touched
            for (Card card : myCards) {
                if (card.isUserTouchMe((int)touchX, (int)touchY)) {
                    handleCardTouch(card);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isPacketTouched(float x, float y) {
        int packetX = getWidth()/2 - 130;
        int packetY = getHeight()/2 - 200;
        return x >= packetX && x <= packetX + 260 &&
                y >= packetY && y <= packetY + 400;
    }

    private void handlePacketTouch() {
        if (!packet.isEmpty()) {
            Card drawnCard = packet.drawCard();
            if (drawnCard != null) {
                myCards.add(drawnCard);

                // Update Firebase
                if (isPlayer1) {
                    fbModule.updatePlayer1Cards(myCards);
                } else {
                    fbModule.updatePlayer2Cards(myCards);
                }
                fbModule.updatePacket(packet.getAllCards());

                // Switch turn
                fbModule.switchTurn();

                invalidate();
            }
        }
    }

    private void handleCardTouch(Card card) {
        // Remove card from current player's hand
        myCards.remove(card);

        // Add card to opponent's hand
        opponentCards.add(card);

        // Update Firebase
        if (isPlayer1) {
            fbModule.updatePlayer1Cards(myCards);
            fbModule.updatePlayer2Cards(opponentCards);
        } else {
            fbModule.updatePlayer2Cards(myCards);
            fbModule.updatePlayer1Cards(opponentCards);
        }

        // Switch turn
        fbModule.switchTurn();

        invalidate();
    }

    private void initializeGame() {
        deck = new Deck();
        deck.AddForDeck(ListOfCards);
        deck.Shuffle(ListOfCards);

        // Initial card distribution
        ArrayList<Card> initialCards = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (isPlayer1) {
                initialCards.add(ListOfCards[i]);
            } else {
                initialCards.add(ListOfCards[i + 4]);
            }
        }
        myCards = initialCards;

        // Add remaining cards to packet
        ArrayList<Card> remainingCards = new ArrayList<>();
        for (int i = 8; i < ListOfCards.length; i++) {
            remainingCards.add(ListOfCards[i]);
        }
        packet.setCards(remainingCards);

        // Update Firebase
        if (isPlayer1) {
            fbModule.updatePlayer1Cards(myCards);
        } else {
            fbModule.updatePlayer2Cards(myCards);
        }
        fbModule.updatePacket(remainingCards);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        drawPacket(canvas);
        drawPlayerCards(canvas, myCards);
        drawOpponentInfo(canvas);

        if (isMyTurn) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(60);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Your Turn!", getWidth() / 2, 200, textPaint);
        }
    }

    private void drawPacket(Canvas canvas) {
        // Draw packet in red like the cards
        Paint packetPaint = new Paint();
        packetPaint.setColor(Color.RED);
        int packetX = getWidth()/2 - 130;
        int packetY = getHeight()/2 - 200;
        canvas.drawRect(packetX, packetY,
                packetX + 260, packetY + 400, packetPaint);

        // Add text "Packet" on the card
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Packet", packetX + 130, packetY + 200, textPaint);
    }

    private void drawPlayerCards(Canvas canvas, ArrayList<Card> cards) {
        int spacing = 280; // Space between cards
        int startX = (getWidth() - (cards.size() * spacing)) / 2;

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            card.setX(startX + (i * spacing));
            card.setY(getHeight() - 450);
            card.draw(canvas);
        }
    }

    private void drawOpponentInfo(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.LEFT);
        String opponentName = isPlayer1 ? "Player 2" : "Player 1";
        canvas.drawText(opponentName + "'s Cards: " + opponentCards.size(),
                50, 100, textPaint);
    }

    public void updatePlayerCards(ArrayList<Card> cards) {
        if ((isPlayer1 && cards == player1Cards) || (!isPlayer1 && cards == player2Cards)) {
            myCards = new ArrayList<>(cards);
        } else {
            opponentCards = new ArrayList<>(cards);
        }
        updateOpponentCardCount();
        invalidate();
    }

    private void updateOpponentCardCount() {
        if (tvOpponentCards != null) {
            String opponentName = isPlayer1 ? "Player 2" : "Player 1";
            tvOpponentCards.setText(opponentName + "'s Cards: " + opponentCards.size());
        }
    }

    public void setTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        invalidate();
    }

    public void updatePacket(ArrayList<Card> cards) {
        packet.setCards(cards);
        invalidate();
    }

    public void updateScore(int score) {
        invalidate();
    }
}