package com.tagliaro.monclin.urca;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends ArrayAdapter<Cours> {
    private int resource;

    public ListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public ListAdapter(@NonNull Context context, int resource, @NonNull List<Cours> classes) {
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

        Cours c = getItem(position);

        if(c != null) {
            TextView className = v.findViewById(R.id.className);
            TextView time = v.findViewById(R.id.time);
            TextView location = v.findViewById(R.id.location);

            if(className != null) {
                className.setText(c.getNomCours());
            }

            if(time != null) {
                time.setText(c.getHeureDebut() + " - " + c.getHeureFin());
            }

            if(location != null) {
                location.setText(c.getSalle());
            }

            v.setTag(c.getId());
        }

        return v;
    }
}
