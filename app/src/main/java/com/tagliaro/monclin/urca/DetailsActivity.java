package com.tagliaro.monclin.urca;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        Long id = intent.getLongExtra("id", 0);

        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        Cours cours = databaseHandler.getCours(id);

        if(cours != null) {
            setTitle(cours.getNomCours());

            String desc = cours.getDescription();
            String[] results = desc.split("\n");

            Pattern descriptionPattern = Pattern.compile("\\[(.*?)\\] (.*?)");

            Map<String, String> classData = new HashMap<>();

            for(String res : results) {
                Matcher m = descriptionPattern.matcher(res);

                if(m.matches()) {
                    if(m.group(1).equals("salle") && m.group(2).toLowerCase().contains("salle")) {
                        String[] split = m.group(2).split(" ");
                        String[] newClassData = new String[split.length-1];
                        System.arraycopy(split, 1, newClassData, 0, split.length-1);
                        String result = TextUtils.join(" ", newClassData);

                        classData.put("salle", result);
                    }
                    else {
                        classData.put(m.group(1), m.group(2));
                    }
                }
            }

            TextView startTime = findViewById(R.id.startTime);
            startTime.setText(cours.getHeureDebut());

            TextView endTime = findViewById(R.id.endTime);
            endTime.setText(cours.getHeureFin());

            TextView classroom = findViewById(R.id.classroom);
            classroom.setText(classData.get("salle"));

            TextView group = findViewById(R.id.group);
            group.setText(classData.get("groupe"));

            TextView teacher = findViewById(R.id.teacher);
            teacher.setText(classData.get("enseignant"));
        }
    }

}
