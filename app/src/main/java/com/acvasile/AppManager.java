package com.acvasile;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.DataOutputStream;
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
                    // Sort package by label name with no case sensitive
                    .sorted((o1, o2) -> o1.loadLabel(packageManager).toString().toLowerCase()
                            .compareTo(o2.loadLabel(packageManager).toString().toLowerCase()))
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

    /**
     * Uses a ROOT (su) process to force stop the given packages by name
     * @implSpec Method does not throw
     * @implNote Method requires ROOT
     * @param packages The packages name to be killed
     */
    public static void forceStopPackages(Iterable<String> packages)
    {
        Process rootProcess = null;
        try
        {
            rootProcess = Runtime.getRuntime().exec("su");

            try (DataOutputStream outputStream =
                         new DataOutputStream(rootProcess.getOutputStream()))
            {
                outputStream.writeBytes("adb shell\n");
                outputStream.flush();

                // Force stop every package
                for (String packageName : packages)
                {
                    outputStream.writeBytes("am force-stop " + packageName + "\n");
                    outputStream.flush();
                }

                outputStream.writeBytes("exit\n");
                outputStream.flush();
            }
        }
        catch (Exception ex)
        {
            Log.e("forceStopPackages", ex.getMessage());
        }
        finally
        {
            if (rootProcess != null)
            {
                try
                {
                    rootProcess.waitFor();
                }
                catch (Exception ex)
                {
                    Log.e("forceStopPackages",
                            "Can not wait for rootProcess: " +  ex.getMessage());
                }
            }
        }
    }
}
