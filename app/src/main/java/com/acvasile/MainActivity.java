package com.acvasile;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

import java.util.List;


public class MainActivity extends AppCompatActivity
{
    static final String PREF_FILE = "pref_file";
    static final String PREF_VALUE = "pref_value";

    private ItemAdapter itemAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemAdapter = new ItemAdapter(getApplicationContext(),
                AppManager.getInstalledApps(getApplicationContext()));

        ListView recyclerView = findViewById(R.id.list_view);
        recyclerView.setFastScrollEnabled(true);
//        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),1));
        recyclerView.setAdapter(itemAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view ->
        {
            itemAdapter.forceStopSelectedPackages();
            Snackbar.make(view, "Killed selected apps", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (itemAdapter != null) { itemAdapter.saveCurrentState(); }
    }
}
