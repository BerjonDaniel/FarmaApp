package com.example.farmaapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.farmaapp.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private TextView tvBarCode;
    private static final String API_URL  = "https://cima.aemps.es/cima/rest/medicamento";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //inflamos el layout
        setContentView(R.layout.activity_notepad);

        /*
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        */
    }

    //---------------------------Menu inicial con PopUp--------------------------------------
    public void showPopup(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popupmenu);
        popup.show();
        LinearLayout dim_layout = (LinearLayout) findViewById(R.id.dim_layout);
        dim_layout.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        LinearLayout dim_layout = (LinearLayout) findViewById(R.id.dim_layout);
        dim_layout.setVisibility(View.INVISIBLE);

        switch (item.getItemId()) {
            case R.id.item1:
                Toast.makeText(this, "Codigo de barras", Toast.LENGTH_SHORT);
                escanear();
                return true;

            case R.id.item2:
                Toast.makeText(this, "Codigo nacional", Toast.LENGTH_SHORT);
                //createNoteFromCN();
                return true;

            default:

                return false;
        }
    }
    //menu para setting

    //----------------OPCION 1: Escanear-----------------------------
    public void  escanear(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    //----------------Busqueda en Api------------------------------

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //fillData();

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent );
        if(result != null)
            if (result.getContents() != null){
                String resultindex = result.getContents().substring(6,12);
                tvBarCode = findViewById(R.id.resultado);

                APIFromCIMATask api = new APIFromCIMATask();
                api.cn = resultindex;
                api.execute();

                tvBarCode.setText("El código de barras es:\n" + resultindex);
            }else{
                Toast.makeText(this, "Scanning cancelled", Toast.LENGTH_LONG).show();
            }
    }



    private class APIFromCIMATask extends AsyncTask<String, String, String> {

        String cn;
        String response;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        protected String doInBackground(String... urls) {
            // We make the connection
            try {
                // Creamos la conexión
                URL url = new URL(API_URL + "?cn=" + cn);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
//                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
//                conn.setDoOutput(true);

                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                response = "";

                for (int c; (c = in.read()) >= 0; )
                    response += (char) c;

            } catch (IOException e) {
                e.printStackTrace();
                response = "ERROR: " + e.getLocalizedMessage();
            }

//            SetPrescriptionData(response);
            Log.i("RECEIVED", response);

            return response;
        }

        protected void onPostExecute(String result){switchMaintoBarCode(response);
        }
    }
    private void switchMaintoBarCode(String result) {
        // Creamos el Intent que va a lanzar la activity de editar medicamento (ApiCodeBar)
        Intent intent = new Intent(this, GuardarMedicamento.class);
        startActivityForResult(intent, 1);
        // Creamos la informacion a pasar entre actividades
        //Bundle b = new Bundle();
        //b.putString("result", result);

        // Asociamos esta informacion al intent
        intent.putExtra("Result", result);
        // Iniciamos la nueva actividad
        startActivity(intent);
    }


    //-----------------Si pulsamos la opcion de Settings---------------------------
    private void switchMaintoSettings() {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void showsettings(View view) {
        PopupMenu popupsettings = new PopupMenu(this, view);
        popupsettings.setOnMenuItemClickListener(this);
        popupsettings.inflate(R.menu.menu_settings);
        popupsettings.show();

    }
}