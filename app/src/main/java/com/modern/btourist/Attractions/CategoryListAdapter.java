package com.modern.btourist.Attractions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.modern.btourist.Database.Category;
import com.modern.btourist.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryListAdapter extends ArrayAdapter<Category> {

    private Context mContext;
    private int mResource;

    public CategoryListAdapter(@NonNull Context context, int resource, @NonNull List<Category> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).getName();
        int pictureId = getItem(position).getPictureId();

        Category category = new Category(name,pictureId);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        ImageView imageView = convertView.findViewById(R.id.categoryImageView);
        TextView categoryText = convertView.findViewById(R.id.categoryTextView);

        imageView.setImageResource(pictureId);
        categoryText.setText(name);

        return convertView;
    }
}
