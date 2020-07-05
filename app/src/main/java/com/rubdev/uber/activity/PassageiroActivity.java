package com.rubdev.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rubdev.uber.R;
import com.rubdev.uber.config.ConfiguracaoFirebase;
import com.rubdev.uber.helper.UsuarioFirebase;
import com.rubdev.uber.model.Destino;
import com.rubdev.uber.model.Requisicao;
import com.rubdev.uber.model.Usuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Componentes
    private EditText editDestino;
    private LinearLayout linearLayoutDestino;
    private Button buttonChamarCarro;


    private GoogleMap mMap;
    private FirebaseAuth autenticacao;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localPassageiro;
    private boolean carroChamado = false;
    private DatabaseReference firebaseRef;
    private Requisicao requisicao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);



        inicializarComponentes();

        // adicionar listener para status da requisição
        verificarStatusRequisicao();

    }

    private void verificarStatusRequisicao(){

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        final DatabaseReference requisicoes = firebaseRef.child( "requisicoes" );
        Query requisicoesPesquisa = requisicoes.orderByChild( "passageiro/id" )
                .equalTo( usuarioLogado.getId() );

        requisicoesPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<Requisicao> lista = new ArrayList<>();

                for ( DataSnapshot ds: dataSnapshot.getChildren() ){
                    lista.add( ds.getValue( Requisicao.class ) );

                }

                Collections.reverse( lista );
                if (lista != null && lista.size() > 0) {
                    requisicao = lista.get(0);

                    switch (requisicao.getStatus()){
                        case  Requisicao.STATUS_AGUARDANDO:
                            linearLayoutDestino.setVisibility( View.GONE );
                            buttonChamarCarro.setText("Cancelar chamado");
                            carroChamado = true;
                            break;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // recup. o local do usuário
        recuperarLocalizacaoUsuario();

    }

    public void chamarCarro(View view){

        if ( !carroChamado ) { // carro nao chamado


            //inicio

            String enderecoDestino = editDestino.getText().toString();

            if ( !enderecoDestino.equals("") || enderecoDestino != null ){

                Address addressDestino = recuperarEndereco( enderecoDestino );
                if ( addressDestino != null ) {

                    final Destino destino = new Destino();
                    destino.setCidade( addressDestino.getAdminArea());
                    destino.setCep(addressDestino.getPostalCode() );
                    destino.setBairro( addressDestino.getSubLocality());
                    destino.setRua( addressDestino.getThoroughfare() );
                    destino.setNumero( addressDestino.getFeatureName());
                    destino.setLatitude( String.valueOf (addressDestino.getLatitude()));
                    destino.setLongitude( String.valueOf( addressDestino.getLongitude()));

                    StringBuilder mensagem = new StringBuilder();
                    mensagem.append( "Cidade: " + destino.getCidade() );
                    mensagem.append( "\nRua: " + destino.getRua() );
                    mensagem.append( "\nBairro: " + destino.getBairro() );
                    mensagem.append( "\nNúmero: " + destino.getNumero() );
                    mensagem.append( "\nCEP: " + destino.getCep() );

                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Confirme seu endereço")
                            .setMessage(mensagem)
                            .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //salvar a requisição
                                    salvarRequisicao( destino );
                                    carroChamado = true;
                                }
                            }).setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }else {
                Toast.makeText(this,
                        "Informe o endereço de destino",
                        Toast.LENGTH_SHORT).show();
            }

            //FIM
        }else { // carro chamado então pode cancelar a requisição

            carroChamado = false;
        }



    }

    private void salvarRequisicao(Destino destino){
        Requisicao requisicao = new Requisicao();
        requisicao.setDestino( destino );

        Usuario usuarioPassageiro = UsuarioFirebase.getDadosUsuarioLogado();
        usuarioPassageiro.setLatitude( String.valueOf( localPassageiro.latitude) );
        usuarioPassageiro.setLongitude( String.valueOf( localPassageiro.longitude));

        requisicao.setPassageiro( usuarioPassageiro );
        requisicao.setStatus( Requisicao.STATUS_AGUARDANDO );
        requisicao.salvar();

        linearLayoutDestino.setVisibility( View.GONE );
        buttonChamarCarro.setText("Cancelar corrida");


    }

    private Address recuperarEndereco( String endereco ){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco, 1);
            if (listaEnderecos != null && listaEnderecos.size() > 0){
                Address address = listaEnderecos.get(0);

               return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
            return null;
    }

    private void recuperarLocalizacaoUsuario() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // recuperar lat e long
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localPassageiro = new LatLng(latitude, longitude);

                mMap.clear();
                mMap.addMarker(
                        new MarkerOptions()
                                .position( localPassageiro )
                                .title("Meu local")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
                );
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localPassageiro, 17)
                );
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    10,
                    locationListener
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuSair :
                autenticacao.signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Iniciar uma viagem");
        setSupportActionBar(toolbar);

        //inicializar os componentes
        editDestino = findViewById(R.id.editDestino);
        linearLayoutDestino = findViewById(R.id.linearLayoutDestino);
        buttonChamarCarro = findViewById(R.id.buttonChamarCarro);

        // config iniciais
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
}
