package com.example.filesave;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SetCodeActivity extends AppCompatActivity {
    EditText editText;
    Button save;
    Button cancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_code);
        editText = findViewById(R.id.EditText_ChangeCode);
        save = findViewById(R.id.Button_CodeOk);
        cancel = findViewById(R.id.Button_CodeCancel);
        try {
            FileInputStream inputStream = openFileInput("canChange.txt");
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader reader1 = new BufferedReader(reader);
            String line = reader1.readLine();
            StringBuilder builder = new StringBuilder();
            while (line!=null) {
                builder.append(line).append("\r\n");
                line=reader1.readLine();
            }
            editText.setText(builder);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //监听器设置
        save.setOnClickListener(v -> {
            String code= String.valueOf(editText.getText());
            try {
                OutputStream outputStream=openFileOutput("canChange.txt",MODE_PRIVATE);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                writer.write(code);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        });
        cancel.setOnClickListener(v -> finish());
    }
}