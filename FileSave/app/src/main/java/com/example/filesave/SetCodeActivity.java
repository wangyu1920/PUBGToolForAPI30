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

import SetCode.SetCode;

public class SetCodeActivity extends AppCompatActivity {
    EditText editText;
    EditText editText2;
    Button save;
    Button cancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_code);
        editText = findViewById(R.id.EditText_ChangeCode);
        editText2 = findViewById(R.id.EditText_Profile);
        save = findViewById(R.id.Button_CodeOk);
        cancel = findViewById(R.id.Button_CodeCancel);
        FileInputStream inputStream;
        InputStreamReader reader;
        BufferedReader reader1 = null;
        String line;
        try {
            inputStream = openFileInput(SetCode.canChangeFile);
            reader = new InputStreamReader(inputStream);
            reader1 = new BufferedReader(reader);
            line = reader1.readLine();
            StringBuilder builder = new StringBuilder();
            while (line!=null) {
                builder.append(line).append("\r\n");
                line=reader1.readLine();
            }
            editText.setText(builder);


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (reader1 != null) {
                try {
                    reader1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            inputStream = openFileInput(SetCode.ProfileFile);
            reader = new InputStreamReader(inputStream);
            reader1 = new BufferedReader(reader);
            line = reader1.readLine();
            StringBuilder builder = new StringBuilder();
            while (line!=null) {
                builder.append(line).append("\r\n");
                line=reader1.readLine();
            }
            editText2.setText(builder);


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (reader1 != null) {
                try {
                    reader1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //监听器设置
        save.setOnClickListener(v -> {
            String code= String.valueOf(editText.getText());
            try {
                OutputStream outputStream=openFileOutput(SetCode.canChangeFile,MODE_PRIVATE);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                writer.write(code);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String code2= String.valueOf(editText2.getText());
            try {
                OutputStream outputStream=openFileOutput(SetCode.ProfileFile,MODE_PRIVATE);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                writer.write(code2);
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