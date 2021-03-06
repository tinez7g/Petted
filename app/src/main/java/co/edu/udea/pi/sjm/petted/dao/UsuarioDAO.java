package co.edu.udea.pi.sjm.petted.dao;

import android.content.Context;

import java.util.List;

import co.edu.udea.pi.sjm.petted.dto.Usuario;

/**
 * Created by Juan on 02/10/2015.
 */
public interface UsuarioDAO {
    void insertarUsuario(Usuario usuario);

    Usuario obtenerUsuario(String correo, Context context);

    Usuario obtenerUsuarioLogueado(Context context);

    void actualizarUsuario(Usuario usuario, Context context);

    void eliminarUsuario(Usuario usuario, Context context);

    List<Usuario> obtener(Context context);

    Usuario obtenerUsuario(String username);

    Usuario obtenerUsuarioPorCorreo(String correo);
}
