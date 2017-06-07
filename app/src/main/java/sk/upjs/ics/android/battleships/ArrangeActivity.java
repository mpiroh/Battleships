package sk.upjs.ics.android.battleships;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import sk.upjs.ics.android.battleships.GameActivity.GameObject;

public class ArrangeActivity extends AppCompatActivity {

    private static final int maxN = 10;
    private ImageView[][] imageViews = new ImageView[maxN][maxN];
    private GameObject[][] boardPlayer = new GameObject[maxN][maxN];
    private GameObject[][] boardBot = new GameObject[maxN][maxN];
    private Drawable[] drawables = new Drawable[4]; // 0-empty, 1-miss, 2-ship, 3-crashed_ship
    private Drawable backgroundDrawable;
    private ImageButton shipButton;
    private int sizeSelected;
    private boolean rotation; // true-na_vysku, false-na_sirku
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrange);

        shipButton = (ImageButton) findViewById(R.id.shipButton);
        shipButton.setImageResource(R.drawable.ship4horiz);

        sizeSelected = 4;
        rotation = false;
        count = 1;

        setBackgroundColor();
        loadDrawables();
        designBoard();
    }

    private void loadDrawables() {
        drawables[0] = null;
        drawables[1] = getResources().getDrawable(R.drawable.miss);
        drawables[2] = getResources().getDrawable(R.drawable.ship);
        drawables[3] = getResources().getDrawable(R.drawable.crashed_ship);
        backgroundDrawable = getResources().getDrawable(R.drawable.cell_bg);
    }

    public void setBackgroundColor() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        LinearLayout activityLayout = (LinearLayout) findViewById(R.id.activityArrangeLayout);
        String colorString = sp.getString("colorList", "#ffffff");
        Log.d("COLORA", colorString);
        int bgColor = Color.parseColor(colorString);
        activityLayout.setBackgroundColor(bgColor);
        if (colorString.equals("#ffffff"))
            shipButton.setBackgroundColor(Color.parseColor("#C0C0C0"));
        else if (colorString.equals("#ef615c"))
            shipButton.setBackgroundColor(Color.parseColor("#ff2e2e"));
        else if (colorString.equals("#3968ea"))
            shipButton.setBackgroundColor(Color.parseColor("#00008B"));
        else if (colorString.equals("#C0C0C0"))
            shipButton.setBackgroundColor(Color.parseColor("#4c4c4c"));
    }

    @SuppressLint("NewApi")
    private void designBoard() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellSize = screenWidth / maxN;
        LinearLayout.LayoutParams lpRow = new LinearLayout.LayoutParams(cellSize * maxN, cellSize); // sirka riadka a vyska riadka
        LinearLayout.LayoutParams lpCell = new LinearLayout.LayoutParams(cellSize, cellSize); // sirka a vyska bunky

        LinearLayout boardLayout = (LinearLayout) findViewById(R.id.arrangeBoardLayout);

        for (int i = 0; i < maxN; i++) {
            LinearLayout rowLayout = new LinearLayout(this);
            for (int j = 0; j < maxN; j++) {
                imageViews[i][j] = new ImageView(this);
                imageViews[i][j].setBackground(backgroundDrawable);
                imageViews[i][j].setImageDrawable(drawables[0]);
                imageViews[i][j].setSoundEffectsEnabled(false);
                boardPlayer[i][j] = GameObject.Empty;
                boardBot[i][j] = GameObject.Empty;

                final int x = i;
                final int y = j;
                imageViews[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (fitsInPlayerBoard(x, y)) {
                            for (int i = 0; i < sizeSelected; i++) {
                                if (rotation) { // na vysku
                                    imageViews[x + i][y].setImageDrawable(drawables[2]);
                                    boardPlayer[x + i][y] = GameObject.Ship;
                                } else { // na sirku
                                    imageViews[x][y + i].setImageDrawable(drawables[2]);
                                    boardPlayer[x][y + i] = GameObject.Ship;
                                }
                            }
                            count--;
                            if (count == 0) {
                                sizeSelected--;
                                if (sizeSelected == 3) {
                                    if (rotation) { // na vysku
                                        shipButton.setImageResource(R.drawable.ship3vert);
                                    } else { // na sirku
                                        shipButton.setImageResource(R.drawable.ship3horiz);
                                    }
                                    count = 2;
                                } else if (sizeSelected == 2) {
                                    if (rotation) {
                                        shipButton.setImageResource(R.drawable.ship2vert);
                                    } else {
                                        shipButton.setImageResource(R.drawable.ship2horiz);
                                    }
                                    count = 3;
                                } else if (sizeSelected == 1) {
                                    shipButton.setImageResource(R.drawable.ship1);
                                    count = 2;
                                } else if (sizeSelected == 0) {
                                    arrangeBotShips();
                                    Intent intent = new Intent(ArrangeActivity.this, GameActivity.class);
                                    ArrayList<String> stringBoardPlayer = createStringBoardPlayer();
                                    ArrayList<String> stringBoardBot = createStringBoardBot();
                                    intent.putExtra("boardPlayer", stringBoardPlayer);
                                    intent.putExtra("boardBot", stringBoardBot);
                                    startActivity(intent);
                                }
                            }
                        }
                    }
                });
                rowLayout.addView(imageViews[i][j], lpCell);
            }
            boardLayout.addView(rowLayout, lpRow);
        }
    }

    public boolean fitsInPlayerBoard(int x, int y) {
        // skontrolujem ci nebude vytrcat mimo hracej plochy
        if (rotation) { // na vysku
            if (!(x + sizeSelected - 1 < maxN)) {
                return false;
            }
        } else { // na sirku
            if (!(y + sizeSelected - 1 < maxN)) {
                return false;
            }
        }

        // skontrolujem ci nebude prekrvat inu lod
        if (rotation) { // na vysku
            for (int i = 0; i < sizeSelected; i++) {
                if (boardPlayer[x + i][y] == GameObject.Ship) {
                    return false;
                }
            }
        } else { // na sirku
            for (int i = 0; i < sizeSelected; i++) {
                if (boardPlayer[x][y + i] == GameObject.Ship) {
                    return false;
                }
            }
        }

        // skontrolujem ci sa nebude dotykat inej lode
        if (rotation) { // na vysku
            for (int i = 0; i < sizeSelected; i++) {
                if ((x - 1 >= 0 && boardPlayer[x + i - 1][y] == GameObject.Ship) ||
                        (x - 1 >= 0 && y - 1 >= 0 && boardPlayer[x + i - 1][y - 1] == GameObject.Ship) ||
                        (y - 1 >= 0 && boardPlayer[x + i][y - 1] == GameObject.Ship) ||
                        (x + i + 1 < maxN && y - 1 >= 0 && boardPlayer[x + i + 1][y - 1] == GameObject.Ship) ||
                        (x + i + 1 < maxN && boardPlayer[x + i + 1][y] == GameObject.Ship) ||
                        (x + i + 1 < maxN && y + 1 < maxN && boardPlayer[x + i + 1][y + 1] == GameObject.Ship) ||
                        (y + 1 < maxN && boardPlayer[x + i][y + 1] == GameObject.Ship) ||
                        (x - 1 >= 0 && y + 1 < maxN && boardPlayer[x + i - 1][y + 1] == GameObject.Ship)) {
                    return false;
                }
            }
        } else { // na sirku
            for (int i = 0; i < sizeSelected; i++) {
                if ((x - 1 >= 0 && boardPlayer[x - 1][y + i] == GameObject.Ship) ||
                        (x - 1 >= 0 && y - 1 >= 0 && boardPlayer[x - 1][y + i - 1] == GameObject.Ship) ||
                        (y - 1 >= 0 && boardPlayer[x][y + i - 1] == GameObject.Ship) ||
                        (x + 1 < maxN && y - 1 >= 0 && boardPlayer[x + 1][y + i - 1] == GameObject.Ship) ||
                        (x + 1 < maxN && boardPlayer[x + 1][y + i] == GameObject.Ship) ||
                        (x + 1 < maxN && y + i + 1 < maxN && boardPlayer[x + 1][y + i + 1] == GameObject.Ship) ||
                        (y + i + 1 < maxN && boardPlayer[x][y + i + 1] == GameObject.Ship) ||
                        (x - 1 >= 0 && y + i + 1 < maxN && boardPlayer[x - 1][y + i + 1] == GameObject.Ship)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean fitsInBotBoard(int x, int y) {
        // skontrolujem ci nebude vytrcat mimo hracej plochy
        if (rotation) { // na vysku
            if (!(x + sizeSelected - 1 < maxN)) {
                return false;
            }
        } else { // na sirku
            if (!(y + sizeSelected - 1 < maxN)) {
                return false;
            }
        }

        // skontrolujem ci nebude prekrvat inu lod
        if (rotation) { // na vysku
            for (int i = 0; i < sizeSelected; i++) {
                if (boardBot[x + i][y] == GameObject.Ship) {
                    return false;
                }
            }
        } else { // na sirku
            for (int i = 0; i < sizeSelected; i++) {
                if (boardBot[x][y + i] == GameObject.Ship) {
                    return false;
                }
            }
        }

        // skontrolujem ci sa nebude dotykat inej lode
        if (rotation) { // na vysku
            for (int i = 0; i < sizeSelected; i++) {
                if ((x - 1 >= 0 && boardBot[x + i - 1][y] == GameObject.Ship) ||
                        (x - 1 >= 0 && y - 1 >= 0 && boardBot[x + i - 1][y - 1] == GameObject.Ship) ||
                        (y - 1 >= 0 && boardBot[x + i][y - 1] == GameObject.Ship) ||
                        (x + i + 1 < maxN && y - 1 >= 0 && boardBot[x + i + 1][y - 1] == GameObject.Ship) ||
                        (x + i + 1 < maxN && boardBot[x + i + 1][y] == GameObject.Ship) ||
                        (x + i + 1 < maxN && y + 1 < maxN && boardBot[x + i + 1][y + 1] == GameObject.Ship) ||
                        (y + 1 < maxN && boardBot[x + i][y + 1] == GameObject.Ship) ||
                        (x - 1 >= 0 && y + 1 < maxN && boardBot[x + i - 1][y + 1] == GameObject.Ship)) {
                    return false;
                }
            }
        } else { // na sirku
            for (int i = 0; i < sizeSelected; i++) {
                if ((x - 1 >= 0 && boardBot[x - 1][y + i] == GameObject.Ship) ||
                        (x - 1 >= 0 && y - 1 >= 0 && boardBot[x - 1][y + i - 1] == GameObject.Ship) ||
                        (y - 1 >= 0 && boardBot[x][y + i - 1] == GameObject.Ship) ||
                        (x + 1 < maxN && y - 1 >= 0 && boardBot[x + 1][y + i - 1] == GameObject.Ship) ||
                        (x + 1 < maxN && boardBot[x + 1][y + i] == GameObject.Ship) ||
                        (x + 1 < maxN && y + i + 1 < maxN && boardBot[x + 1][y + i + 1] == GameObject.Ship) ||
                        (y + i + 1 < maxN && boardBot[x][y + i + 1] == GameObject.Ship) ||
                        (x - 1 >= 0 && y + i + 1 < maxN && boardBot[x - 1][y + i + 1] == GameObject.Ship)) {
                    return false;
                }
            }
        }

        return true;
    }
    @SuppressLint("NewApi")
    public void onShipButtonClicked(View view) {
        switch (sizeSelected) {
            case 4:
                if (rotation) { // je na vysku
                    rotation = false;
                    shipButton.setImageResource(R.drawable.ship4horiz);
                } else { // je na sirku
                    rotation = true;
                    shipButton.setImageResource(R.drawable.ship4vert);
                }
                break;
            case 3:
                if (rotation) { // je na vysku
                    rotation = false;
                    shipButton.setImageResource(R.drawable.ship3horiz);
                } else { // je na sirku
                    rotation = true;
                    shipButton.setImageResource(R.drawable.ship3vert);
                }
                break;
            case 2:
                if (rotation) { // je na vysku
                    rotation = false;
                    shipButton.setImageResource(R.drawable.ship2horiz);
                } else { // je na sirku
                    rotation = true;
                    shipButton.setImageResource(R.drawable.ship2vert);
                }
                break;
            case 1:
                // netreba otacat
        }
    }

    public ArrayList<String> createStringBoardPlayer() {
        ArrayList<String> stringBoardPlayer = new ArrayList<>();
        for (int i = 0; i < maxN; i++) {
            for (int j = 0; j < maxN; j++) {
                if (boardPlayer[i][j] == GameObject.Empty) {
                    stringBoardPlayer.add("empty");
                } else if (boardPlayer[i][j] == GameObject.Ship) {
                    stringBoardPlayer.add("ship");
                }
            }
        }
        return stringBoardPlayer;
    }

    public ArrayList<String> createStringBoardBot() {
        ArrayList<String> stringBoardBot = new ArrayList<>();
        for (int i = 0; i < maxN; i++) {
            for (int j = 0; j < maxN; j++) {
                if (boardBot[i][j] == GameObject.Empty) {
                    stringBoardBot.add("empty");
                } else if (boardBot[i][j] == GameObject.Ship) {
                    stringBoardBot.add("ship");
                }
            }
        }
        return stringBoardBot;
    }

    public void arrangeBotShips() {
        sizeSelected = 4;
        count = 1;
        while (sizeSelected > 0) {
            int x, y;
            do {
                rotation = Math.random() < 0.5;
                x = (int) (Math.random() * 10);
                y = (int) (Math.random() * 10);
            } while (!fitsInBotBoard(x, y));

            if (rotation) { // if na vysku
                for (int i = 0; i < sizeSelected; i++) {
                    boardBot[x + i][y] = GameObject.Ship;
                }
            } else { // na sirku
                for (int i = 0; i < sizeSelected; i++) {
                    boardBot[x][y + i] = GameObject.Ship;
                }
            }

            count--;
            if (count == 0) {
                sizeSelected--;
                if (sizeSelected == 3) {
                    count = 2;
                } else if (sizeSelected == 2) {
                    count = 3;
                } else if (sizeSelected == 1) {
                    count = 2;
                }
            }
        }
    }
}

