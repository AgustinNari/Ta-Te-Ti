package desarrollo.aplicaciones1.tateti;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private TextView tvInfo;
    private TextView tvStatus;

    private Button[] cells = new Button[9];
    private Button btnRestart;
    private Button btnBack;

    private char[] board = new char[9];

    private char playerSymbol = 'X';
    private char aiSymbol = 'O';
    private String playerName = "Extraño";

    private boolean gameOver = false;
    private boolean playerTurn = true;

    private int difficulty = MainActivity.DIFFICULTY_HARD;

    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvInfo = findViewById(R.id.tvInfo);
        tvStatus = findViewById(R.id.tvStatus);
        btnRestart = findViewById(R.id.btnRestart);
        btnBack = findViewById(R.id.btnBack);

        readDataFromIntent();
        bindBoardButtons();

        btnRestart.setOnClickListener(v -> resetGame());
        btnBack.setOnClickListener(v -> finish());

        resetGame();
    }

    private void readDataFromIntent() {
        Intent intent = getIntent();

        String name = intent.getStringExtra(MainActivity.EXTRA_NAME);
        if (name == null || name.trim().isEmpty()) {
            playerName = "Extraño";
        } else {
            playerName = name.trim();
        }

        String symbol = intent.getStringExtra(MainActivity.EXTRA_SYMBOL);
        if ("O".equals(symbol)) {
            playerSymbol = 'O';
        } else {
            playerSymbol = 'X';
        }

        aiSymbol = (playerSymbol == 'X') ? 'O' : 'X';

        difficulty = intent.getIntExtra(MainActivity.EXTRA_DIFFICULTY, MainActivity.DIFFICULTY_HARD);

        String difficultyText = (difficulty == MainActivity.DIFFICULTY_EASY) ? "Fácil" : "Difícil";
        tvInfo.setText(playerName + " juega con " + playerSymbol + " | Máquina: " + difficultyText);
    }

    private void bindBoardButtons() {
        cells[0] = findViewById(R.id.btn0);
        cells[1] = findViewById(R.id.btn1);
        cells[2] = findViewById(R.id.btn2);
        cells[3] = findViewById(R.id.btn3);
        cells[4] = findViewById(R.id.btn4);
        cells[5] = findViewById(R.id.btn5);
        cells[6] = findViewById(R.id.btn6);
        cells[7] = findViewById(R.id.btn7);
        cells[8] = findViewById(R.id.btn8);

        for (int i = 0; i < 9; i++) {
            final int index = i;
            cells[i].setOnClickListener(v -> onCellClicked(index));
        }
    }

    private void resetGame() {
        Arrays.fill(board, ' ');
        gameOver = false;

        for (Button cell : cells) {
            cell.setText("");
            cell.setEnabled(true);
        }

        playerTurn = random.nextBoolean();

        if (playerTurn) {
            tvStatus.setText("Arranca " + playerName + " con " + playerSymbol);
        } else {
            tvStatus.setText("Arranca la máquina con " + aiSymbol);
            tvStatus.post(() -> {
                if (gameOver) return;

                aiTurnWithDelay();

//                if (!checkGameEnd()) {
//                    playerTurn = true;
//                    tvStatus.setText("Tu turno");
//                }
            });
        }
    }

    private void onCellClicked(int index) {
        if (gameOver || !playerTurn) return;
        if (board[index] != ' ') return;

        makeMove(index, playerSymbol);

        if (checkGameEnd()) {
            return;
        }

        playerTurn = false;
        tvStatus.setText("Turno de la máquina con " + aiSymbol);

        aiTurnWithDelay();

//        if (!checkGameEnd()) {
//            playerTurn = true;
//            tvStatus.setText("Tu turno");
//        }
    }

    private void makeMove(int index, char symbol) {
        board[index] = symbol;
        cells[index].setText(String.valueOf(symbol));
        cells[index].setEnabled(false);
    }

    private void aiTurn() {
        int move;
        int bestMove;
        boolean aiMistake = false;

        if (difficulty == MainActivity.DIFFICULTY_EASY) {
            move = findRandomMove();
        } else {
            int variation = random.nextInt(100);
            if (variation >= 10){
                move = findBestMove();}
            else{
                bestMove = findBestMove();
                move = findRandomMove();
                if (countAvailableMoves() > 1){
                    while (move == bestMove) {
                        move = findRandomMove();
                    }
                    aiMistake = true;
                }
            }
        }

        if (move != -1) {
            makeMove(move, aiSymbol);
        }

        if (aiMistake) {
            Toast.makeText(this, "La máquina parece haberse confundido 🤖, ¡Aprovechalo!", Toast.LENGTH_SHORT).show();
        }
    }

    private void aiTurnWithDelay(){
        tvStatus.postDelayed(() -> {
            if (gameOver) return;

            aiTurn();

            if (!checkGameEnd()){
                playerTurn = true;
                tvStatus.setText("Tu turno con " + playerSymbol);
            }

        },1000);
    }

    private int countAvailableMoves() {
        int count = 0;
        for (char c : board) {
            if (c == ' ') count++;
        }
        return count;
    }

    private int findRandomMove() {
        List<Integer> availableMoves = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            if (board[i] == ' ') {
                availableMoves.add(i);
            }
        }

        if (availableMoves.isEmpty()) {
            return -1;
        }

        return availableMoves.get(random.nextInt(availableMoves.size()));
    }

    private int findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = -1;

        for (int i = 0; i < 9; i++) {
            if (board[i] == ' ') {
                board[i] = aiSymbol;
                int score = minimax(0, false);
                board[i] = ' ';

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = i;
                }
            }
        }

        return bestMove;
    }

    private int minimax(int depth, boolean isMaximizing) {
        if (hasWon(aiSymbol)) {
            return 10 - depth;
        }

        if (hasWon(playerSymbol)) {
            return depth - 10;
        }

        if (isBoardFull()) {
            return 0;
        }

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;

            for (int i = 0; i < 9; i++) {
                if (board[i] == ' ') {
                    board[i] = aiSymbol;
                    int score = minimax(depth + 1, false);
                    board[i] = ' ';
                    bestScore = Math.max(bestScore, score);
                }
            }

            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;

            for (int i = 0; i < 9; i++) {
                if (board[i] == ' ') {
                    board[i] = playerSymbol;
                    int score = minimax(depth + 1, true);
                    board[i] = ' ';
                    bestScore = Math.min(bestScore, score);
                }
            }

            return bestScore;
        }
    }

    private boolean checkGameEnd() {
        if (hasWon(playerSymbol)) {
            endGame(playerName + " ganó");
            return true;
        }

        if (hasWon(aiSymbol)) {
            endGame("Ganó la máquina");
            return true;
        }

        if (isBoardFull()) {
            endGame("El juego terminó en empate");
            return true;
        }

        return false;
    }

    private void endGame(String message) {
        gameOver = true;
        tvStatus.setText(message);

        for (Button cell : cells) {
            cell.setEnabled(false);
        }
    }

    private boolean hasWon(char symbol) {
        return (board[0] == symbol && board[1] == symbol && board[2] == symbol) ||
                (board[3] == symbol && board[4] == symbol && board[5] == symbol) ||
                (board[6] == symbol && board[7] == symbol && board[8] == symbol) ||
                (board[0] == symbol && board[3] == symbol && board[6] == symbol) ||
                (board[1] == symbol && board[4] == symbol && board[7] == symbol) ||
                (board[2] == symbol && board[5] == symbol && board[8] == symbol) ||
                (board[0] == symbol && board[4] == symbol && board[8] == symbol) ||
                (board[2] == symbol && board[4] == symbol && board[6] == symbol);
    }

    private boolean isBoardFull() {
        for (char c : board) {
            if (c == ' ') {
                return false;
            }
        }
        return true;
    }
}