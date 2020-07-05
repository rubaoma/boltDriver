package com.rubdev.uber.helper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rubdev.uber.activity.PassageiroActivity;
import com.rubdev.uber.activity.RequisicoesActivity;
import com.rubdev.uber.config.ConfiguracaoFirebase;
import com.rubdev.uber.model.Usuario;

public class UsuarioFirebase {

    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return usuario.getCurrentUser();
    }

    public static Usuario getDadosUsuarioLogado(){
        FirebaseUser firebaseUser = getUsuarioAtual();

        Usuario usuario = new Usuario();
        usuario.setId( firebaseUser.getUid() );
        usuario.setEmail( firebaseUser.getEmail() );
        usuario.setNome( firebaseUser.getDisplayName() );

        return usuario;
    }

    public static boolean atualizarNomeUsuario(String nome) {

        try {
            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();
            user.updateProfile( profile ).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if ( !task.isSuccessful()){
                        Log.d("Perfil","Erro ao atualizar nome de perfil");
                    }
                }
            });

            return true;

        }catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }
    }

    public static void redirecionarUsuarioLogado(final Activity activity){

        FirebaseUser user = getUsuarioAtual();
        if ( user !=null ){
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("usuarios")
                    .child( getIdentificadorUsuario());

            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Usuario usuario = dataSnapshot.getValue(Usuario.class);

                    String tipoUsuario = usuario.getTipo();
                    if ( tipoUsuario.equals("M")){
                        activity.startActivity(new Intent(activity, RequisicoesActivity.class));
                    }else {
                        activity.startActivity(new Intent(activity, PassageiroActivity.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }



    }

    public static String getIdentificadorUsuario(){
        return getUsuarioAtual().getUid();
    }
}
