package com.gmail.specifikarma.optimumklus.UI.card;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.gmail.specifikarma.optimumklus.R;
import com.gmail.specifikarma.optimumklus.networking.Settings;


public class CardAdapter extends RecyclerView.Adapter<CardAdapter.PagerViewHolder> {


    private LayoutInflater mInflater;
    private CardAdapter.ItemClickListener mClickListener;
    private Settings settings;
    private Context context;

    private boolean isCreated;

    public CardAdapter(Context context, Settings settings) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.settings = settings;
    }

    public class PagerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

        TextView title01;
        ImageView card01;

        public PagerViewHolder(@NonNull View itemView) {
            super(itemView);

            card01 = itemView.findViewById(R.id.main_image);
            title01 = itemView.findViewById(R.id.main_text);
            card01.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    @NonNull
    @Override
    public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = mInflater.inflate(R.layout.service_li, viewGroup, false);

        return new PagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {

        Drawable work;
        String desc;
        switch (position) {
            case 0:
                work = context.getDrawable(R.drawable.images_cover_design_attic);
                desc = context.getString(R.string.design);
                break;
            case 1:
                work = context.getDrawable(R.drawable.images_cover_landscaping_trees);
                desc = context.getString(R.string.garden);
                break;
            case 2:
                work = context.getDrawable(R.drawable.images_cover_install_floor);
                desc = context.getString(R.string.install);
                break;
            case 3:
                work = context.getDrawable(R.drawable.images_cover_wall_paint);
                desc = context.getString(R.string.paint);
                break;
            case 4:
                work = context.getDrawable(R.drawable.images_cover_placing_appliance);
                desc = context.getString(R.string.place);
                break;
            case 5:
                work = context.getDrawable(R.drawable.images_cover_repair_tile);
                desc = context.getString(R.string.repair);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + position);
        }

            Glide.with(holder.card01.getContext())
                    .load(work)
                    .into(holder.card01);

            holder.title01.setText(desc);
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setClickListener(CardAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
