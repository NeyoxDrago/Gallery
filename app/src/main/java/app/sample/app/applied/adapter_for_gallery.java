package app.sample.app.applied;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class adapter_for_gallery extends RecyclerView.Adapter<adapter_for_gallery.ViewHolder> {

    private Context context;
    private List urls = new ArrayList<>();

    public adapter_for_gallery(Context context, List<String> urls) {
        this.context = context;
        this.urls = urls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v= LayoutInflater.from(context).inflate(R.layout.gallery_item,viewGroup,false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        final RequestOptions options = new RequestOptions().placeholder(R.drawable.ic_launcher_background);

        Glide.with(context).load(urls.get(i)).apply(options).into(viewHolder.image);

        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View v) {

                Dialog a = new Dialog(context);
                a.setContentView(R.layout.item);

                final ImageView imageview = a.findViewById(R.id.itemimage);

                Glide.with(context).load(urls.get(i)).apply(options).into(imageview);

                a.show();
            }
        });


    }


    @Override
    public int getItemCount() {
        return urls.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.galleryimage);

        }
    }

}
