package com.shejan.theclock;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_screen_saver) {
            android.widget.Toast.makeText(this, "Screen Saver clicked", android.widget.Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_settings) {
            android.widget.Toast.makeText(this, "Settings clicked", android.widget.Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_help) {
            android.widget.Toast.makeText(this, "Help clicked", android.widget.Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
