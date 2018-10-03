package app.sample.app.applied;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class page extends AppCompatActivity {

    private RecyclerView mrecycler;
    private List<String> urls = new ArrayList<>();
    private DatabaseReference mdef;
    private String a ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        a = getIntent().getStringExtra("Name");
        RelativeLayout rl = findViewById(R.id.pagerl);

        getSupportActionBar().setHomeButtonEnabled(true);

        mrecycler = (RecyclerView) findViewById(R.id.pagerecycler);
        mdef = FirebaseDatabase.getInstance().getReference();

        mrecycler.setLayoutManager(new LinearLayoutManager(this));
        mdef.child("gallery").child(a.toLowerCase()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot i : dataSnapshot.getChildren())
                {
                    if(!urls.contains(i.getValue().toString())){
                        urls.add(i.getValue().toString());
                    }
                }

                adapter_for_gallery adapter = new adapter_for_gallery(page.this , urls);
                mrecycler.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mdef.child("gallery").child(a.toLowerCase()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                urls.clear();
                for(DataSnapshot i : dataSnapshot.getChildren())
                {
                    if(!urls.contains(i.getValue().toString())){
                        urls.add(i.getValue().toString());
                    }
                }

                adapter_for_gallery adapter = new adapter_for_gallery(page.this , urls);
                mrecycler.setAdapter(adapter);


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mrecycler.setHasFixedSize(true);
        mrecycler.setItemViewCacheSize(20);
        mrecycler.setDrawingCacheEnabled(true);
        mrecycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add2,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.add2:
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .start(page.this);

                break;
        }
        return true;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final ProgressDialog progress = new ProgressDialog(page.this);
        progress.setMessage("Uploading Image....");
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                progress.show();
                final Uri uri = result.getUri();
                final StorageReference mstorage = FirebaseStorage.getInstance().getReference().child(uri.getLastPathSegment());
                mstorage.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {

                            mstorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {

                                    mdef.child("gallery").child(a.toLowerCase()).push().setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mdef.child("gallery").child("all").push().setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progress.dismiss();
                                                }
                                            });

                                        }
                                    });

                                }
                            });

                        }
                        else
                        {
                            Toast.makeText(page.this, "AL HABIBI .... ", Toast.LENGTH_SHORT).show();

                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }


}
