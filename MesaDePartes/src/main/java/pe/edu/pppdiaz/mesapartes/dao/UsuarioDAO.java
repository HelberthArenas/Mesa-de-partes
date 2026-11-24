package pe.edu.pppdiaz.mesapartes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import pe.edu.pppdiaz.mesapartes.db.Conexion;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Usuario;
import pe.edu.pppdiaz.mesapartes.util.Seguridad;

public class UsuarioDAO {

    /** @return el usuario si las credenciales son correctas; {@code null} en caso contrario. */
    public Usuario autenticar(String usuario, String clave) throws MesaPartesException {
        String sql = "SELECT id, usuario, nombre FROM usuario WHERE usuario = ? AND clave_hash = ?";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario.trim());
            ps.setString(2, Seguridad.sha256(clave));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getLong("id"));
                    u.setUsuario(rs.getString("usuario"));
                    u.setNombre(rs.getString("nombre"));
                    return u;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new MesaPartesException("No se pudo validar el usuario: " + e.getMessage(), e);
        }
    }
}
