package com.example.orproject;

import java.util.ArrayList;
import java.util.Random;

public class Packet {
    private static final int MAX_CARDS = 28; // מספר מקסימלי של קלפים בקופה
    private ArrayList<Card> cards; // רשימת הקלפים בקופה

    // בנאי של הקופה
    public Packet() {
        cards = new ArrayList<>();
    }

    // פעולה לערבוב הקלפים בקופה
    public void shuffle() {
        Random random = new Random();
        for (int i = cards.size() - 1; i > 0; i--) {
            int randomIndex = random.nextInt(i + 1);
            Card temp = cards.get(i);
            cards.set(i, cards.get(randomIndex));
            cards.set(randomIndex, temp);
        }
    }

    // הוספת רשימת קלפים לקופה (עד למספר המקסימלי)
    public void addCards(ArrayList<Card> newCards) {
        for (Card card : newCards) {
            if (cards.size() < MAX_CARDS) {
                cards.add(card);
            } else {
                break; // עצירה אם הגענו למספר המקסימלי
            }
        }
    }

    // משיכת קלף מהקופה
    public Card drawCard() {
        if (isEmpty()) {
            return null; // החזרת null אם הקופה ריקה
        }
        return cards.remove(0); // הסרת הקלף הראשון והחזרתו
    }

    // בדיקה אם הקופה ריקה
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    // קבלת מספר הקלפים בקופה
    public int size() {
        return cards.size();
    }

    // קבלת רשימת כל הקלפים בקופה
    public ArrayList<Card> getAllCards() {
        return new ArrayList<>(cards);
    }
}
