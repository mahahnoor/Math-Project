package com.phc.fingerpaintpractice;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class DigitClassifier {
    private final Interpreter interpreter;

    public DigitClassifier(AssetManager assetManager, String modelPath) throws IOException {
        interpreter = new Interpreter(loadModelFile(assetManager, modelPath));
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public int classify(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 28, 28, true);
        float[][][][] input = new float[1][28][28][1];

        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 28; j++) {
                int pixel = resized.getPixel(j, i);
                float normalized = (Color.red(pixel) / 255.0f);
                input[0][i][j][0] = 1.0f - normalized;  
            }
        }

        float[][] output = new float[1][10];
        interpreter.run(input, output);

        int maxIdx = 0;
        float maxProb = 0;
        for (int i = 0; i < 10; ++i) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    public void close() {
        interpreter.close();
    }

    public void initialize() {
    }
}

