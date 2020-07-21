package de.kai_morich.simple_usb_terminal;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.di.oximeter.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HisteryResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    public List<OldHisteryModel> listArticles = new ArrayList<>();

    OnHisteryItemClickListener onArticleItemClickListener;

    public HisteryResultAdapter(Context c, OnHisteryItemClickListener listener) {

        this.context = c;
        this.onArticleItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result_detail_view_activity, parent, false);
        return new CustomViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {

        try {

            OldHisteryModel oldHisteryModel = listArticles.get(position);

            CustomViewHolder customViewHolder = (CustomViewHolder) holder;

            customViewHolder.tv_heartpulse.setVisibility(View.VISIBLE);
            customViewHolder.tv_oxygen.setVisibility(View.VISIBLE);
            customViewHolder.tv_heartpulse.setText(oldHisteryModel.getHeartpulse()+" Bpm");
            customViewHolder.tv_oxygen.setText(oldHisteryModel.getOxygen()+" %");


            customViewHolder.tv_range_data.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onArticleItemClickListener.onHisteryItemClickListener(oldHisteryModel);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listArticles.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {


        public TextView tv_heartpulse;
        public TextView tv_oxygen;
        public TextView tv_range_data;


        public CustomViewHolder(View view) {
            super(view);
            tv_heartpulse = (TextView) view.findViewById(R.id.tv_heartpulse);
            tv_oxygen = (TextView) view.findViewById(R.id.tv_oxygen);
            tv_range_data = (TextView) view.findViewById(R.id.tv_range_data);
        }
    }

    public void addAll(List<OldHisteryModel> listdata) {

        try {

            if (listArticles != null) {
                listArticles.clear();
                listArticles.addAll(listdata);

            } else {
                this.listArticles = listdata;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();

    }
}