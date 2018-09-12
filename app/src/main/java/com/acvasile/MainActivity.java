package com.acvasile;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;


public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ItemAdapter itemAdapter = new ItemAdapter(getApplicationContext(),
                AppManager.getInstalledApps(getApplicationContext()));

        RecyclerView recyclerView = findViewById(R.id.recyclerview_id);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),1));
        recyclerView.setAdapter(itemAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view ->
        {
            itemAdapter.forceStopSelectedPackages();
            Snackbar.make(view, "Killed selected apps", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });
    }
}
