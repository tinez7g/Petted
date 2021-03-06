package co.edu.udea.pi.sjm.petted.vista.mascota;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import co.edu.udea.pi.sjm.petted.R;
import co.edu.udea.pi.sjm.petted.dao.MascotaDAO;
import co.edu.udea.pi.sjm.petted.dao.impl.MascotaDAOImpl;
import co.edu.udea.pi.sjm.petted.util.Validacion;
import co.edu.udea.pi.sjm.petted.util.Utility;
import co.edu.udea.pi.sjm.petted.dto.Mascota;

public class MascotaFormularioActivity extends AppCompatActivity {


    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    private EditText etFechaNacimiento;
    private DatePickerDialog electorDeFechaDialogo;

    private Spinner spinnerTipo;
    private Spinner spinnerRaza;
    private Spinner spinnerGenero;
    private EditText etNombre;
    private ImageButton ibtnFechaNacimiento;
    private ImageView ivFotoPrevia;
    private Bitmap foto;

    private String APP_DIRECTORY = "myPictureApp/";
    private String MEDIA_DIRECTORY = APP_DIRECTORY + "media";
    private String TEMPORAL_PICTURE_NAME = "temporal.jpg";

    private final int PHOTO_CODE = 100;
    private final int SELECT_PICTURE = 200;

    private Mascota mascota;
    private MascotaDAO daoM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_mascota);

        ActionBar actionBar = ((AppCompatActivity) this).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_close_white);
        }

        etFechaNacimiento = (EditText) findViewById(R.id.etFechaNacimientoMascota);
        spinnerTipo = (Spinner) findViewById(R.id.spinnerTipoMascota);
        spinnerRaza = (Spinner) findViewById(R.id.spinnerRazaMascota);
        spinnerGenero = (Spinner) findViewById(R.id.spinnerGeneroMascota);
        etNombre = (EditText) findViewById(R.id.etNombreMascota);
        ibtnFechaNacimiento = (ImageButton) findViewById(R.id.ibtnFechaNacimientoMascota);
        ivFotoPrevia = (ImageView) findViewById(R.id.ivFotoPreviaMascota);

        mostrarFecha();

        if (this.getIntent().getExtras().getString("mascotaId") != null) {
            daoM = new MascotaDAOImpl();
            mascota = daoM.obtenerMascota(this.getIntent().getExtras().getString("mascotaId"));
        }

        inicializarSpinners();

        if (this.getIntent().getExtras().getString("mascotaId") == null) {
            super.setTitle("Nueva Mascota");
        } else {
            inicializarFormulario();
            super.setTitle("Editar Mascota");
        }
    }

    public void onClickGuardarMascota() {

        verificarCamposRequeridos();

        Mascota m;
        ParseUser propietario = ParseUser.getCurrentUser();

        UUID uuid = UUID.randomUUID();
        m = new Mascota();

        m.setId(uuid.toString());
        m.setNombre(etNombre.getText().toString());
        m.setPropietario(propietario.getObjectId());
        if (!etFechaNacimiento.getText().toString().equals("")) {
            try {
                m.setFechaNacimiento(formatoFecha.parse(etFechaNacimiento.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e("Error en fecha", e.getMessage());
            }
        }
        m.setTipo((String) spinnerTipo.getSelectedItem());
        m.setRaza((String) spinnerRaza.getSelectedItem());
        m.setGenero((String) spinnerGenero.getSelectedItem());
        if (foto != null) {
            m.setFoto(Utility.getBytes(Utility.resizeImage(foto, 207, 207)));
        } else {
            m.setFoto(Utility.getBytes(Utility.resizeImage(this, R.drawable.mascota1, 207, 207)));
        }
        m.setNotificaciones(true);

        switch (Validacion.validarMascota(m)) {
            case 0:
                if ((etNombre.getError() == null) && (etFechaNacimiento.getError() == null)) {
                    if (this.getIntent().getExtras().getString("mascotaId") == null) {
                        daoM = new MascotaDAOImpl();
                        daoM.insertarMascota(m);
                    } else {
                        daoM = new MascotaDAOImpl();
                        m.setId(mascota.getId());
                        m.setNotificaciones(mascota.getNotificaciones());
                        m.setIdTag(mascota.getIdTag());
                        daoM.actualizarMascota(m);
                        setResult(0);
                    }
                    finish();
                }
                break;
            case 1:
                break;
        }
    }

    private void verificarCamposRequeridos() {
        if (etNombre.getText().toString().equals("")) {
            etNombre.setError("Campo requerido.");
        }
        if (etFechaNacimiento.getText().toString().equals("")) {
            etFechaNacimiento.setError("Campo requerido.");
        } else {
            etFechaNacimiento.setError(null);
        }
    }

    private void inicializarFormulario() {
        etNombre.setText(mascota.getNombre());

        if (mascota.getFechaNacimiento() != null) {
            etFechaNacimiento.setText(formatoFecha.format(mascota.getFechaNacimiento()));
        }

        int posicion = Utility.getIndex(spinnerTipo, mascota.getTipo());
        spinnerTipo.setSelection(posicion);

        spinnerGenero.setSelection(Utility.getIndex(spinnerGenero, mascota.getGenero()));

        if (mascota.getFoto() != null) {
            foto = Utility.getFoto(mascota.getFoto());
            ivFotoPrevia.setImageBitmap(foto);
        }
    }

    public void onClickFoto(View view) {
        final CharSequence[] opciones = {"Tomar Foto", "Seleccionar foto", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MascotaFormularioActivity.this);
        builder.setTitle("Selecciona una opción: ");
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int seleccion) {
                switch (seleccion) {
                    case 0:
                        tomarFoto();
                        break;
                    case 1:
                        seleccionarFoto();
                        break;
                    case 2:
                        dialog.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PHOTO_CODE:
                if (resultCode == RESULT_OK) {
                    String dir = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                            + File.separator + TEMPORAL_PICTURE_NAME;
                    foto = Utility.cutImage(BitmapFactory.decodeFile(dir));
                    ivFotoPrevia.setImageBitmap(foto);
                }
                break;
            case SELECT_PICTURE:
                if (resultCode == RESULT_OK) {
                    Uri dir = data.getData();
                    try {
                        foto = Utility.cutImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), dir));
                        ivFotoPrevia.setImageBitmap(foto);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    public void tomarFoto() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        file.mkdirs();

        String path = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                + File.separator + TEMPORAL_PICTURE_NAME;
        File newFile = new File(path);

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));

        startActivityForResult(i, PHOTO_CODE);
    }

    public void seleccionarFoto() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Selecciona app de imagen"), SELECT_PICTURE);
    }


    private void mostrarFecha() {
        ibtnFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                electorDeFechaDialogo.show();
            }
        });
        etFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                electorDeFechaDialogo.show();
            }
        });
        Calendar calendario = Calendar.getInstance();
        electorDeFechaDialogo = new DatePickerDialog(this, new OnDateSetListener() {
            public void onDateSet(DatePicker view, int año, int mes, int dia) {
                Calendar nuevaFecha = Calendar.getInstance();
                nuevaFecha.set(año, mes, dia);
                etFechaNacimiento.setText(formatoFecha.format(nuevaFecha.getTime()));
                etFechaNacimiento.setError(null);
            }
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));
    }


    private void inicializarSpinners() {
        ArrayAdapter<CharSequence> adapter;

        adapter = ArrayAdapter.createFromResource(this, R.array.TiposDeMascotas,
                android.R.layout.simple_spinner_item);
        final String raza;
        if (this.getIntent().getExtras().getString("mascotaId") == null) {
            raza = null;
        } else {
            raza = mascota.getRaza();
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTipo.setAdapter(adapter);
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSelectedItemTiposDeMascotas(view, position, raza);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        adapter = ArrayAdapter.createFromResource(this, R.array.Generos,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerGenero.setAdapter(adapter);
    }

    private void onSelectedItemTiposDeMascotas(View view, int posicion, String raza) {
        try {
            cambiarItemsSpinnerRaza(posicion, view);
            if (raza != null) {
                seleccionarItemRaza(raza);
            }
        } catch (Exception e) {
        }
    }

    private void cambiarItemsSpinnerRaza(int posicion, View view) {
        ArrayAdapter<CharSequence> adapter = null;
        switch (posicion) {
            case 0:
                adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.RazasDePerros,
                        android.R.layout.simple_dropdown_item_1line);
                break;
            case 1:
                adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.RazasDeGatos,
                        android.R.layout.simple_dropdown_item_1line);
                break;
            case 2:
                adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.RazasDeAves,
                        android.R.layout.simple_dropdown_item_1line);
                break;
            case 3:
                adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.RazasDeReptiles,
                        android.R.layout.simple_dropdown_item_1line);
                break;
            case 4:
                adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.RazasOtro,
                        android.R.layout.simple_dropdown_item_1line);
                break;
        }
        spinnerRaza.setAdapter(adapter);
    }

    private void seleccionarItemRaza(String raza) {
        spinnerRaza.setSelection(Utility.getIndex(spinnerRaza, raza));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_formulario_mascota, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_guardar:
                onClickGuardarMascota();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
