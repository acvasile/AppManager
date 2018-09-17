package com.acvasile;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static android.content.Context.MODE_PRIVATE;


public class ItemAdapter extends BaseAdapter implements SectionIndexer
{
    static class ItemViewHolder
    {
        TextView primaryTitle;
        TextView secondaryTitle;
        ImageView itemIcon;
        SwitchCompat switchCompat;

        ItemViewHolder(View itemView)
        {
            primaryTitle = itemView.findViewById(R.id.primary_title_id);
            secondaryTitle = itemView.findViewById(R.id.secondary_title_id);
            itemIcon = itemView.findViewById(R.id.item_icon);
            switchCompat = itemView.findViewById(R.id.on_off_switch);
        }
    }

    private class InternalData
    {
        ApplicationInfo applicationInfo;
        String indexSelection;
        boolean active;

        InternalData(ApplicationInfo applicationInfo, boolean active)
        {
            this.applicationInfo = applicationInfo;
            this.active = active;

            if (this.applicationInfo.packageName.length() > 0)
            {
                this.indexSelection = this.applicationInfo
                        .packageName.substring(0, 1);
            }
            else
            {
                this.indexSelection = "";
            }
        }
    }

    private Context context;
    private PackageManager packageManager;
    private List<InternalData> data;
    private HashMap<String, Integer> alphaIndexer;

    public void forceStopSelectedPackages()
    {
        List<String> packages = new ArrayList<>(data.size());
        for (InternalData internalData : data)
        {
            if (internalData.active) { packages.add(internalData.applicationInfo.packageName); }
        }
        AppManager.forceStopPackages(packages);
    }

    private Set<String> serializeInternalData()
    {
        Set<String> internalDataSet = new HashSet<>();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            internalDataSet.addAll(data.stream()
                    .filter(internalData -> internalData.active)
                    .map(elem -> elem.applicationInfo.packageName)
                    .collect(Collectors.toList()));
        }
        else
        {
            for (InternalData internalData : data)
            {
                if (internalData.active)
                {
                    internalDataSet.add(internalData.applicationInfo.packageName);
                }
            }
        }

        return internalDataSet;
    }

    public void saveCurrentState()
    {
        Set<String> internalDataSet = serializeInternalData();
        // Nothing to be saved
        if (internalDataSet.size() == 0) { return; }

        // NOTE: SharedPreferences are lost if the app is force stopped
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(MainActivity.PREF_VALUE, internalDataSet);
        editor.apply();
    }

    private void restoreLastState()
    {
        SharedPreferences settings =
                context.getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE);

        Set<String> internalDataSet;
        try
        {
            internalDataSet = settings.getStringSet(MainActivity.PREF_VALUE, null);
        }
        catch (ClassCastException ex)
        {
            Log.e("restoreLastState", "Could not resume state: " + ex.getMessage());
            return;
        }

        // Set is empty
        if (internalDataSet == null || internalDataSet.size() == 0) { return; }

        // Restore the last state
        for (InternalData internalData : data)
        {
            try
            {
                internalData.active =
                        internalDataSet.contains(internalData.applicationInfo.packageName);
            }
            catch (ClassCastException ex)
            {
                Log.e("restoreLastState", "internalDataSet: " + ex.getMessage());
                return;
            }
        }
    }

    ItemAdapter(Context mContext, List<ApplicationInfo> mData)
    {
        this.context = mContext;

        this.alphaIndexer = new HashMap<>();

        this.data = new ArrayList<>(mData.size());
        int index = 0;
        for (ApplicationInfo applicationInfo : mData)
        {
            InternalData internalData = new InternalData(applicationInfo, false);
            this.data.add(new InternalData(applicationInfo, false));
            if (!this.alphaIndexer.containsKey(internalData.indexSelection))
            {
                this.alphaIndexer.put(internalData.indexSelection, index);
            }
            ++index;
        }

        this.packageManager = context.getPackageManager();
        restoreLastState();
    }

    @Override
    public int getCount()
    {
        return data.size();
    }

    @Override
    public Object getItem(int position)
    {
        return data.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ItemViewHolder holder;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(context).inflate(R.layout.present_app,
                    parent, false);
            holder = new ItemViewHolder(convertView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ItemViewHolder) convertView.getTag();
        }

        InternalData internalData;
        try
        {
            internalData = data.get(position);
        }
        catch (IndexOutOfBoundsException ex)
        {
            Log.e("ItemAdaptor", "Can not obtain element at position: " + position);
            return null;
        }
        holder.primaryTitle.setText(internalData.applicationInfo.loadLabel(packageManager));
        holder.secondaryTitle.setText(internalData.applicationInfo.loadDescription(packageManager));
        holder.itemIcon.setImageDrawable(internalData.applicationInfo.loadIcon(packageManager));

        holder.switchCompat.setChecked(internalData.active);
        holder.switchCompat.setOnClickListener(view -> internalData.active = !internalData.active);
        return convertView;
    }

    @Override
    public Object[] getSections()
    {
        return data.toArray();
    }

    @Override
    public int getPositionForSection(int sectionIndex)
    {
        return alphaIndexer.get(data.get(sectionIndex).indexSelection);
    }

    @Override
    public int getSectionForPosition(int position)
    {
        return 0;
    }
}
