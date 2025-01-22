package com.example.orproject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import java.util.ArrayList;

public class BoardGame extends View {
    private Deck deck;
    private Card[] ListOfCards = new Card[36];
    private Player p1 = new Player("Player 1");
    private Player p2 = new Player("Player 2");
    private ArrayList<Card> player1Cards = new ArrayList<>();
    private ArrayList<Card> player2Cards = new ArrayList<>();
    private Packet packet = new Packet();
    private boolean isPacketDrawn = true;
    private TextView tvAgainst;

    // Animation variables
    private Card animatingCard = null;
    private float animationProgress = 0;
    private final int ANIMATION_DURATION = 500;
    private float startX, startY, endX, endY;
    private ValueAnimator cardAnimator;
    private boolean isMovingToPlayer2 = false;

    public BoardGame(Context context) {
        super(context);
        if (context instanceof GameActivity) {
            tvAgainst = ((GameActivity) context).findViewById(R.id.tvAgainst);
            updatePlayer2CardCount();
        }
        initializeGame();
    }

    private void updatePlayer2CardCount() {
        if (tvAgainst != null) {
            tvAgainst.setText("Player 2 Cards: " + player2Cards.size());
        }
    }

    private void initializeGame() {
        deck = new Deck();
        deck.AddForDeck(ListOfCards);
        deck.Shuffle(ListOfCards);

        // חלוקת קלפים לשחקנים
        for (int i = 0; i < 4; i++) {
            player1Cards.add(ListOfCards[i]);
        }
        for (int i = 4; i < 8; i++) {
            player2Cards.add(ListOfCards[i]);
        }

        ArrayList<Card> remainingCards = new ArrayList<>();        // הוספת הקלפים הנותרים ל-Packet
        for (int i = 8; i < ListOfCards.length; i++) {
            remainingCards.add(ListOfCards[i]);
        }
        packet.addCards(remainingCards);

        updatePlayer2CardCount();
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawPacket(canvas);
        drawPlayerCards(canvas);
    }

    private void drawPlayerCards(Canvas canvas) {
        int spacing = 270;
        int startX = 30;
        int baseY = 1300;

        for (int i = 0; i < player1Cards.size(); i++) {
            Card currentCard = player1Cards.get(i);

            if (currentCard == animatingCard) {
                if (isMovingToPlayer2) {
                    float currentY = startY + (endY - startY) * animationProgress;
                    currentCard.setX((int)startX);
                    currentCard.setY((int)currentY);
                } else {
                    float currentX = startX + (endX - startX) * animationProgress;
                    float currentY = startY + (endY - startY) * animationProgress;
                    currentCard.setX((int)currentX);
                    currentCard.setY((int)currentY);
                }
            } else {
                currentCard.setX(startX + (i * spacing));
                currentCard.setY(baseY);
            }

            currentCard.draw(canvas);
        }
    }

    private void drawPacket(Canvas canvas) {
        Paint packetPaint = new Paint();
        packetPaint.setColor(Color.BLUE);
        packetPaint.setStyle(Paint.Style.FILL);

        int screenWidth = getWidth();
        int screenHeight = getHeight();
        int packetWidth = 300;
        int packetHeight = 400;
        int packetX = (screenWidth - packetWidth) / 2;
        int packetY = (screenHeight - packetHeight) / 2;

        canvas.drawRect(packetX, packetY, packetX + packetWidth, packetY + packetHeight, packetPaint);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("PACKET", packetX + packetWidth / 2, packetY + packetHeight / 2, textPaint);
    }

    public void drawAgainstPlayerCards(Canvas canvas) {
        Paint tvPaint = new Paint();
        tvPaint.setColor(Color.BLACK);
    }

    private void animateCardFromPacket(Card card, float finalX, float finalY) {
        animatingCard = card;
        isMovingToPlayer2 = false;

        startX = getWidth() / 2f - 130;
        startY = getHeight() / 2f - 200;
        endX = finalX;
        endY = finalY;

        startCardAnimation();
    }

    private void animateCardToPlayer2(Card card) {
        animatingCard = card;
        isMovingToPlayer2 = true;

        startX = card.getX();
        startY = card.getY();
        endX = startX;
        endY = -500;

        startCardAnimation();
    }

    private void startCardAnimation() {
        cardAnimator = ValueAnimator.ofFloat(0f, 1f);
        cardAnimator.setDuration(ANIMATION_DURATION);
        cardAnimator.setInterpolator(new DecelerateInterpolator());

        cardAnimator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });

        cardAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isMovingToPlayer2) {
                    player1Cards.remove(animatingCard);
                    updatePlayer2CardCount();
                }
                animatingCard = null;
                invalidate();
            }
        });

        cardAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isPacketTouched(touchX, touchY)) {
                if (!packet.isEmpty()) {
                    Card drawnCard = packet.drawCard();
                    player1Cards.add(drawnCard);

                    int index = player1Cards.size() - 1;
                    float finalX = 30 + (index * 270);
                    float finalY = 1300;

                    animateCardFromPacket(drawnCard, finalX, finalY);
                    return true;
                } else {
                    // Show a Toast message when the packet is empty
                    Toast.makeText(getContext(), "אין עוד קלפים בקופה!", Toast.LENGTH_SHORT).show();
                }
            }

            for (Card card : player1Cards) {
                if (card.isUserTouchMe(touchX, touchY)) {
                    player2Cards.add(card);
                    animateCardToPlayer2(card);
                    updatePlayer2CardCount();
                    RummyCheck(player1Cards);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isPacketTouched(int touchX, int touchY) {
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        int packetWidth = 300;
        int packetHeight = 400;
        int packetX = (screenWidth - packetWidth) / 2;
        int packetY = (screenHeight - packetHeight) / 2;

        return touchX >= packetX && touchX <= packetX + packetWidth &&
                touchY >= packetY && touchY <= packetY + packetHeight;
    }

    private void RummyCheck(ArrayList<Card> playerCards) {

        if (playerCards.size() < 4) {// פונקציה שבודקת אם יש רביעיות בקלפים של השחקן
            return;// אם אין מספיק קלפים לרביעייה, נצא מהפונקציה
        }

        for (int i = 0; i < playerCards.size(); i++) {

            Card currentCard = playerCards.get(i);// עובר על כל הקלפים בחפיסה
            int count = 0;


            if (currentCard == null) {// אם הקלף הוא null, נמשיך לקלף הבא
                continue;
            }


            String currentCategory = currentCard.getCatagory(); // בודק כמה קלפים זהים יש מאותה קטגוריה
            for (int j = 0; j < playerCards.size(); j++) {
                if (playerCards.get(j) != null &&
                        playerCards.get(j).getCatagory().equals(currentCategory)) {
                    count++;
                }
            }

            if (count == 4) {  // אם מצאנו רביעייה
                for (int j = 0; j < playerCards.size(); j++) {
                    // מוחק את כל הקלפים מאותה קטגוריה
                    if (playerCards.get(j) != null &&
                            playerCards.get(j).getCatagory().equals(currentCategory)) {
                        playerCards.remove(j);
                        j--;  // מתקן את האינדקס אחרי המחיקה
                    }
                }
            }
        }
    }
}

