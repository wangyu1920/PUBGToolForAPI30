package com.example.filesave;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
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

        //监听器设置
        save.setOnClickListener(v -> {
            String code= String.valueOf(editText.getText());
            try {
                OutputStream outputStream=openFileOutput("UserCustom.ini",MODE_PRIVATE);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                writer.write(code);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        });
        cancel.setOnClickListener(v -> {
            finish();
        });
    }
}