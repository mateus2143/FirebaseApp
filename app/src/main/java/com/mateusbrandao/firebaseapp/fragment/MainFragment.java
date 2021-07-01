package com.mateusbrandao.firebaseapp.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mateusbrandao.firebaseapp.R;
import com.mateusbrandao.firebaseapp.adapter.UserAdapter;
import com.mateusbrandao.firebaseapp.model.User;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private RecyclerView recyclercontatos;
    private UserAdapter userAdapter;
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference requestref = FirebaseDatabase.getInstance().getReference("requests");
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private User userLogged;

    private ArrayList<User> listaContatos = new ArrayList<>();

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_main, container, false);
        userLogged = new User(auth.getCurrentUser().getUid(),
                             auth.getCurrentUser().getEmail(),
                             auth.getCurrentUser().getDisplayName());

        recyclercontatos = layout.findViewById(R.id.frag_main_recycler_user);

        userAdapter = new UserAdapter(getContext(),listaContatos);
        userAdapter.setListener(new UserAdapter.ClickAdapterUser() {
            @Override
            public void adicionarContato(int position) {
                User u = listaContatos.get(position);
                requestref.child(userLogged.getId()).child("send").setValue(u.getId());
                //request receive
                requestref.child(u.getId()).child("receive").child(userLogged.getId()).setValue(userLogged);
                //tirar o usuario solicitado
                listaContatos.get(position).setReceiveRequest(true);
                userAdapter.notifyDataSetChanged();
            }
        });

        recyclercontatos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclercontatos.setAdapter(userAdapter);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserDatabase();
    }

    public void getUserDatabase(){
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaContatos.clear();

                for (DataSnapshot filho: snapshot.getChildren()){
                    User u = filho.getValue(User.class);
                    // comparar com usuario Logafo
                    if(userLogged.equals(u)){
                       /* if (cont%2==0){
                            u.setReceiveRequest(true);
                        }else {
                            u.setReceiveRequest(false);
                        }*/
                        listaContatos.add(u);

                    }

                }

                // Verificar quais contatos ja foram solitados
                requestref.child(userLogged.getId()).child("send").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot no_filho : snapshot.getChildren()){
                            User usuarioSolicitado = no_filho.getValue(User.class);
                            for(int i=0; i<listaContatos.size(); i++){
                                if (listaContatos.get(i).equals(usuarioSolicitado)){
                                    listaContatos.get(i).setReceiveRequest(true);
                                }
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
