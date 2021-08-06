package com.tgknyhn.hexgame;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // final fields
    public static int MAX_SIZE = 9;
    public static int MIN_SIZE = 5;

    // Cells
    private ArrayList<Button> cells = new ArrayList<>();
    private Button[][] hexCells;

    // Buttons
    private Button button_NewGame;
    private Button button_MoveBack;
    private Button button_Settings;

    // Scores
    private ProgressBar scoreRed;
    private ProgressBar scoreBlue;

    // Game Statistics
    private String opponent;
    private String AI;
    private String turn;
    private String color;
    private int boardSize;
    private int moveCount;

    // Variables for game functionality (game end, move back, etc.)
    private int[][] route;
    private int[][] playedPath;
    private boolean isEnd;

    enum CellState { EMPTY, BLUE, RED, GREEN }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startGame();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.newGameButton:
                newGameDialog();
                break;
            case R.id.backButton:
                if(getEnd()) { Toast.makeText(this, "Game is over", Toast.LENGTH_SHORT).show(); return; }
                try { moveBack(); }
                catch (IndexOutOfBoundsException e) {
                    Toast.makeText(this, "There should be at least 2 moves on the board", Toast.LENGTH_SHORT).show(); }
                catch (NullPointerException ignored) { }
                break;
            case R.id.settingsButton:
                settingsActivity();
                break;
            default:
                // getting the name of clicked cell
                String cellName = v.getResources().getResourceName(v.getId());
                String name;

                int x = 0, y;
                boolean hasPlayed = false;

                // finding clicked cell's coordinates
                for(y=1; y<10; y++) {
                    for(x=1; x<10; x++) {
                        // creating every cell's string one by one until we find a match
                        name = "cell_" + y + "_" + x;
                        // if it contains the created string name then we execute play with x and y values
                        if(cellName.contains(name)) {
                            hasPlayed = true;
                            break;
                        }
                    }
                    if(hasPlayed) break;
                }

                // execute when player makes a move and opponent is a computer
                if(hasPlayed && isEmpty(x, y) && !getEnd() && getOpponent().equals("computer")) { play(x,y); playAI(); }
                // execute when player makes a move and opponent is a player
                else if (hasPlayed && isEmpty(x, y) && !getEnd() && getOpponent().equals("player"))
                    play(x,y);
                // execute when player makes a move and game is already over
                else if (getEnd())
                    Toast.makeText(this, "Game is over", Toast.LENGTH_SHORT).show();
                // execute when player makes and invalid move. (clicking to a played cell)
                else
                    Toast.makeText(this, "This place is not empty", Toast.LENGTH_SHORT).show();

                break;
        }
    }





    // ******************** //
    //   STARTING METHODS   //
    // ******************** //

    // executes every initializing method
    public void startGame() {
        // game information
        setOpponent("computer");
        setAI("easy");
        setTurn("P1");
        setBoardSize(9);
        setMove(9);
        setEnd(false);
        setColor("red");

        // initializing
        initializeCells();
        initializeRoute();
        initializePlayedPath();
        initializeButtons();
        initializeScores();
    }

    // initializes cells and adds a listener for them
    private void initializeCells() {

        String buttonString;
        int buttonID;
        int border = getSize() + 2;

        hexCells = new Button[border][border];
        System.out.println(border);

        for (int y=1; y<border-1; y++) {
            for(int x=1; x<border-1; x++) {
                // ID of a cell (as String)
                buttonString = "cell_" + y + "_" + x;
                // ID of a cell (as int)
                buttonID = getResources().getIdentifier(buttonString, "id", getPackageName());
                // finding that cell's View and adding into cells array
                hexCells[y][x] = findViewById(buttonID);
                // adding a onClick listener for it
                hexCells[y][x].setOnClickListener(this);
                // adding a state tag
                hexCells[y][x].setTag("empty");
            }
        }
    }

    // initializes route which used for game end check
    private void initializeRoute() {
        int size    = getSize();
        int maxMove = ( (size*size) / 2 ) + 1;

        // row and column && allocating enough space to store all moves that one player makes
        route = new int[2][maxMove];
    }

    // initializes playedPath which used for some game functionality
    private void initializePlayedPath() {
        int size    = getSize();
        int maxMove = (size*size) + 1;

        // row and column && allocating enough space to store all moves that one player makes
        playedPath = new int[2][maxMove];
    }

    // gets buttons IDs' and adds listener for them
    private void initializeButtons() {
        // ID
        button_NewGame  = findViewById(R.id.newGameButton);
        button_MoveBack = findViewById(R.id.backButton);
        button_Settings = findViewById(R.id.settingsButton);

        // Listener
        button_NewGame.setOnClickListener(this);
        button_MoveBack.setOnClickListener(this);
        button_Settings.setOnClickListener(this);
    }

    // ID for progress bars
    private void initializeScores() {
        scoreRed  = findViewById(R.id.redProgressBar);
        scoreBlue = findViewById(R.id.blueProgressBar);

        // giving a max value
        scoreRed.setMax(getSize());
        scoreBlue.setMax(getSize());

        // setting their value to 0
        scoreRed.setProgress(0);
        scoreBlue.setProgress(0);
    }

    // changes board's cell sequence with board size
    private void changeBoard() {
        int size = getSize();

        // making all cells invisible and not clickable
        for(int i=1; i<=MAX_SIZE; i++) {
            for(int j=1; j<=MAX_SIZE; j++) {
                hexCells[i][j].setClickable(false);
                hexCells[i][j].setVisibility(View.GONE);
            }
        }

        // making cells visible and clickable only within the size
        for(int i=1; i<=size; i++) {
            for(int j=1; j<=size; j++) {
                hexCells[i][j].setClickable(true);
                hexCells[i][j].setVisibility(View.VISIBLE);
            }
        }


        // [Changing Location of Board]

        // getting Layout of cells'
        ConstraintLayout cellsLayout = (ConstraintLayout) findViewById(R.id.cellsLayout);

        // getting density
        float density = getApplicationContext().getResources().getDisplayMetrics().density;

        // calculating padding size
        int topPaddingBoard  = (int)( (MAX_SIZE - size) * 15 * density);
        int leftPaddingBoard = (int)( (MAX_SIZE - size) * 30 * density);

        // adding new margins to them
        cellsLayout.setPadding(leftPaddingBoard, topPaddingBoard, 0, 0);

        // Getting all information
        TextView txtOpponent   = findViewById(R.id.text_opponent);
        TextView txtSize       = findViewById(R.id.text_size);
        TextView txtDifficulty = findViewById(R.id.text_difficulty);
        TextView txtColor      = findViewById(R.id.text_color);

        // new information
        String newTxtOpponent   = "Opponent : " + getOpponent();
        String newTxtSize       = "Size : " + size + "x" + size;
        String newTxtDifficulty = "Difficulty : " + getAI();
        String newTxtColor      = "Color : " + getColor();

        // inserting new information
        txtOpponent.setText(newTxtOpponent);
        txtSize.setText(newTxtSize);
        txtDifficulty.setText(newTxtDifficulty);
        txtColor.setText(newTxtColor);
    }




    // ******************** //
    //     PLAY METHODS     //
    // ******************** //

    // plays the current color for the given coordinates
    public void play(int x, int y) {
        // changing cell color to current color
        if(getColor().equals("red")) {
            hexCells[y][x].setBackgroundResource(R.drawable.cell_red);
            hexCells[y][x].setTag("red");
            // setting score for Red
            scoreRed.setProgress(getScoreRed());
        }
        else {
            hexCells[y][x].setBackgroundResource(R.drawable.cell_blue);
            hexCells[y][x].setTag("blue");
            // setting score for Blue
            scoreBlue.setProgress(getScoreBlue());
        }

        int index = getMoveCount();
        // adding coordinates into playedPath
        playedPath[0][index] = y;
        playedPath[1][index] = x;
        // increasing move count by one
        setMove(getMoveCount() + 1);
        // checking for if there is a winning condition or not
        checkEnd();
        // changing turn
        changeTurn();
    }

    // plays the round for AI with given difficulty
    private void playAI() {
        // coordinates
        int x = 0, y = 0;

        switch(getAI()) {
            case "easy":
                AI_easy();
                break;
            case "nominal":
                AI_nominal();
                break;
            case "difficult":
                AI_difficult();
                break;
            case "master":
                AI_master();
                break;
        }
    }

    // goes for a random empty cell
    private void AI_easy() {
        // seed for rand
        Random rand = new Random();

        int randX = 0;
        int randY = 0;

        do {
            // getting random x and y values until we find a valid coordinate
            randX = rand.nextInt(boardSize) + 1;
            randY = rand.nextInt(boardSize) + 1;
        } while (!isEmpty(randX, randY));

        // playing selected coordinates
        play(randX, randY);
    }

    // goes for a straight line
    private void AI_nominal() {
        String color = getColor();

        int x, y;

        // if computer playing blue
        if(color.equals("blue")) {
            for(y = getSize(); y > 0; y--) {
                for(x = getSize(); x > 0; x--) {
                    if(getState(x,y) == CellState.BLUE) {
                        try {
                            if(isEmpty(x, y+1))  { play(x, y+1); return; }
                            else if(isEmpty(x+1, y+1))  { play(x+1, y+1); return; }
                            else if(isEmpty(x-1, y))  { play(x-1, y); return; }
                        } catch (NullPointerException ignored) { }
                    }
                }
            }
            x = findRandomColumn();
            play(x, 1);
        }
        else if(color.equals("red")) {
            for(x = getSize(); x > 0; x--) {
                for(y = getSize(); y > 0; y--) {
                    if(getState(x,y) == CellState.RED) {
                        try {
                            if(isEmpty(x+1, y)) { play(x+1, y); return; }
                            else if(isEmpty(x+1, y-1)) { play(x+1, y-1); return; }
                        } catch (NullPointerException ignored) { }
                    }
                }
            }
            y = findRandomRow();
            play(1, y);
        }

    }

    // ...
    private void AI_difficult() { }

    // ...
    private void AI_master() { }

    private int findRandomRow() {
        // seed for rand
        Random rand = new Random();

        int randY = 0;

        do {
            randY = rand.nextInt(boardSize) + 1;
        } while (!isEmpty(1, randY));

        return randY;
    }

    private int findRandomColumn() {
        // seed for rand
        Random rand = new Random();

        int randX = 0;

        do {
            randX = rand.nextInt(boardSize) + 1;
        } while (!isEmpty(randX, 1));

        return randX;
    }


    // ******************** //
    //    GETTER METHODS    //
    // ******************** //

    public int getSize() {
        return boardSize;
    }

    public String getTurn() {
        return turn;
    }

    public CellState getState(int x, int y) throws NullPointerException {

        if(x < 1 || x > getSize() || y < 1 || y > getSize())
            throw new NullPointerException();

        java.lang.String color = (java.lang.String)hexCells[y][x].getTag();

        if(color.contains("red"))   return CellState.RED;
        if(color.contains("blue"))  return CellState.BLUE;
        if(color.contains("green")) return CellState.GREEN;

        return CellState.EMPTY;
    }
    
    public boolean getEnd() { return isEnd; }

    public int getMoveCount() { return moveCount; }

    public int getScoreRed() {
        int score = 0;

        // calculating score
        for(int x=1; x<=getSize(); x++)
            for(int y=1; y<=getSize(); y++)
                if(getState(x, y) == CellState.RED || getState(x, y) == CellState.GREEN)
                {
                    score++;
                    break;
                }

        // checking if the game is ended or not
        if(score == getSize() && getEnd() && getTurn() == "P2")
            return getSize();
        else if(score == getSize() && !getEnd())
            return getSize() - 1;
        else
            return score;
    }

    public int getScoreBlue() {

        int score = 0;

        // calculating score
        for(int y=1; y<=getSize(); y++)
            for(int x=1; x<=getSize(); x++)
                if(getState(x, y) == CellState.BLUE || getState(x, y) == CellState.GREEN)
                {
                    score++;
                    break;
                }

        // checking if the game is ended or not
        if(score == getSize() && getEnd() && getTurn() == "P1")
            return getSize();
        else if(score == getSize() && !getEnd())
            return getSize() - 1;
        else
            return score;
    }

    public String getOpponent() { return opponent; }

    public String getAI() { return AI; }

    public String getColor() { return color; }




    // ******************** //
    //    SETTER METHODS    //
    // ******************** //

    public void changeTurn() {
        String turn = getTurn();
        String color = getColor();

        if(turn.equals("P1") && color.equals("red")) {
            setTurn("P2");
            setColor("blue");
        }
        else if(turn.equals("P1") && color.equals("blue")) {
            setTurn("P2");
            setColor("red");
        }
        else if(turn.equals("P2") && color.equals("red")) {
            setTurn("P1");
            setColor("blue");
        }
        else if(turn.equals("P2") && color.equals("blue")) {
            setTurn("P1");
            setColor("red");
        }
    }

    public void setTurn(String turn) { this.turn = turn; }

    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }
    
    public void setEnd(boolean end) { this.isEnd = end; }

    public void setMove(int count) { moveCount = count; }

    public void setOpponent(String opponent) { this.opponent = opponent; }

    public void setAI(String AI) { this.AI = AI; }

    public void setColor(String color) { this.color = color; }
    
    


    // ********************* //
    //   END CHECK METHODS   //
    // ********************* //

    private void checkEnd() {

        int step = 1;

        searchRoute(step);
    }

    private void searchRoute(int step) {
        int size     = getSize();
        int maxMove  = (size*size)/2;
        String turn = getTurn();

        if(isPathCompleted(step-1) && !getEnd()) {
            // converting cells which completes the route into green cells
            convertToWonCell();
            winDialog();
            setEnd(true);
        }

        else if(step == 1 && !getEnd()) {
            for(int i=1; i<= size; i++) {
                cleanRoute();
                if(getState(i, step) == CellState.BLUE && turn == "P2") {

                    route[0][step] = 1;
                    route[1][step] = i;
                    searchRoute(step+1);
                }
                else if(getState(step, i) == CellState.RED && turn == "P1") {

                    route[0][step] = i;
                    route[1][step] = 1;
                    searchRoute(step+1);
                }
            }
        }

        else if(step > 1 && step < maxMove && !getEnd()) {
            for(int i=0; i<6; i++)
                try {
                    if (isNeighborFriend(step, i))
                        searchRoute(step + 1);
                } catch (NullPointerException exception) {
                    System.err.println("NullPointerException - isNeighborFriend()");
                }
        }
    }





    // **************************** //
    //   END CHECK HELPER METHODS   //
    // **************************** //

    private boolean isPathCompleted(int step) {
        String turn = getTurn();

        final int startPoint = 1;
        final int endPoint = getSize();

        if(turn == "P1" && route[1][1] == startPoint && route[1][step] == endPoint) return true;
        if(turn == "P2" && route[0][1] == startPoint && route[0][step] == endPoint) return true;

        return false;
    }

    private boolean isNeighborFriend(int step, int neighbor) throws NullPointerException {

        if(getTurn() == "P1" && neighbor == 0 && neighbor == 2) { return false; }
        if(getTurn() == "P2" && neighbor == 0 && neighbor == 1 && neighbor == 3) { return false; }

        int[] y = new int[6];
        int[] x = new int[6];

        // neighbor locations                                    Ex: (B  5)
        y[0] = route[0][step-1] - 1; x[0] = route[1][step-1] - 1; //  A  4
        y[1] = route[0][step-1] - 1; x[1] = route[1][step-1]    ; //  A  5
        y[2] = route[0][step-1]    ; x[2] = route[1][step-1] - 1; //  B  4
        y[3] = route[0][step-1]    ; x[3] = route[1][step-1] + 1; //  B  6
        y[4] = route[0][step-1] + 1; x[4] = route[1][step-1]    ; //  C  5
        y[5] = route[0][step-1] + 1; x[5] = route[1][step-1] + 1; //  C  6


        // getting pressed cell's state
        CellState cellState = getState(route[1][step-1], route[0][step-1]);

        // selected neighbor's cell state
        CellState neighborState = getState(x[neighbor], y[neighbor]);

        // if they both in same state and neighbor is not already selected then go for this neighbor
        if (cellState == neighborState && !isOnRoute(x[neighbor], y[neighbor], step)) {
            route[0][step] = y[neighbor];
            route[1][step] = x[neighbor];
            return true;
        }

        return false;
    }

    private boolean isOnRoute(int x, int y, int step) {
        for(int i=1; i<=step; i++) {
            if(route[0][i] == y && route[1][i] == x){
                return true;
            }
        }

        return false;
    }

    private boolean isEmpty(int x, int y) { return getState(x, y) == CellState.EMPTY; }

    private void cleanRoute() {
        for(int i=0; i<2; i++)
            Arrays.fill(route[i], 0);
    }

    private void cleanPlayedPath() {
        for(int i=0; i<2; i++)
            Arrays.fill(playedPath[i], 0);
    }

    private void convertToWonCell() {

        int x, y, i = 1;

        while (route[0][i] != 0) {
            x = route[1][i]; // getting route's column values
            y = route[0][i]; // getting route's row values

            hexCells[y][x].setBackgroundResource(R.drawable.cell_won);
            hexCells[y][x].setTag("green");

            i++;
        }
    }




    // ****************** //
    //   BUTTON METHODS   //
    // ****************** //

    private void newGame() {
        int size = getSize();


        // changing cells into empty state
        for(int y=1; y<=size; y++)
            for(int x=1; x<=size; x++) {
                hexCells[y][x].setBackgroundResource(R.drawable.cell_empty);
                hexCells[y][x].setTag("empty");
            }

        // clearing route and playedPath
        cleanRoute();
        cleanPlayedPath();

        // new game.. new start
        setEnd(false);
        setMove(0);

        // resetting scores
        scoreRed.setProgress(0);
        scoreBlue.setProgress(0);

        // changing board
        changeBoard();
    }

    private void moveBack() throws IndexOutOfBoundsException, NullPointerException {
        int count = getMoveCount();

        if(count < 2)
            throw new IndexOutOfBoundsException();
        else if(getEnd()) { }

        // indexes of cells that we will remove the play
        int index1 = getMoveCount() - 1;
        int index2 = getMoveCount() - 2;
        // getting coordinates of the cells
        int y1 = playedPath[0][index1]; int x1 = playedPath[1][index1];
        int y2 = playedPath[0][index2]; int x2 = playedPath[1][index2];
        // changing their properties into initial state
        // - background -
        hexCells[y1][x1].setBackgroundResource(R.drawable.cell_empty);
        hexCells[y2][x2].setBackgroundResource(R.drawable.cell_empty);
        // - tag -
        hexCells[y1][x1].setTag("empty");
        hexCells[y2][x2].setTag("empty");
        // decreasing move count by 2
        setMove(getMoveCount() - 2);
        // scores
        scoreRed.setProgress(getScoreRed());
        scoreBlue.setProgress(getScoreBlue());
    }




    // ****************** //
    //   DIALOG METHODS   //
    // ****************** //
    private void winDialog() {

        // creating dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        java.lang.String winText;
        java.lang.String newGameText = "New Game";
        java.lang.String cancelText  = "Cancel";


        // Win Message
        if(getColor().equals("blue") && getOpponent().equals("player"))         // if PvP and color = blue
            winText = "Blue won the game!";
        else if (getColor().equals("red") && getOpponent().equals("player"))    // if PvP and color = red
            winText = "Red won the game!";
        else if (getTurn().equals("P1") && getOpponent().equals("computer"))    // if PvE and turn = P1
            winText = "Congratulations! You beat the " + getAI() + " BOT!" ;
        else
            winText = "You lost! Wanna try again?";

        // win Text
        builder.setTitle(winText);
        // positive button
        builder.setPositiveButton(newGameText, (dialog, which) -> newGame());
        // negative button
        builder.setNegativeButton(cancelText, null);

        builder.show();
    }

    private void newGameDialog() {

        if(getEnd() || getMoveCount() == 0) { newGame(); return; }

        // creating dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        java.lang.String newGameText = "Current game's data will be lost. Are you sure?";
        java.lang.String posText = "Yes";
        java.lang.String negText = "No";

        // dialog text
        builder.setTitle(newGameText);
        // positive button
        builder.setPositiveButton(posText, (dialog, which) -> newGame());
        // negative button
        builder.setNegativeButton(negText, null);

        builder.show();
    }

    private void settingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);

        intent.putExtra("AI", AI);
        intent.putExtra("size", boardSize);
        intent.putExtra("opponent", opponent);
        intent.putExtra("color", color);

        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String AI;
        String opponent;
        String color;
        int boardSize;


        if(requestCode == 1){
            if (resultCode == RESULT_OK) {
                AI        = data.getStringExtra("AI");
                opponent  = data.getStringExtra("opponent");
                color     = data.getStringExtra("color");
                boardSize = data.getIntExtra("size", 9);

                System.out.println(color);

                if(AI != null)
                    this.AI = AI;
                if(boardSize != 0)
                    this.boardSize = boardSize;
                if(opponent != null)
                    this.opponent = opponent;
                if(color != null)
                    this.color = color;

                newGame();
            }
        }
    }
}