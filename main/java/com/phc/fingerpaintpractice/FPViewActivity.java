package com.phc.fingerpaintpractice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nex3z.fingerpaintview.FingerPaintView;

import java.io.IOException;

public class FPViewActivity extends AppCompatActivity {

    private final String TAG = "PHC";
    private StringBuilder inputBuilder = new StringBuilder();
    private DigitClassifier digitClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fpview);

        FingerPaintView fpv = findViewById(R.id.fpv_paint);
        Button btnClear = findViewById(R.id.button_clear);
        Button btnDetect = findViewById(R.id.button_detect);
        Button btnPlus = findViewById(R.id.button_plus);
        Button btnMinus = findViewById(R.id.button_minus);
        ImageButton clearLogBtn = findViewById(R.id.button_clear_log);
        Button btnCalculate = findViewById(R.id.button_calculate);
        TextView textLog = findViewById(R.id.text_log);
        RadioGroup radioGroup = findViewById(R.id.radioGroupColors);
        RadioButton radioRed = findViewById(R.id.radio_red);
        RadioButton radioBlue = findViewById(R.id.radio_blue);
        RadioButton radioGreen = findViewById(R.id.radio_green);

        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        fpv.setPen(p);

        try {
            digitClassifier = new DigitClassifier(getAssets(), "mnist2.tflite");
            digitClassifier.initialize();
        } catch (IOException e) {
            Log.e("DigitClassifier", "Failed to initialize the classifier", e);
        }

        radioRed.setOnClickListener(v -> {
            p.setColor(Color.RED);
            p.setStrokeWidth(28);
            fpv.setPen(p);
            radioGroup.clearCheck();
        });

        radioBlue.setOnClickListener(v -> {
            p.setColor(Color.BLUE);
            p.setStrokeWidth(36);
            fpv.setPen(p);
            radioGroup.clearCheck();
        });

        radioGreen.setOnClickListener(v -> {
            p.setColor(Color.GREEN);
            p.setStrokeWidth(48);
            fpv.setPen(p);
            radioGroup.clearCheck();
        });

        btnClear.setOnClickListener(view -> {
            fpv.clear();
            inputBuilder.setLength(0);
            textLog.setText("Draw a digit...");
        });

        clearLogBtn.setOnClickListener(v -> {
            inputBuilder.setLength(0);
            textLog.setText("");
        });

        btnDetect.setOnClickListener(v -> {
            Bitmap bitmap = fpv.exportToBitmap(fpv.getWidth(), fpv.getHeight());
            int digit = digitClassifier.classify(bitmap);
            inputBuilder.append(digit);
            textLog.setText(inputBuilder.toString());
            fpv.clear();
        });

        btnPlus.setOnClickListener(v -> {
            inputBuilder.append("+");
            textLog.setText(inputBuilder.toString());
        });

        btnMinus.setOnClickListener(v -> {
            inputBuilder.append("-");
            textLog.setText(inputBuilder.toString());
        });

        btnCalculate.setOnClickListener(v -> {
            try {
                int result = evaluateExpression(inputBuilder.toString());
                textLog.setText(inputBuilder.toString() + " = " + result);
                inputBuilder.setLength(0);

                MediaPlayer mediaPlayer = MediaPlayer.create(FPViewActivity.this, R.raw.yay);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> mp.release());

            } catch (Exception e) {
                textLog.setText("Error");
            }
        });
    }

    private int evaluateExpression(String expression) {
        String[] tokens = expression.split("(?=[+-])|(?<=[+-])");
        int result = Integer.parseInt(tokens[0]);
        for (int i = 1; i < tokens.length; i += 2) {
            String op = tokens[i];
            int num = Integer.parseInt(tokens[i + 1]);
            if (op.equals("+")) {
                result += num;
            } else if (op.equals("-")) {
                result -= num;
            }
        }
        return result;
    }
} // END