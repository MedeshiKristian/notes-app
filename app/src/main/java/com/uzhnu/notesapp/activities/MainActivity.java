package com.uzhnu.notesapp.activities;

import androidx.appcompat.app.*;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.uzhnu.notesapp.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private MaterialToolbar toolBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ListView listView;
    private ListView listView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolBar = findViewById(R.id.topAppBar);
        navigationView = findViewById(R.id.nav_view);
        toolBar.setTitle("Dynamic title");
//        setSupportActionBar(toolBar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolBar, R.string.navigation_open, R.string.navigation_close);

        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        toolBar.setNavigationOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        ArrayList<String> categories = new ArrayList<>();
        categories.add("Personal");
        categories.add("Work");
        categories.add("Life");
        categories.add("Travel");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, categories);
        listView = findViewById(R.id.list_slidermenu);
        listView.setAdapter(adapter);
        listView2 = findViewById(R.id.list_slidermenu2);
        listView2.setAdapter(adapter);

    }
}