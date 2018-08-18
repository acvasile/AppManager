package com.acvasile;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppManager
{
    private static final boolean IGNORE_FAILS = false;

    private static List<ApplicationInfo> getInstalledApps(PackageManager packageManager)
    {
        List<ApplicationInfo> packageList =
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        if (IGNORE_FAILS)
        {
            return packageList;
        }

        // Introduce streams from v24
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            return packageList.stream()
                    .filter(item -> packageManager.getLaunchIntentForPackage(item.packageName) != null)
                    .collect(Collectors.toList());
        }

        ArrayList<ApplicationInfo> apps = new ArrayList<>();
        // Must obtain the list from meta data with every call
        for (ApplicationInfo applicationInfo :
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA))
        {
            // Does not throw an exception since 4.0
            if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null)
            {
                apps.add(applicationInfo);
            }
        }

        return apps;
    }


    public static List<ApplicationInfo> getInstalledApps(Context context)
    {
        return getInstalledApps(context.getPackageManager());
    }
}
