package com.acvasile;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder>
{
    static class ItemViewHolder extends RecyclerView.ViewHolder
    {
        TextView primaryTitle;
        TextView secondaryTitle;
        ImageView itemIcon;
        CardView cardView;

        ItemViewHolder(View itemView)
        {
            super(itemView);

            primaryTitle = itemView.findViewById(R.id.primary_title_id);
            secondaryTitle = itemView.findViewById(R.id.secondary_title_id);
            itemIcon = itemView.findViewById(R.id.item_icon);
            cardView = itemView.findViewById(R.id.cardview_id);
        }
    }

    private class HolderOnClick implements View.OnClickListener
    {
        private Context context;
        private ApplicationInfo applicationInfo;


        HolderOnClick(Context context, ApplicationInfo applicationInfo)
        {
            this.context = context;
            this.applicationInfo = applicationInfo;
        }

        @Override
        public void onClick(View v)
        {
            Log.i("ITEM_ADAPTER", "onClickListener");

            ActivityManager activityManager = (ActivityManager)context.getSystemService(Activity.ACTIVITY_SERVICE);
            // Failed to obtain the activity manager
            if (activityManager == null) { return; }

            try
            {
                activityManager.killBackgroundProcesses(applicationInfo.packageName);
            }
            catch (Exception ex)
            {
                Log.e("HolderOnClock", "Could not kill packageName: " + applicationInfo.packageName);
                Log.e("HolderOnClock", "Kill failed with: " + ex.getMessage());

                Toast.makeText(context, "Failed to kill: " + applicationInfo.packageName,
                        Toast.LENGTH_LONG).show();
                return;
            }

            Log.e("HolderOnClock", "Killed: " + applicationInfo.packageName);
            Toast.makeText(context, "killed: " + applicationInfo.packageName,
                    Toast.LENGTH_LONG).show();
        }
    }

    private Context context;
    private PackageManager packageManager;
    private List<ApplicationInfo> data;

    ItemAdapter(Context mContext, List<ApplicationInfo> mData)
    {
        this.context = mContext;
        this.data = mData;
        this.packageManager = context.getPackageManager();
    }

    @Override
    @NonNull
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater mInflater = LayoutInflater.from(context);
        View view = mInflater.inflate(R.layout.cardview_present_app, parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position)
    {
        position = holder.getAdapterPosition();

        ApplicationInfo applicationInfo;
        try
        {
            applicationInfo = data.get(position);
        }
        catch (IndexOutOfBoundsException ex)
        {
            Log.e("ItemAdaptor", "Can not obtain element at position: " + position);
            return;
        }

        holder.primaryTitle.setText(applicationInfo.loadLabel(packageManager));
        holder.secondaryTitle.setText(applicationInfo.loadDescription(packageManager));
        holder.itemIcon.setImageDrawable(applicationInfo.loadIcon(packageManager));

        holder.cardView.setOnClickListener(new HolderOnClick(context, applicationInfo));
    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }
}
