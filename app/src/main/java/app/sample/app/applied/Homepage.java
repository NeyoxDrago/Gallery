package app.sample.app.applied;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mingle.entity.MenuEntity;
import com.mingle.sweetpick.BlurEffect;
import com.mingle.sweetpick.CustomDelegate;
import com.mingle.sweetpick.Delegate;
import com.mingle.sweetpick.Effect;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;
import com.mingle.sweetpick.ViewPagerDelegate;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Homepage extends AppCompatActivity {

    private RecyclerView mrecycler;
    private DatabaseReference mdef;
    private List<String> urls = new ArrayList<>();
    private Button b;
    private ProgressDialog progress , dialog;
    private SweetSheet msweetsheet;
    private RelativeLayout Rl;
    private adapter_for_gallery adapter;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        mrecycler = (RecyclerView) findViewById(R.id.mrecycler);

        mdef = FirebaseDatabase.getInstance().getReference();

        Rl = (RelativeLayout) findViewById(R.id.rl);

        msweetsheet = new SweetSheet(Rl);
        msweetsheet.setMenuList(R.menu.sweet);
        msweetsheet.setDelegate(new RecyclerViewDelegate(false));
        msweetsheet.setBackgroundEffect(new BlurEffect(5));
        msweetsheet.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
            @Override
            public boolean onItemClick(int position, MenuEntity menuEntity) {

                Intent a = new Intent(Homepage.this , page.class);
                a.putExtra("Name" , menuEntity.title);
                startActivity(a);
                return true;
            }
        });

        progress = new ProgressDialog(this);
        progress.setMessage("Loading......");
        progress.show();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading.....");


 load();



    }

    private void load() {
        mdef.child("gallery").child("all").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot i:dataSnapshot.getChildren()) {
                    if (!urls.contains(i.getValue())) {
                        urls.add((i.getValue()).toString());
                    }

                }
                if(adapter != null)
                    adapter.notifyDataSetChanged();
                else
                    adapter = new adapter_for_gallery(Homepage.this , urls);


                final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL);
                mrecycler.setLayoutManager(staggeredGridLayoutManager);
                mrecycler.setHasFixedSize(true);
                mrecycler.setItemViewCacheSize(20);
                mrecycler.setDrawingCacheEnabled(true);
                mrecycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
                mrecycler.setAdapter(adapter);
                progress.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                progress.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.add:
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .start(Homepage.this);

                break;

            case R.id.waytoSections:

                msweetsheet.show();
                break;

        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                final Uri a = result.getUri();
                dialog.show();
                final StorageReference mstorage = FirebaseStorage.getInstance().getReference().child(a.getLastPathSegment());
                mstorage.putFile(a).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {

                            mstorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    mdef.child("gallery").child("all").push().setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            load();
                                       dialog.dismiss();
                                        }
                                    });

                                }
                            });

                        }
                        else
                        {
                            Toast.makeText(Homepage.this, "AL HABIBI .... ", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {

        if(msweetsheet.isShow())
        {
            msweetsheet.dismiss();
        }

    }
}
