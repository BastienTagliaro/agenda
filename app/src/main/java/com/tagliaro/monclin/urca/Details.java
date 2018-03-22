package com.tagliaro.monclin.urca;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class Details extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        Long id = intent.getLongExtra("id", 0);

        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        Cours cours = databaseHandler.getCours(id);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(cours.getNomCours());
        setSupportActionBar(toolbar);

        TextView test = findViewById(R.id.test);
        test.setText(cours.getDescription());

    }

}
