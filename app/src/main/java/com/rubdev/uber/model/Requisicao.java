package com.rubdev.uber.model;

public class Requisicao {

    private  String id;
    private  String status;
    private Usuario passageiro;
    private Usuario motorista;
    private Destino destino;

    public static final String STATUS_AGUARDANDO = "aguardando";

}
