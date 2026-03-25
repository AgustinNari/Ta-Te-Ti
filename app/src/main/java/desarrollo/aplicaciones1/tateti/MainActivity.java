package desarrollo.aplicaciones1.tateti;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etName;
    private RadioGroup rgSymbol;
    private RadioGroup rgDifficulty;
    private Button btnStart;

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_SYMBOL = "extra_symbol";
    public static final String EXTRA_DIFFICULTY = "extra_difficulty";

    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_HARD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        rgSymbol = findViewById(R.id.rgSymbol);
        rgDifficulty = findViewById(R.id.rgDifficulty);
        btnStart = findViewById(R.id.btnStart);

        setupToggleableRadioGroup(rgSymbol, R.id.rbCross, R.id.rbCircle);
        setupToggleableRadioGroup(rgDifficulty, R.id.rbEasy, R.id.rbHard);

        btnStart.setOnClickListener(v -> startGame());
    }

    private void setupToggleableRadioGroup(RadioGroup group, int... radioButtonIds) {
        for (int id : radioButtonIds) {
            RadioButton button = findViewById(id);

            button.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP && button.isChecked()) {
                    group.clearCheck();
                    return true;
                }
                return false;
            });
        }
    }

    private void startGame() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            name = "Extraño";
        }

        char playerSymbol = 'X'; // default
        int checkedSymbolId = rgSymbol.getCheckedRadioButtonId();

        if (checkedSymbolId == R.id.rbCircle) {
            playerSymbol = 'O';
        } else if (checkedSymbolId == R.id.rbCross) {
            playerSymbol = 'X';
        }

        int difficulty = DIFFICULTY_HARD; // default
        int checkedDifficultyId = rgDifficulty.getCheckedRadioButtonId();

        if (checkedDifficultyId == R.id.rbEasy) {
            difficulty = DIFFICULTY_EASY;
        } else if (checkedDifficultyId == R.id.rbHard) {
            difficulty = DIFFICULTY_HARD;
        }

        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_SYMBOL, String.valueOf(playerSymbol));
        intent.putExtra(EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
    }
}