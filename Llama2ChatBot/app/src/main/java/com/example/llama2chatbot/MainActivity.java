package com.example.llama2chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, windowInsets) -> {
            Insets padding = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(padding.left, padding.top, padding.right, padding.bottom);
            return windowInsets;
        });
        EditText userInputField = findViewById(R.id.username_input);
        findViewById(R.id.go_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                chatIntent.putExtra("username", userInputField.getText().toString());
                startActivity(chatIntent);
            }
        });
    }
}