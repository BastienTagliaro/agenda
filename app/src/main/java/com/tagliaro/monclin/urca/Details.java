package com.tagliaro.monclin.urca;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if(cours != null) {
            toolbar.setTitle(cours.getNomCours());

            String desc = cours.getDescription();
            System.out.println(desc);
            Pattern descriptionPattern = Pattern.compile("\\[(\\w)\\] (.*)", Pattern.MULTILINE);

            Matcher m = descriptionPattern.matcher(desc);

            if(m.matches()) {
                System.out.println(m.group(1) + " " + m.group(2));
            }
            else
                System.out.println("No results");

            TextView startTime = findViewById(R.id.startTime);
            startTime.setText(cours.getHeureDebut());

            TextView endTime = findViewById(R.id.endTime);
            endTime.setText(cours.getHeureFin());

            TextView description = findViewById(R.id.description);
            description.setText(cours.getDescription());

        }

        setSupportActionBar(toolbar);
    }

}
