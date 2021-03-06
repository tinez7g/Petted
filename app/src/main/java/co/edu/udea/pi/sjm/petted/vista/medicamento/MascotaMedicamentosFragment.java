package co.edu.udea.pi.sjm.petted.vista.medicamento;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.edu.udea.pi.sjm.petted.R;
import co.edu.udea.pi.sjm.petted.dao.MedicamentoDAO;
import co.edu.udea.pi.sjm.petted.dao.impl.MedicamentoDAOImpl;
import co.edu.udea.pi.sjm.petted.dto.Medicamento;
import co.edu.udea.pi.sjm.petted.vista.mascota.MascotaActivity;

/**
 * Created by Juan on 21/09/2015.
 */
public class MascotaMedicamentosFragment extends Fragment {

    private ListView lvMedicamentos;
    private ImageButton ibtnNuevoMedicamento;
    private MedicamentoDAO daoMe;
    private List<Medicamento> listaMediacamentos;
    private MedicamentoCustomAdapter customAdapter;
    private MascotaActivity ma;

    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private SimpleDateFormat formatoHora = new SimpleDateFormat("hh:mm a", Locale.US);

    public MascotaMedicamentosFragment() {
    }

    public static MascotaMedicamentosFragment newInstance() {
        MascotaMedicamentosFragment fragment = new MascotaMedicamentosFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mascota_medicamentos, container, false);
        ma = (MascotaActivity) getActivity();

        lvMedicamentos = (ListView) rootView.findViewById(R.id.lvListaMedicamentos);
        ibtnNuevoMedicamento = (ImageButton) rootView.findViewById(R.id.ibtnNuevoMedicamento);

        listaMediacamentos = new ArrayList<>();
        daoMe = new MedicamentoDAOImpl();

        lvMedicamentos.setEmptyView(rootView.findViewById(R.id.llNoMedicamentos));
        onResume();

        lvMedicamentos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Medicamento m = customAdapter.getItem(position);
                final String fechaHoraInicioS;
                if (m.getFechaHoraInicio() != null) {
                    fechaHoraInicioS = "Fecha Inicio: " + formatoFecha.format(m.getFechaHoraInicio()) +
                            "\nHora: " + formatoHora.format(m.getFechaHoraInicio());
                } else {
                    fechaHoraInicioS = "";
                }
                new AlertDialog.Builder(ma)
                        .setTitle("Medicamento " + m.getNombre())
                        .setMessage("Suministro vía " + m.getViaSuministro() +
                                "\n\nDosis: " + "\n" + m.getPesoDosis() + "grs. " + " Cada "
                                + m.getIntervaloDosis() + " horas, " + m.getCantidadDosis() + " veces." +
                                "\n\n" + fechaHoraInicioS)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setNeutralButton("Editar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(ma, MedicamentoFormularioActivity.class);
                                i.putExtra("medicamentoId", m.getId());
                                startActivity(i);
                            }
                        })
                        .setNegativeButton("Eliminar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new AlertDialog.Builder(ma)
                                        .setTitle("Eliminar Medicamento")
                                        .setMessage("¿Desea eliminar a " + m.getNombre() + "?")
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                daoMe.eliminarMedicamento(m);
                                                onResume();
                                            }
                                        })
                                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_delete)
                                        .show();

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        });

        ibtnNuevoMedicamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarActividadMedicamentoNuevo();
            }
        });

        return rootView;
    }

    private void iniciarActividadMedicamentoNuevo() {
        Intent i = new Intent(ma, MedicamentoFormularioActivity.class);
        i.putExtra("mascotaId", ma.getMascota().getId());
        startActivity(i);
    }

    @Override
    public void onResume() {
        super.onResume();
        listaMediacamentos = daoMe.obtenerMedicamentos(ma.getMascota().getId());
        customAdapter = new MedicamentoCustomAdapter(ma, listaMediacamentos);
        lvMedicamentos.setAdapter(customAdapter);
    }
}