package com.acvasile;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder>
{
    static class ItemViewHolder extends RecyclerView.ViewHolder
    {
        TextView primaryTitle;
        TextView secondaryTitle;
        ImageView itemIcon;
        SwitchCompat switchCompat;
        CardView cardView;

        ItemViewHolder(View itemView)
        {
            super(itemView);

            primaryTitle = itemView.findViewById(R.id.primary_title_id);
            secondaryTitle = itemView.findViewById(R.id.secondary_title_id);
            itemIcon = itemView.findViewById(R.id.item_icon);
            switchCompat = itemView.findViewById(R.id.on_off_switch);
            cardView = itemView.findViewById(R.id.cardview_id);
        }
    }

    private class InternalData
    {
        ApplicationInfo applicationInfo;
        boolean active;

        InternalData(ApplicationInfo applicationInfo, boolean active)
        {
            this.applicationInfo = applicationInfo;
            this.active = active;
        }
    }

    private Context context;
    private PackageManager packageManager;
    private List<InternalData> data;

    ItemAdapter(Context mContext, List<ApplicationInfo> mData)
    {
        this.context = mContext;

        data = new ArrayList<>(mData.size());
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            mData.forEach(applicationInfo ->
                    data.add(new InternalData(applicationInfo, false)));
        }
        else
        {
            for (ApplicationInfo applicationInfo : mData)
            {
                data.add(new InternalData(applicationInfo, false));
            }
        }

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

        InternalData internalData;
        try
        {
            internalData = data.get(position);
        }
        catch (IndexOutOfBoundsException ex)
        {
            Log.e("ItemAdaptor", "Can not obtain element at position: " + position);
            return;
        }

        holder.primaryTitle.setText(internalData.applicationInfo.loadLabel(packageManager));
        holder.secondaryTitle.setText(internalData.applicationInfo.loadDescription(packageManager));
        holder.itemIcon.setImageDrawable(internalData.applicationInfo.loadIcon(packageManager));

        holder.switchCompat.setChecked(internalData.active);
        holder.switchCompat.setOnClickListener(view -> internalData.active = !internalData.active);

        holder.cardView.setOnClickListener(view ->
        {
            AppManager.forceStopPackages(
                    new ArrayList<>(Arrays.asList(internalData.applicationInfo.packageName)));

            Log.e("HolderOnClock", "Killed: " + internalData.applicationInfo.packageName);
            Toast.makeText(context, "killed: " + internalData.applicationInfo.packageName,
                    Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }
}
