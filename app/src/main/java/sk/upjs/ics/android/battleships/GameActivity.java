package sk.upjs.ics.android.battleships;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import sk.upjs.ics.android.battleships.provider.Provider;
import sk.upjs.ics.android.battleships.provider.ScoreContentProvider;

public class GameActivity extends AppCompatActivity {

    private static final int maxN = 10;
    private TextView turnTextView;
    private ImageView[][] imageViews = new ImageView[maxN][maxN];
    private GameObject[][] boardPlayer = new GameObject[maxN][maxN];
    private GameObject[][] boardBot = new GameObject[maxN][maxN];
    private Drawable[] drawables = new Drawable[4]; // 0-empty, 1-miss, 2-ship, 3-crashed_ship
    private Drawable backgroundDrawable;
    private boolean myTurn;
    private Difficulty difficulty;
    private int speed;
    private Queue<int[]> toShoot = new LinkedList<>();
    private int[] point = new int[2];
    private int playerCellsRemaining = 18; // number of players' ship cells remaining
    private int botCellsRemaining = 18; // number of bots' ship cells remaining
    private MediaPlayer waterMediaPlayer;
    private MediaPlayer gunMediaPlayer;
    private int playerPoints = 0;
    private String nick;
    private int shotsNumber = 0;
    private boolean soundsOn;
    private boolean isReleased = false;

    public enum GameObject {
        Empty, Miss, Ship, CrashedShip
    }

    public enum Difficulty {
        Easy, Normal, Hard
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        loadDrawables();
        applySettings();
        loadArrangedShips();
        designBoard();
        initGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isReleased = false;
        waterMediaPlayer = MediaPlayer.create(this, R.raw.watersplash);
        gunMediaPlayer = MediaPlayer.create(this, R.raw.gunhit);
    }

    @Override
    protected void onPause() {
        isReleased = true;
        waterMediaPlayer.release();
        gunMediaPlayer.release();
        super.onPause();
    }

    private void loadDrawables() {
        drawables[0] = null;
        drawables[1] = getResources().getDrawable(R.drawable.miss);
        drawables[2] = getResources().getDrawable(R.drawable.ship);
        drawables[3] = getResources().getDrawable(R.drawable.crashed_ship);
        backgroundDrawable = getResources().getDrawable(R.drawable.cell_bg);
    }

    @SuppressLint("NewApi")
    private void designBoard() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellSize = screenWidth / maxN;
        LinearLayout.LayoutParams lpRow = new LinearLayout.LayoutParams(cellSize * maxN, cellSize); // sirka riadka a vyska riadka
        LinearLayout.LayoutParams lpCell = new LinearLayout.LayoutParams(cellSize, cellSize); // sirka a vyska bunky

        LinearLayout boardLayout = (LinearLayout) findViewById(R.id.boardLayout);

        for (int i = 0; i < maxN; i++) {
            LinearLayout rowLayout = new LinearLayout(this);
            for (int j = 0; j < maxN; j++) {
                imageViews[i][j] = new ImageView(this);
                imageViews[i][j].setBackground(backgroundDrawable);
                imageViews[i][j].setSoundEffectsEnabled(false);

                final int x = i;
                final int y = j;
                imageViews[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (myTurn && boardBot[x][y] != GameObject.Miss &&
                                boardBot[x][y] != GameObject.CrashedShip) {
                            if (boardBot[x][y] == GameObject.Empty) {
                                boardBot[x][y] = GameObject.Miss;
                                imageViews[x][y].setImageDrawable(drawables[1]);
                                if (soundsOn && !isReleased)
                                    waterMediaPlayer.start();
                            } else if (boardBot[x][y] == GameObject.Ship) {
                                boardBot[x][y] = GameObject.CrashedShip;
                                imageViews[x][y].setImageDrawable(drawables[3]);
                                if (soundsOn && !isReleased)
                                    gunMediaPlayer.start();
                                addPoints();
                                botCellsRemaining--;

                                if (botCellsRemaining == 0) {
                                    playerPoints += 100;
                                    displayDialog(true);
                                    return;
                                }
                            }
                            shotsNumber++;

                            Timer timer = new Timer();
                            TimerTask timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            turnTextView.setText("Computer");
                                            drawPlayerBoard();
                                        }
                                    });
                                }
                            };
                            timer.schedule(timerTask, speed);

                            timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    int[] xy = getBotDecision();
                                    final int xShot = xy[0];
                                    final int yShot = xy[1];
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (boardPlayer[xShot][yShot] == GameObject.Empty) {
                                                boardPlayer[xShot][yShot] = GameObject.Miss;
                                                imageViews[xShot][yShot].setImageDrawable(drawables[1]);
                                                if (soundsOn && !isReleased)
                                                    waterMediaPlayer.start();
                                            } else if (boardPlayer[xShot][yShot] == GameObject.Ship) {
                                                boardPlayer[xShot][yShot] = GameObject.CrashedShip;
                                                imageViews[xShot][yShot].setImageDrawable(drawables[3]);
                                                if (soundsOn && !isReleased)
                                                    gunMediaPlayer.start();
                                                playerCellsRemaining--;

                                                if (playerCellsRemaining == 0) {
                                                    displayDialog(false);
                                                    return;
                                                }
                                            }
                                        }
                                    });
                                }
                            };
                            timer.schedule(timerTask, speed * 2 + 500);

                            timerTask = new TimerTask() {
                                @Override
                                public void run() {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            turnTextView.setText(nick);
                                            drawBotBoard();
                                            //for (int i = 0; i < maxN; i++) {
                                            //    String string = "";
                                            //    for (int j = 0; j < maxN; j++) {
                                            //        string = string + boardBot[i][j].toString() + " ";
                                            //    }
                                            //    Log.d("riadok" + i, string);
                                            //}
                                        }
                                    });
                                    myTurn = true;
                                }
                            };
                            timer.schedule(timerTask, speed * 3 + 500);
                        }
                    }
                });
                rowLayout.addView(imageViews[i][j], lpCell);
            }
            boardLayout.addView(rowLayout, lpRow);
        }
    }

    // sets difficulty, speed and background color
    public void applySettings() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        switch (sp.getString("difficultyList", "normal")) {
            case "easy":
                difficulty = Difficulty.Easy;
                break;
            case "normal":
                difficulty = Difficulty.Normal;
                break;
            case "hard":
                difficulty = Difficulty.Hard;
        }

        speed = Integer.parseInt(sp.getString("speedList", "1750"));

        LinearLayout activityLayout = (LinearLayout) findViewById(R.id.activityGameLayout);
        int bgColor = Color.parseColor(sp.getString("colorList", "#ffffff"));
        activityLayout.setBackgroundColor(bgColor);

        nick = sp.getString("nickEditText", "Player A");

        soundsOn = sp.getBoolean("soundsSwitch", true);
    }

    public void initGame() {
        boolean firstTurn = Math.random() < 0.5; // true-player plays first, false-bot plays first
        turnTextView = (TextView) findViewById(R.id.turnTextView);
        if (firstTurn) {
            myTurn = true;
            turnTextView.setText(nick);
            Toast.makeText(this, "You play first.", Toast.LENGTH_SHORT).show();

            drawBotBoard();
        } else {
            myTurn = false;
            turnTextView.setText("Computer");
            Toast.makeText(this, "Computer plays first.", Toast.LENGTH_SHORT).show();

            drawPlayerBoard();

            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    int[] xy = getBotDecision();
                    final int xShot = xy[0];
                    final int yShot = xy[1];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (boardPlayer[xShot][yShot] == GameObject.Empty) {
                                boardPlayer[xShot][yShot] = GameObject.Miss;
                                imageViews[xShot][yShot].setImageDrawable(drawables[1]);
                                if (soundsOn && !isReleased)
                                    waterMediaPlayer.start();
                            } else if (boardPlayer[xShot][yShot] == GameObject.Ship) {
                                boardPlayer[xShot][yShot] = GameObject.CrashedShip;
                                imageViews[xShot][yShot].setImageDrawable(drawables[3]);
                                if (soundsOn && !isReleased)
                                    gunMediaPlayer.start();
                                playerCellsRemaining--;
                            }
                        }
                    });
                }
            };
            timer.schedule(timerTask, speed);

            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            turnTextView.setText(nick);
                            drawBotBoard();
                        }
                    });
                    myTurn = true;
                }
            };
            timer.schedule(timerTask, speed * 2);
        }
    }

    public void loadArrangedShips() {
        // load players' ships
        ArrayList<String> stringBoardPlayer = getIntent().getStringArrayListExtra("boardPlayer");
        int i = 0;
        int j = 0;
        for (String s : stringBoardPlayer) {
            if (s.equals("empty")) {
                boardPlayer[i][j] = GameObject.Empty;
            } else if (s.equals("ship")) {
                boardPlayer[i][j] = GameObject.Ship;
            }
            j++;
            if (j == maxN) {
                i++;
                j = 0;
            }
        }

        //load bots' ships
        ArrayList<String> stringBoardBot = getIntent().getStringArrayListExtra("boardBot");
        i = 0;
        j = 0;
        for (String s : stringBoardBot) {
            if (s.equals("empty")) {
                boardBot[i][j] = GameObject.Empty;
            } else if (s.equals("ship")) {
                boardBot[i][j] = GameObject.Ship;
            }
            j++;
            if (j == maxN) {
                i++;
                j = 0;
            }
        }
    }

    public void drawPlayerBoard() {
        for (int i = 0; i < maxN; i++) {
            for (int j = 0; j < maxN; j++) {
                if (boardPlayer[i][j] == GameObject.Empty) {
                    imageViews[i][j].setImageDrawable(drawables[0]);
                } else if (boardPlayer[i][j] == GameObject.Miss) {
                    imageViews[i][j].setImageDrawable(drawables[1]);
                } else if (boardPlayer[i][j] == GameObject.Ship) {
                    imageViews[i][j].setImageDrawable(drawables[2]);
                } else { // (boardPlayer[i][j] == GameObject.CrashedShip)
                    imageViews[i][j].setImageDrawable(drawables[3]);
                }
            }
        }
    }

    public void drawBotBoard() {
        for (int i = 0; i < maxN; i++) {
            for (int j = 0; j < maxN; j++) {
                if (boardBot[i][j] == GameObject.Empty || boardBot[i][j] == GameObject.Ship) {
                    imageViews[i][j].setImageDrawable(drawables[0]);
                } else if (boardBot[i][j] == GameObject.Miss) {
                    imageViews[i][j].setImageDrawable(drawables[1]);
                } else { // (boardBot[i][j] == GameObject.CrashedShip)
                    imageViews[i][j].setImageDrawable(drawables[3]);
                }
            }
        }
    }

    public int[] getBotDecision() {
        int[] xy = new int[2];
        int x, y;

        if (difficulty == Difficulty.Easy) {
            do {
                x = (int) (Math.random() * 10);
                y = (int) (Math.random() * 10);
            } while (boardPlayer[x][y] == GameObject.Miss ||
                    boardPlayer[x][y] == GameObject.CrashedShip);
            xy[0] = x;
            xy[1] = y;
            return xy;
        } else if (difficulty == Difficulty.Normal) {
            if (toShoot.isEmpty()) {
                do {
                    x = (int) (Math.random() * 10);
                    y = (int) (Math.random() * 10);
                } while (boardPlayer[x][y] == GameObject.Miss ||
                        boardPlayer[x][y] == GameObject.CrashedShip);

                if (boardPlayer[x][y] == GameObject.Ship) { // if zasiahol
                    point[0] = x;
                    point[1] = y;

                    if (x + 1 < maxN && (boardPlayer[x + 1][y] == GameObject.Empty || boardPlayer[x + 1][y] == GameObject.Ship))
                        toShoot.add(new int[]{x + 1, y});
                    if (x - 1 >= 0 && (boardPlayer[x - 1][y] == GameObject.Empty || boardPlayer[x - 1][y] == GameObject.Ship))
                        toShoot.add(new int[]{x - 1, y});
                    if (y + 1 < maxN && (boardPlayer[x][y + 1] == GameObject.Empty || boardPlayer[x][y + 1] == GameObject.Ship))
                        toShoot.add(new int[]{x, y + 1});
                    if (y - 1 >= 0 && (boardPlayer[x][y - 1] == GameObject.Empty || boardPlayer[x][y - 1] == GameObject.Ship))
                        toShoot.add(new int[]{x, y - 1});
                }

                xy[0] = x;
                xy[1] = y;
                return xy;
            } else {
                xy = toShoot.poll();
                x = xy[0];
                y = xy[1];

                if (boardPlayer[x][y] == GameObject.Ship) { // if zasiahol
                    toShoot.clear();

                    if (x - 1 >= 0 && boardPlayer[x - 1][y] == GameObject.CrashedShip) {
                        if (x + 1 < maxN)
                            toShoot.add(new int[]{x + 1, y});
                    } else if (x + 1 < maxN && boardPlayer[x + 1][y] == GameObject.CrashedShip) {
                        if (x - 1 >= 0)
                            toShoot.add(new int[]{x - 1, y});
                    } else if (y - 1 >= 0 && boardPlayer[x][y - 1] == GameObject.CrashedShip) {
                        if (y + 1 < maxN)
                            toShoot.add(new int[]{x, y + 1});
                    } else if (y + 1 < maxN && boardPlayer[x][y + 1] == GameObject.CrashedShip) {
                        if (y - 1 >= 0)
                            toShoot.add(new int[]{x, y - 1});
                    }
                } else if (boardPlayer[x][y] == GameObject.Empty) {
                    if (point[0] - 1 >= 0 && boardPlayer[point[0] - 1][point[1]] == GameObject.CrashedShip) {
                        if (point[0] + 1 < maxN)
                            toShoot.add(new int[]{point[0] + 1, point[1]});
                    } else if (point[0] + 1 < maxN && boardPlayer[point[0] + 1][1] == GameObject.CrashedShip) {
                        if (point[0] - 1 >= 0)
                            toShoot.add(new int[]{point[0] - 1, point[1]});
                    } else if (point[1] - 1 >= 0 && boardPlayer[point[0]][point[1] - 1] == GameObject.CrashedShip) {
                        if (point[1] + 1 < maxN)
                            toShoot.add(new int[]{point[0], point[1] + 1});
                    } else if (point[1] + 1 < maxN && boardPlayer[point[0]][point[1] + 1] == GameObject.CrashedShip) {
                        if (point[1] - 1 >= 0)
                            toShoot.add(new int[]{point[0], point[1] - 1});
                    }
                }

                return xy;
            }
        } else { // if (difficulty == Difficulty.Hard)
            if (toShoot.isEmpty()) {
                do {
                    x = (int) (Math.random() * 10);
                    y = (int) (Math.random() * 10);
                } while ((boardPlayer[x][y] == GameObject.Miss ||
                        boardPlayer[x][y] == GameObject.CrashedShip) || !checkCell(x, y));

                if (boardPlayer[x][y] == GameObject.Ship) { // if zasiahol
                    point[0] = x;
                    point[1] = y;

                    if (x + 1 < maxN && (boardPlayer[x + 1][y] == GameObject.Empty || boardPlayer[x + 1][y] == GameObject.Ship))
                        toShoot.add(new int[]{x + 1, y});
                    if (x - 1 >= 0 && (boardPlayer[x - 1][y] == GameObject.Empty || boardPlayer[x - 1][y] == GameObject.Ship))
                        toShoot.add(new int[]{x - 1, y});
                    if (y + 1 < maxN && (boardPlayer[x][y + 1] == GameObject.Empty || boardPlayer[x][y + 1] == GameObject.Ship))
                        toShoot.add(new int[]{x, y + 1});
                    if (y - 1 >= 0 && (boardPlayer[x][y - 1] == GameObject.Empty || boardPlayer[x][y - 1] == GameObject.Ship))
                        toShoot.add(new int[]{x, y - 1});
                }

                xy[0] = x;
                xy[1] = y;
                return xy;
            } else {
                xy = toShoot.poll();
                x = xy[0];
                y = xy[1];

                if (boardPlayer[x][y] == GameObject.Ship) { // if zasiahol
                    toShoot.clear();

                    if (x - 1 >= 0 && boardPlayer[x - 1][y] == GameObject.CrashedShip) {
                        if (x + 1 < maxN)
                            toShoot.add(new int[]{x + 1, y});
                    } else if (x + 1 < maxN && boardPlayer[x + 1][y] == GameObject.CrashedShip) {
                        if (x - 1 >= 0)
                            toShoot.add(new int[]{x - 1, y});
                    } else if (y - 1 >= 0 && boardPlayer[x][y - 1] == GameObject.CrashedShip) {
                        if (y + 1 < maxN)
                            toShoot.add(new int[]{x, y + 1});
                    } else if (y + 1 < maxN && boardPlayer[x][y + 1] == GameObject.CrashedShip) {
                        if (y - 1 >= 0)
                            toShoot.add(new int[]{x, y - 1});
                    }
                } else if (boardPlayer[x][y] == GameObject.Empty) {
                    if (point[0] - 1 >= 0 && boardPlayer[point[0] - 1][point[1]] == GameObject.CrashedShip) {
                        if (point[0] + 1 < maxN)
                            toShoot.add(new int[]{point[0] + 1, point[1]});
                    } else if (point[0] + 1 < maxN && boardPlayer[point[0] + 1][1] == GameObject.CrashedShip) {
                        if (point[0] - 1 >= 0)
                            toShoot.add(new int[]{point[0] - 1, point[1]});
                    } else if (point[1] - 1 >= 0 && boardPlayer[point[0]][point[1] - 1] == GameObject.CrashedShip) {
                        if (point[1] + 1 < maxN)
                            toShoot.add(new int[]{point[0], point[1] + 1});
                    } else if (point[1] + 1 < maxN && boardPlayer[point[0]][point[1] + 1] == GameObject.CrashedShip) {
                        if (point[1] - 1 >= 0)
                            toShoot.add(new int[]{point[0], point[1] - 1});
                    }
                }
                return xy;
            }
        }
    }

    public boolean checkCell(int x, int y) {
        if (x - 1 >= 0) {
            if (boardPlayer[x - 1][y] == GameObject.CrashedShip) {
                return false;
            }
        }
        if (x + 1 < maxN) {
            if (boardPlayer[x + 1][y] == GameObject.CrashedShip) {
                return false;
            }
        }
        if (y - 1 >= 0) {
            if (boardPlayer[x][y - 1] == GameObject.CrashedShip) {
                return false;
            }
        }
        if (y + 1 < maxN) {
            if (boardPlayer[x][y + 1] == GameObject.CrashedShip) {
                return false;
            }
        }
        if (x - 1 >= 0 && y - 1 >= 0) {
            if (boardPlayer[x - 1][y - 1] == GameObject.CrashedShip) {
                return false;
            }
        }
        if (x - 1 >= 0 && y + 1 < maxN) {
            if (boardPlayer[x - 1][y + 1] == GameObject.CrashedShip) {
                return false;
            }
        }
        if (x + 1 < maxN && y - 1 >= 0) {
            if (boardPlayer[x + 1][y - 1] == GameObject.CrashedShip) {
                return false;
            }
        }
        if (x + 1 < maxN && y + 1 < maxN) {
            if (boardPlayer[x + 1][y + 1] == GameObject.CrashedShip) {
                return false;
            }
        }

        return true;
    }

    public void displayDialog(boolean playerWon) {
        if (playerWon) {
            new AlertDialog.Builder(GameActivity.this)
                    .setTitle("Victory")
                    .setMessage("You won.\nDo you want to save your score?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String diff = null;
                            switch (difficulty) {
                                case Easy:
                                    diff = "easy";
                                    break;
                                case Normal:
                                    diff = "normal";
                                    break;
                                case Hard:
                                    diff = "hard";
                            }
                            Log.d("here", "3");
                            insertIntoContentProvider(nick, 1, System.currentTimeMillis() / 1000, diff, shotsNumber, playerPoints);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            new AlertDialog.Builder(GameActivity.this)
                    .setTitle("Game over")
                    .setMessage("You lost.\nDo you want to save your score?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String diff = null;
                            switch (difficulty) {
                                case Easy:
                                    diff = "easy";
                                    break;
                                case Normal:
                                    diff = "normal";
                                    break;
                                case Hard:
                                    diff = "hard";
                            }
                            insertIntoContentProvider(nick, 0, System.currentTimeMillis() / 1000, diff, shotsNumber, playerPoints);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    public void insertIntoContentProvider(String nick, int winner, long time, String diff, int shotsNumber, int points) {
        ContentValues values = new ContentValues();
        values.put(Provider.Score.NICK, nick);
        values.put(Provider.Score.WINNER, winner);
        values.put(Provider.Score.TIME, time);
        values.put(Provider.Score.DIFFICULTY, diff);
        values.put(Provider.Score.SHOTS_NUMBER, shotsNumber);
        values.put(Provider.Score.POINTS, points);

        AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                Toast.makeText(GameActivity.this, "Data was saved.", Toast.LENGTH_SHORT).show();
            }
        };
        Log.d("here", "4");
        handler.startInsert(0, null, ScoreContentProvider.CONTENT_URI, values);
    }

    public void addPoints() {
        if (difficulty == Difficulty.Easy) {
            playerPoints += Math.round(300 / 18);
        } else if (difficulty == Difficulty.Normal) {
            playerPoints += Math.round(600 / 18);
        } else {
            playerPoints += Math.round(900 / 18);
        }
    }

    public void subtractPoints() {
        if (difficulty == Difficulty.Easy) {
            playerPoints -= Math.round(300 / 18 / 2);
        } else if (difficulty == Difficulty.Normal) {
            playerPoints -= Math.round(600 / 18 / 2);
        } else {
            playerPoints -= Math.round(900 / 18 / 2);
        }
    }
}