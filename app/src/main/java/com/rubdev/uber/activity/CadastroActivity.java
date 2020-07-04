package com.rubdev.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.rubdev.uber.R;
import com.rubdev.uber.config.ConfiguracaoFirebase;
import com.rubdev.uber.helper.UsuarioFirebase;
import com.rubdev.uber.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText camponome, campoEmail, campoSenha;
    private Switch swithTipoUsuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);


        camponome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        swithTipoUsuario = findViewById(R.id.switchTipoUsuario);

    }

    public void validarCadastroUsuario(View view){
        // recuperar textos dos campos

        String textonome = camponome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if ( !textonome.isEmpty()){ // verificar campo nome
            if ( !textoEmail.isEmpty()){ // verificar campo e-mail
                if ( !textoSenha.isEmpty()){ // verificar campo senha

                    Usuario usuario = new Usuario();
                    usuario.setNome( textonome );
                    usuario.setEmail( textoEmail );
                    usuario.setSenha( textoSenha );
                    //validou o usuario
                    usuario.setTipo( verificaTipoUsuario() );

                    //cadastrar usuario
                    cadastrarUsuario( usuario );



                }else {
                    Toast.makeText(CadastroActivity.this,
                            "Preencha a senha!",
                            Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(CadastroActivity.this,
                        "Preencha o E-mail!",
                        Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(CadastroActivity.this,
                    "Preencha o nome!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public  void cadastrarUsuario(final Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    try {
                        //recuperar o id do usuario
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId( idUsuario );
                        usuario.salvar();

                        // Atualizar nome do UserProfile
                        UsuarioFirebase.atualizarNomeUsuario( usuario.getNome());

                        //redireciona o usuário com base no seu tipo
                        // se o usuário for passageiro irá chamar a activity maps
                        // se não chama a activity requisições

                        if ( verificaTipoUsuario() == "P"){
                            startActivity(new Intent(CadastroActivity.this, PassageiroActivity.class));
                            finish();
                            Toast.makeText(CadastroActivity.this,
                                    "Sucesso ao cadastrar o passageiro!",
                                    Toast.LENGTH_SHORT).show();


                        }else {
                            startActivity(new Intent(CadastroActivity.this, RequisicoesActivity.class ));
                            finish();
                            Toast.makeText(CadastroActivity.this,
                                    "Sucesso ao cadastrar o motorista!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }catch ( Exception e) {
                        e.printStackTrace();
                    }


                }else{

                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch ( FirebaseAuthWeakPasswordException e ){
                        excecao = "Digite uma senha mais forte!";
                    }catch ( FirebaseAuthInvalidCredentialsException e ){
                        excecao = "Por favor, digite um e-mail válido";
                    }catch ( FirebaseAuthUserCollisionException e ){
                        excecao = "Esta conta já foi cadastrada";
                    }catch ( Exception e) {
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this,
                            excecao,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public String verificaTipoUsuario(){
        return swithTipoUsuario.isChecked() ? "M" : "P" ;  // M = motorista "true" e P = passageiro "false"

    }
}
