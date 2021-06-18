package com.mateusbrandao.firebaseapp;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.storage.FirebaseStorage;

public class StorageActivity extends AppCompatActivity {
    private FirebaseStorage storage;
    private Button btnUpload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        btnUpload = findViewById(R.id.storage_btn_upload);
        btnUpload.setOnClickListener(v ->{
            Uri uri = Uri.parse("https://github.com/clemiltonfucapi/DispMoveisII-2021-02/blob/main/Firebase%20Storage/celular.jpg")
            storage.getReference().putFile(uri);
        });
    }
}
