package com.example.mydoodle;


import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private Button chooseColorButton, clearButton, clearAreaButton;
    private SeekBar brushSizeSeekBar, brushOpacitySeekBar;
    private int currentColor = Color.BLACK; // Default color

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize drawing view and tools
        drawingView = findViewById(R.id.drawing_view);
        chooseColorButton = findViewById(R.id.choose_color_button);
        clearButton = findViewById(R.id.clear_button);
        clearAreaButton = findViewById(R.id.clear_area_button);
        brushSizeSeekBar = findViewById(R.id.brush_size_seekbar);
        brushOpacitySeekBar = findViewById(R.id.brush_opacity_seekbar);

        // Set listener for Choose Color button
        chooseColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog();
            }
        });

        // Set listener for the clear button
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clearCanvas();
            }
        });

        // Set listener for the clear area button
        clearAreaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.startClearingArea();
            }
        });

        // Set listener for brush size SeekBar
        brushSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setBrushSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Set listener for brush opacity SeekBar
        brushOpacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setOpacity(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    // Method to show a color picker dialog
    private void showColorPickerDialog() {
        final String[] colorNames = {"Black", "Red", "Green", "Blue", "Yellow", "Cyan", "Magenta"};
        final int[] colors = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a color");
        builder.setItems(colorNames, (dialog, which) -> {
            currentColor = colors[which];
            drawingView.setColor(currentColor);
        });
        builder.create().show();
    }
}
