package co.edu.udea.pi.sjm.petted.vista.listadoCita;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import co.edu.udea.pi.sjm.petted.R;
import co.edu.udea.pi.sjm.petted.dao.CitaDAO;
import co.edu.udea.pi.sjm.petted.dao.MascotaDAO;
import co.edu.udea.pi.sjm.petted.dao.impl.CitaDAOImpl;
import co.edu.udea.pi.sjm.petted.dao.impl.MascotaDAOImpl;
import co.edu.udea.pi.sjm.petted.dto.Cita;
import co.edu.udea.pi.sjm.petted.dto.Mascota;
import co.edu.udea.pi.sjm.petted.dto.Vacuna;
import co.edu.udea.pi.sjm.petted.util.Validacion;

public class CitaFormularioActivity extends AppCompatActivity {

    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private SimpleDateFormat formatoHora = new SimpleDateFormat("hh:mm a", Locale.US);
    private SimpleDateFormat formatoFechaHora = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US);

    private DatePickerDialog electorDeFechaDialogo;
    private TimePickerDialog electorDeHoraDialogo;

    private String mascotaId;

    private EditText etNombre;
    private EditText etDescripcion;
    private Spinner spinnerTipo;
    private EditText etFecha;
    private ImageButton ibtnFecha;
    private EditText etHora;
    private ImageButton ibtnHora;

    private CitaDAO daoC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_cita);

        ActionBar actionBar = ((AppCompatActivity) this)
                .getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_close_white);
        }

        mascotaId = this.getIntent().getExtras().getString("mascotaId");

        etNombre = (EditText) findViewById(R.id.etNombreCita);
        etDescripcion = (EditText) findViewById(R.id.etDescripcionCita);
        spinnerTipo = (Spinner) findViewById(R.id.spinnerTipoCita);
        etFecha = (EditText) findViewById(R.id.etFechaCita);
        ibtnFecha = (ImageButton) findViewById(R.id.ibtnFechaCita);
        etHora = (EditText) findViewById(R.id.etHoraCita);
        ibtnHora = (ImageButton) findViewById(R.id.ibtnHoraCita);

        mostrarFecha();
        mostrarHora();

        inicializarSpinner();

        if (this.getIntent().getExtras().getString("citaId") == null) {
            super.setTitle("Nueva Cita");
        } else {
            inicializarFormulario((Cita) this.getIntent().getExtras().getSerializable("cita"));
            super.setTitle("Editar Cita");
        }
        if (this.getIntent().getExtras().getSerializable("vacuna") != null) {
            inicializarFormulario((Vacuna) this.getIntent().getExtras().getSerializable("vacuna"));
            new AlertDialog.Builder(this)
                    .setTitle("Asignar Cita")
                    .setMessage("Cree una cita con la fecha proxima de la vacuna.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton("Crear despues", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }
    }

    private void inicializarFormulario(Vacuna vacuna) {
        etNombre.setText(vacuna.getNombre());
        etDescripcion.setText("Aplicación de la vacuna " + vacuna.getNombre());
        spinnerTipo.setSelection(2);
        spinnerTipo.setEnabled(false);
        if (vacuna.getFechaProxima() != null) {
            etFecha.setText(formatoFecha.format(vacuna.getFechaProxima().getTime()));
        }
    }

    public void onClickGuardarCita() {
        Cita c;
        UUID uuid = UUID.randomUUID();
        c = new Cita();

        c.setId(uuid.toString());
        c.setMascota(mascotaId);
        c.setNombre(etNombre.getText().toString());
        c.setDescripcion(etDescripcion.getText().toString());
        c.setTipo((String) spinnerTipo.getSelectedItem());
        if (!etFecha.getText().toString().equals("")) {
            try {
                c.setFechaHora(formatoFechaHora.parse(etFecha.getText().toString() + " " + etHora.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e("Error en fecha", e.getMessage());
            }
        }

        switch (Validacion.validarCita(c)) {
            case 0:
                if (this.getIntent().getExtras().getString("citaId") == null) {
                    daoC = new CitaDAOImpl();
                    daoC.insertarCita(c, this);
                    Toast.makeText(CitaFormularioActivity.this, "Cita insertada", Toast.LENGTH_SHORT).show();

                } else {
//                    c.setId(((Cita) this.getIntent().getExtras().getSerializable("cita")).getId());
//                    c.setEstado(((Cita) this.getIntent().getExtras().getSerializable("cita")).getEstado());
                    daoC.actualizarCita(c, this);
                    setResult(0);
                    Toast.makeText(CitaFormularioActivity.this, "Cita editada", Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
        }
    }

    private void inicializarSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.TiposDeCitas,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTipo.setAdapter(adapter);
    }

    // TODO: INICIALIZAR FORMULARIO
    private void inicializarFormulario(Cita cita) {
    }

    private void mostrarFecha() {
        ibtnFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                electorDeFechaDialogo.show();
            }
        });
        etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                electorDeFechaDialogo.show();
            }
        });
        Calendar calendario = Calendar.getInstance();
        electorDeFechaDialogo = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int año, int mes, int dia) {
                Calendar nuevaFecha = Calendar.getInstance();
                nuevaFecha.set(año, mes, dia);
                etFecha.setText(formatoFecha.format(nuevaFecha.getTime()));
                if (etHora.getText().toString().equals("")) {
                    etHora.setText(formatoHora.format(nuevaFecha.getTime()));
                }
            }
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));
    }

    private void mostrarHora() {
        ibtnHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                electorDeHoraDialogo.show();
            }
        });
        etHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                electorDeHoraDialogo.show();
            }
        });
        Calendar calendario = Calendar.getInstance();
        electorDeHoraDialogo = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                Calendar nuevaHora = Calendar.getInstance();
                nuevaHora.set(Calendar.HOUR, selectedHour);
                nuevaHora.set(Calendar.MINUTE, selectedMinute);
                if (Calendar.AM_PM == 0) {
                    nuevaHora.set(Calendar.AM_PM, 1);
                }
                nuevaHora.set(Calendar.AM_PM, 0);
                etHora.setText(formatoHora.format(nuevaHora.getTime()));
            }
        }, calendario.get(Calendar.HOUR), calendario.get(Calendar.MINUTE), false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_formulario_cita, menu);
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
                onClickGuardarCita();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
