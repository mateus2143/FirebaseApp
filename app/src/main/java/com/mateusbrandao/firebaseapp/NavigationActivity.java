package com.mateusbrandao.firebaseapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.mateusbrandao.firebaseapp.util.NotificationService;

public class NavigationActivity extends AppCompatActivity {
    private ImageView btnMenu;
    private DrawerLayout drawerLayout;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        btnMenu = findViewById(R.id.navigation_icon);
        drawerLayout = findViewById(R.id.nav_drawerLayout);

        btnMenu.setOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.START);


            NavigationView navigationView = findViewById(R.id.navigationView);
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_nome)).setText(auth.getCurrentUser().getDisplayName());
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_email)).setText(auth.getCurrentUser().getEmail());

            //evento Logout
            navigationView.getMenu().findItem(R.id.nav_menu_logout).setOnMenuItemClickListener(item -> {
                auth.signOut();
                finish();
                return false;
            });

            //Recuperar o navController -> realizs troca de fragments
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

            // juntar navController com navView(Menu)
            NavigationUI.setupWithNavController(navigationView, navController);
            //Criar um serviço
            Intent service = new Intent(getApplicationContext(), NotificationService.class);
            getApplicationContext().startService(service);

        });

    }

}

