package com.acvasile;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppManager
{
    private static final boolean IGNORE_FAILS = false;
    private static boolean root = false;

    public static List<ApplicationInfo> getInstalledApps(PackageManager packageManager)
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

    /**
     * Creates a SU process and force stops the given package and waits until the
     * SU process terminates
     * @param packageName The package to be killed
     * @return True if operation succeeded
     */
    private static boolean forceStopPackageRoot(String packageName)
    {
        Process rootProcess = null;
        // Verbose su failure
        try
        {
            rootProcess = Runtime.getRuntime().exec("su");
        }
        catch (IOException e)
        {
            Log.e("forceStopPackageRoot", "Can not run 'su' (no root): " + e.getMessage());
            return false;
        }

        boolean ret = true;
        try (DataOutputStream outputStream = new DataOutputStream(rootProcess.getOutputStream()))
        {
            outputStream.writeBytes("adb shell\n");
            outputStream.flush();

            outputStream.writeBytes("am force-stop" + packageName + "\n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
        }
        catch (IOException e)
        {
            Log.e("forceStopPackageRoot", "Failure while writing commands: " + e.getMessage());
            ret = false;
        }

        try
        {
            rootProcess.waitFor();
        }
        catch (InterruptedException e)
        {
            Log.e("forceStopPackageRoot", "Can not wait for rootProcess: " + e.getMessage());
            ret = false;
        }
        return ret;
    }


    public static void forceStopPackages(Iterable<String> packages)
    {
        Process rootProcess = null;
        // Verbose su failure
        try
        {
            rootProcess = Runtime.getRuntime().exec("su");
        }
        catch (IOException e)
        {
            Log.e("forceStopPackageRoot",
                    "Can not run 'su' (no root): " + e.getMessage());
            return;
        }

        DataOutputStream outputStream = null;
        try
        {
            outputStream = new DataOutputStream(rootProcess.getOutputStream());
            // Obtain the adb shell
            outputStream.writeBytes("adb shell\n");
            outputStream.flush();

            for (String packageName : packages)
            {
                // Ignore failures
                try
                {
                    outputStream.writeBytes("am force-stop" + packageName + "\n");
                    outputStream.flush();
                }
                catch (Exception e)
                {
                    Log.e("forceStopPackages",
                            "Can not force stop '" + packageName + "':" + e.getMessage());
                }
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();
        }
        catch (Exception e)
        {
            Log.e("forceStopPackages",
                    "Failed obtaining the adb shell / exit:" + e.getMessage());
        }
        finally
        {
            if (outputStream != null)
            {
                try
                {
                    outputStream.close();
                }
                catch (IOException e)
                {
                    Log.e("forceStopPackages", "Can not close stream:" + e.getMessage());
                }
            }
        }

        try
        {
            rootProcess.waitFor();
        }
        catch (Exception e)
        {
            Log.e("forceStopPackages", "Can not wait for su:" + e.getMessage());
        }
    }
}
