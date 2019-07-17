package com.modern.btourist.Guides;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.modern.btourist.Database.Btourist;
import com.modern.btourist.Database.Category;
import com.modern.btourist.Database.StorageUtil;
import com.modern.btourist.Database.User;
import com.modern.btourist.Map.GlideApp;
import com.modern.btourist.R;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference col = db.collection("users");
    private ListenerRegistration registration;

    private Context mContext;
    private int mResource;

    public UserAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String email = getItem(position).getEmail();
        String firstName = getItem(position).getFirstName();
        String lastName = getItem(position).getLastName();
        String fullName = getItem(position).getFullName();
        String pictureId = getItem(position).getProfilePicturePath();
        String language1 = getItem(position).getLanguage1();
        String language2 = getItem(position).getLanguage2();
        String sex = getItem(position).getSex();
        int age = getItem(position).getAge();
        long phone = getItem(position).getPhone();
        double latitude = getItem(position).getLatitude();
        double longitude = getItem(position).getLongitude();
        String interest1 = getItem(position).getInterest1();
        String interest2 = getItem(position).getInterest2();
        String interest3 = getItem(position).getInterest3();
        ArrayList<String> arrayList = new ArrayList<>();

        User user = new User(firstName,lastName,email,phone,interest1,interest2,interest3,age,sex,language1,language2,pictureId,latitude,longitude,fullName,arrayList);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        ImageView imageView = convertView.findViewById(R.id.userImageView);
        TextView nameText = convertView.findViewById(R.id.fullNameText);
        TextView languageText1 = convertView.findViewById(R.id.languageText1);
        TextView languageText2 = convertView.findViewById(R.id.languageText2);
        TextView sexText = convertView.findViewById(R.id.nameText);
        TextView ageText = convertView.findViewById(R.id.ageTextView);
        TextView phoneText = convertView.findViewById(R.id.phoneText);
        TextView interestText1 =convertView.findViewById(R.id.interestText1);
        TextView interestText2 =convertView.findViewById(R.id.interestText2);
        TextView interestText3 =convertView.findViewById(R.id.interestText3);


        Query query = col.whereEqualTo("fullName", fullName);
        ListenerRegistration registration = query.addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException ex) {
                        if(ex!=null){
                            Log.e("AdapterListener","Snapshot listen failed",ex);
                        }
                        List<User> user = queryDocumentSnapshots.toObjects(User.class);
                        for(User u : user){
                            String path = u.getProfilePicturePath();
                            StorageReference ref = StorageUtil.INSTANCE.pathToReference(path);

                            try {
                                File localFile;
                                localFile = File.createTempFile("Images", "jpeg");
                                ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inSampleSize = 8;
                                        Bitmap myImage = BitmapFactory.decodeFile(localFile.getAbsolutePath(),options);
                                        GlideApp.with(Btourist.Companion.getInstance()).load(myImage).placeholder(R.drawable.people).into(imageView);
                                    }
                                }) ;

                            } catch (IOException e) {
                                e.printStackTrace();
                            }



                        }
                    }
                    // ...
                });


        nameText.setText(fullName);
        languageText1.setText(language1);
        languageText2.setText(language2);
        sexText.setText(sex);
        ageText.setText(String.valueOf(age));
        phoneText.setText(String.valueOf(phone));
        interestText1.setText(interest1);
        interestText2.setText(interest2);
        interestText3.setText(interest3);


        return convertView;
       // registration.remove();
    }
}
