package com.tagliaro.monclin.urca.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tagliaro.monclin.urca.R;
import com.tagliaro.monclin.urca.utils.Classes;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassesListAdapter extends ArrayAdapter<Classes> {
    private int resource;

    public ClassesListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public ClassesListAdapter(@NonNull Context context, int resource, @NonNull List<Classes> classes) {
        super(context, resource, classes);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if(v == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            v = vi.inflate(resource, null);
        }

        Classes c = getItem(position);

        if(c != null) {
            TextView className = v.findViewById(R.id.className);
            TextView time = v.findViewById(R.id.time);
            TextView location = v.findViewById(R.id.location);

            if(className != null) {
                className.setText(c.getClassname());
            }

            if(time != null) {
                time.setText(String.format(getContext().getString(R.string.class_time), c.getStartTime(), c.getEndTime()));
            }

            if(location != null) {
                Pattern classroomPattern = Pattern.compile("\\[(.*?)\\] (.*?)");
                Matcher m = classroomPattern.matcher(c.getClassroom());

                if(m.matches()) {
                    String classroom = m.group(2);
                    classroom = classroom.substring(0,1).toUpperCase() + classroom.substring(1);

                    location.setText(classroom);
                }
                else
                    location.setText(c.getClassroom());
            }

            v.setTag(c.getId());
        }

        return v;
    }
}
