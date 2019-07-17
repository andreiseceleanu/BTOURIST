package com.modern.btourist.Guides;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.firestore.*;
import com.modern.btourist.Database.Attraction;
import com.modern.btourist.Database.Btourist;
import com.modern.btourist.Map.MainActivity;
import com.modern.btourist.R;

import java.util.List;

public class AttractionAdapter extends ArrayAdapter<Attraction> {

    public int getImage(String imageName){

        Context app = Btourist.Companion.getInstance();

        int drawableResourceId = app.getResources().getIdentifier(imageName, "drawable", app.getPackageName());

        return drawableResourceId;
    }

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference col = db.collection("users");
    private ListenerRegistration registration;

    private Context mContext;
    private int mResource;

    public AttractionAdapter(@NonNull Context context, int resource, @NonNull List<Attraction> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String image = getItem(position).getImage();
        String name = getItem(position).getName();
        String category = getItem(position).getCategory();

        Attraction attraction = new Attraction(category,"",0.0,0.0,name,0L,"",image);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        ImageView imageView = convertView.findViewById(R.id.attractionImage);
        TextView nameText = convertView.findViewById(R.id.nameTextView);
        TextView categoryTextView = convertView.findViewById(R.id.categoryTextView);

        Query query = col.whereEqualTo("name", name);
        ListenerRegistration registration = query.addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException ex) {
                        if (ex != null) {
                            Log.e("AdapterListener", "Snapshot listen failed", ex);
                        }
                        List<Attraction> attractions = queryDocumentSnapshots.toObjects(Attraction.class);
                        for (Attraction u : attractions) {
                            String image = u.getImage();
                            int resource = getImage(image);
                            imageView.setImageResource(resource);
                        }
                    }
                    // ...
                });


        nameText.setText(name);
        categoryTextView.setText(category);
        int resource = getImage(image);
        imageView.setImageResource(resource);

        return convertView;
        // registration.remove();
    }

}

