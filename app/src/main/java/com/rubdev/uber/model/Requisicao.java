package com.rubdev.uber.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rubdev.uber.config.ConfiguracaoFirebase;

public class Requisicao {

    private  String id;
    private  String status;
    private Usuario passageiro;
    private Usuario motorista;
    private Destino destino;

    public static final String STATUS_AGUARDANDO = "aguardando";
    public static final String STATUS_A_CAMINHO = "acaminho";
    public static final String STATUS_VIAGEM = "viagem";
    public static final String STATUS_FINALIZADA = "finalizada";

    public Requisicao() {
    }

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        String idRrequisicao = requisicoes.push().getKey();
        setId( idRrequisicao );

        requisicoes.child( getId()).setValue(this);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }
}
