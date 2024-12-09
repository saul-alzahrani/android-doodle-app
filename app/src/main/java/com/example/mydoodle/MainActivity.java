package com.example.mydoodle;


import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView imageView;
    private DrawingView drawingView;
    private SeekBar brightnessSeekBar;
    private SeekBar brushSizeSeekBar;
    private SeekBar opacitySeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("MyDoodle");

        imageView = findViewById(R.id.image_view);
        drawingView = findViewById(R.id.drawing_view);
        brightnessSeekBar = findViewById(R.id.brightness_seekbar);
        brushSizeSeekBar = findViewById(R.id.brush_size_seekbar);
        opacitySeekBar = findViewById(R.id.brush_opacity_seekbar);

        Button addImageButton = findViewById(R.id.add_image_button);
        Button clearButton = findViewById(R.id.clear_button);
        Button chooseColorButton = findViewById(R.id.choose_color_button);
        Button saveImageButton = findViewById(R.id.save_image_button);

        // Add Image Button: Opens the gallery to pick a picture
        addImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        // Clear Drawing Button
        clearButton.setOnClickListener(v -> drawingView.clearCanvas());

        // Choose Color Button
        chooseColorButton.setOnClickListener(v -> showColorPickerDialog());

        // Save Image Button
        saveImageButton.setOnClickListener(v -> saveImage());

        // Brightness Adjustment SeekBar
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float brightness = (progress - 100) / 100f; // Normalize between -1.0 and 1.0
                setBrightness(brightness);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Brush Size Adjustment SeekBar
        brushSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setBrushSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Brush Opacity Adjustment SeekBar
        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setOpacity(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Handle the result from the image picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Adjust the brightness of the selected image
    private void setBrightness(float brightness) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(new float[]{
                1, 0, 0, 0, brightness * 255,
                0, 1, 0, 0, brightness * 255,
                0, 0, 1, 0, brightness * 255,
                0, 0, 0, 1, 0
        });
        imageView.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    }

    // Save the image with drawings
    private void saveImage() {
        Bitmap resultBitmap = combineImageAndDrawing();
        try {
            Uri savedImageUri = saveBitmapToGallery(resultBitmap);
            if (savedImageUri != null) {
                Toast.makeText(this, "Image saved to gallery!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image.", Toast.LENGTH_SHORT).show();
        }
    }

    // Combine the ImageView and DrawingView into a single Bitmap
    private Bitmap combineImageAndDrawing() {
        Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        imageView.draw(canvas);
        drawingView.draw(canvas);
        return bitmap;
    }

    // Save the combined Bitmap to the gallery
    private Uri saveBitmapToGallery(Bitmap bitmap) {
        Uri imageCollection;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "EditedImage_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.WIDTH, bitmap.getWidth());
        values.put(MediaStore.Images.Media.HEIGHT, bitmap.getHeight());

        try {
            Uri imageUri = getContentResolver().insert(imageCollection, values);
            if (imageUri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                }
                return imageUri;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Show Color Picker Dialog
    private void showColorPickerDialog() {
        String[] colors = {"Red", "Green", "Blue", "Black", "Yellow", "Purple", "Orange"};
        int[] colorValues = {
                android.graphics.Color.RED,
                android.graphics.Color.GREEN,
                android.graphics.Color.BLUE,
                android.graphics.Color.BLACK,
                android.graphics.Color.YELLOW,
                android.graphics.Color.MAGENTA,
                android.graphics.Color.rgb(255, 165, 0)
        };

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Choose Brush Color");
        builder.setItems(colors, (dialog, which) -> {
            drawingView.setColor(colorValues[which]);
            dialog.dismiss();
        });
        builder.show();
    }
}
