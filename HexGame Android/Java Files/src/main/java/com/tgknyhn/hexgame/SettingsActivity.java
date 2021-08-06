package com.tgknyhn.hexgame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    // Radio Groups
    private RadioGroup rg_AI;
    private RadioGroup rg_boardSize;
    private RadioGroup rg_opponent;
    private RadioGroup rg_color;
    // Save Button
    private Button saveButton;
    // Game Information
    private int boardSize;
    private String AI;
    private String opponent;
    private String color;
    // flags
    private boolean isSizeChecked;
    private boolean isAIChecked;
    private boolean isOpponentChecked;
    private boolean isColorChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Checked
        isSizeChecked = false;
        isAIChecked   = false;
        isOpponentChecked = false;
        isColorChecked = false;

        // Intent
        Intent intent = getIntent();

        // Radio Groups
        rg_AI        = findViewById(R.id.radioGroup_AI_difficulty);
        rg_boardSize = findViewById(R.id.radioGroup_boardSize);
        rg_opponent  = findViewById(R.id.radioGroup_opponent);
        rg_color     = findViewById(R.id.radioGroup_color);

        // Button
        saveButton = findViewById(R.id.save_button);

        // Listeners
        rg_AI.setOnCheckedChangeListener(this);
        rg_boardSize.setOnCheckedChangeListener(this);
        rg_opponent.setOnCheckedChangeListener(this);
        rg_color.setOnCheckedChangeListener(this);

        saveButton.setOnClickListener(this);

        // Checking Buttons
        checkAI(intent.getStringExtra("AI"));
        checkSize(intent.getIntExtra("size", 9));
        checkOpponent(intent.getStringExtra("opponent"));
        checkColor(intent.getStringExtra("color"));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.AI_easy:
                isAIChecked = true;
                AI = "easy";
                break;
            case R.id.AI_nominal:
                isAIChecked = true;
                AI = "nominal";
                break;
            case R.id.AI_difficult:
                isAIChecked = true;
                AI = "difficult";
                break;
            case R.id.AI_master:
                isAIChecked = true;
                AI = "master";
                break;
            case R.id.rb_5x5:
                isSizeChecked = true;
                boardSize = 5;
                break;
            case R.id.rb_6x6:
                isSizeChecked = true;
                boardSize = 6;
                break;
            case R.id.rb_7x7:
                isSizeChecked = true;
                boardSize = 7;
                break;
            case R.id.rb_8x8:
                isSizeChecked = true;
                boardSize = 8;
                break;
            case R.id.rb_9x9:
                isSizeChecked = true;
                boardSize = 9;
                break;
            case R.id.rb_pvp:
                isOpponentChecked = true;
                opponent = "player";
                break;
            case R.id.rb_pve:
                isOpponentChecked = true;
                opponent = "computer";
                break;
            case R.id.rb_red:
                isColorChecked = true;
                color = "red";
                break;
            case R.id.rb_blue:
                isColorChecked = true;
                color = "blue";
                break;
        }
    }

    @Override
    public void onClick(View v) {
        // Result Intent
        Intent result = new Intent();

        if(v.getId() == R.id.save_button) {
            if(isSizeChecked)
                result.putExtra("size", boardSize);
            if(isOpponentChecked)
                result.putExtra("opponent", opponent);
            if(isAIChecked)
                result.putExtra("AI", AI);
            if(isColorChecked)
                result.putExtra("color", color);

            setResult(RESULT_OK, result);
        }
        else
            setResult(RESULT_CANCELED);

        finish();
    }

    private void checkAI(String AI) {
        switch (AI) {
            case "easy":
                rg_AI.check(R.id.AI_easy);
                break;
            case "nominal":
                rg_AI.check(R.id.AI_nominal);
                break;
            case "difficult":
                rg_AI.check(R.id.AI_difficult);
                break;
            case "master":
                rg_AI.check(R.id.AI_master);
                break;
        }
    }

    private void checkSize(int size) {
        switch (size) {
            case 5:
                rg_boardSize.check(R.id.rb_5x5);
                break;
            case 6:
                rg_boardSize.check(R.id.rb_6x6);
                break;
            case 7:
                rg_boardSize.check(R.id.rb_7x7);
                break;
            case 8:
                rg_boardSize.check(R.id.rb_8x8);
                break;
            case 9:
                rg_boardSize.check(R.id.rb_9x9);
                break;
        }
    }

    private void checkOpponent(String opponent) {
        switch (opponent) {
            case "computer":
                rg_opponent.check(R.id.rb_pve);
                break;
            case "player":
                rg_opponent.check(R.id.rb_pvp);
                break;
        }
    }

    private void checkColor(String color) {
        switch (color) {
            case "red":
                rg_color.check(R.id.rb_red);
                break;
            case "blue":
                rg_color.check(R.id.rb_blue);
                break;
        }
    }
}