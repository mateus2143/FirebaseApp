package com.mateusbrandao.firebaseapp.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mateusbrandao.firebaseapp.NavigationActivity;
import com.mateusbrandao.firebaseapp.R;
import com.mateusbrandao.firebaseapp.model.Upload;
import com.mateusbrandao.firebaseapp.util.LoadingDialog;

import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class StorageFragment extends Fragment {
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Button btnUpload, btnGaleria;
    private ImageView imageView;
    private Uri imageUri = null;
    private EditText editNome;
    //Referencia para im nó RealtimeDB
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference("uploads");

    public StorageFragment() {

        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_storage, container, false);
        btnGaleria = layout.findViewById(R.id.storage_btn_galeria);
        editNome = layout.findViewById(R.id.storage_edit_nome);
        btnUpload = layout.findViewById(R.id.storage_btn_upload);
        imageView = layout.findViewById(R.id.storage_image_cel);
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_storage, container, false);
        btnUpload.setOnClickListener(v -> {
            if (editNome.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), "Digite um nome para imagem", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                uploadImagemUri();
            } else {
                uploadImagemByte();
            }
        });

        btnGaleria.setOnClickListener(v -> {
            Intent intent = new Intent();
            //intent implicita -> pegar um arquivo do celular
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            // inicia uma Activity, e espera o retorno(foto)
            startActivityForResult(intent, 1);
        });
        return layout;

    }


    private void uploadImagemUri() {

        LoadingDialog dialog = new LoadingDialog(getActivity(), R.layout.custom_dialog);
        dialog.StartLoadingDialog();

        String tipo = getFileExtension(imageUri);
        //criar uma referencia para o arquivo no firebase
        Date d = new Date();
        String nome = editNome.getText().toString() + toString();

        // criando referencia da imagem no Storage
        StorageReference imagemREF = storage.getReference().child("imagens/" + nome + "-" + d.getTime() + "." + tipo);

        imagemREF.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(getActivity(), "Upload feito com sucesso", Toast.LENGTH_SHORT).show();

                    /* inserir dados da imagem no RealtimeDatabase */

                    //pegar a URL da imagem
                    taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // inserir no database
                                //criando referencia(database) do upload
                                DatabaseReference refUpload = database.push();
                                String id = refUpload.getKey();

                                Upload upload = new Upload(id, nome, uri.toString());
                                //salvando upload no DB
                                refUpload.setValue(upload)
                                        .addOnSuccessListener(aVoid -> {
                                            dialog.dismissDialog();
                                            Toast.makeText(getActivity(), "Upload  sucesso!", Toast.LENGTH_SHORT).show();

                                            NavController navcontroller = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);

                                            //voltar para a fragment inicial
                                            navcontroller.navigateUp();
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });

    }

    //retorna o tipo (.pmg, .jpg) da imagem
    private String getFileExtension(Uri imageUri) {
        ContentResolver cr = getActivity().getContentResolver();
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(cr.getType(imageUri));
    }

    //Resultado do startActivityResult()
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("RESULT", "requestCode" + requestCode + ",resultCode" + resultCode);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            //caso o usuario selecionou uma imagem da galeria

            //endereco da imagem selecionada
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    public byte[] convertImage2Byte(ImageView imageView) {
        //converter ImageView ->byte[]
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        //objeto baos -> armazenar a imagem convertida
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    //fazer um upload de uma imagem convertida p/bytes
    public void uploadImagemByte() {
        byte[] data = convertImage2Byte(imageView);

        //criar uma referencia para uma imagem no storage
        StorageReference imagemRef = storage.getReference().child("imagems/01.jpeg");
        //Realizar upload da imagem
        imagemRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(getActivity(), "Upload feito com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.i("UPLOAD", "Sucesso");
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });

    }
}