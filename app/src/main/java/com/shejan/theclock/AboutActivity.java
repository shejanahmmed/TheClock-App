package com.shejan.theclock;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set status bar color to match background
        getWindow().setStatusBarColor(getResources().getColor(R.color.black, getTheme()));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ImageView githubIcon = findViewById(R.id.icon_github);
        ImageView instagramIcon = findViewById(R.id.icon_instagram);
        ImageView linkedinIcon = findViewById(R.id.icon_linkedin);

        githubIcon.setOnClickListener(v -> openUrl("https://github.com/shejanahmmed"));
        instagramIcon.setOnClickListener(v -> openUrl("https://www.instagram.com/iamshejan/"));
        linkedinIcon.setOnClickListener(v -> openUrl("https://www.linkedin.com/in/farjan-ahmmed/"));
    }

    private void openUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
