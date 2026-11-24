package pe.edu.pppdiaz.mesapartes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import pe.edu.pppdiaz.mesapartes.db.Conexion;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Remitente;

public class RemitenteDAO {

    public Remitente buscarPorDocumento(String documento) throws MesaPartesException {
        String sql = "SELECT id, tipo_doc, documento, nombre, telefono, correo FROM remitente WHERE documento = ?";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, documento.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Remitente r = new Remitente();
                r.setId(rs.getLong("id"));
                r.setTipoDoc(rs.getString("tipo_doc"));
                r.setDocumento(rs.getString("documento"));
                r.setNombre(rs.getString("nombre"));
                r.setTelefono(rs.getString("telefono"));
                r.setCorreo(rs.getString("correo"));
                return r;
            }
        } catch (SQLException e) {
            throw new MesaPartesException("Error al buscar el remitente: " + e.getMessage(), e);
        }
    }

    /**
     * Inserta el remitente o actualiza sus datos si el documento ya existe.
     * Usa la conexión recibida para participar en la transacción del expediente.
     * @return id del remitente
     */
    static long guardar(Connection cn, Remitente r) throws SQLException {
        long id = -1;
        try (PreparedStatement ps = cn.prepareStatement("SELECT id FROM remitente WHERE documento = ?")) {
            ps.setString(1, r.getDocumento().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getLong(1);
                }
            }
        }
        if (id >= 0) {
            try (PreparedStatement ps = cn.prepareStatement(
                    "UPDATE remitente SET tipo_doc=?, nombre=?, telefono=?, correo=? WHERE id=?")) {
                ps.setString(1, r.getTipoDoc());
                ps.setString(2, r.getNombre().trim());
                ps.setString(3, r.getTelefono());
                ps.setString(4, r.getCorreo());
                ps.setLong(5, id);
                ps.executeUpdate();
            }
            return id;
        }
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO remitente(tipo_doc, documento, nombre, telefono, correo) VALUES (?,?,?,?,?)")) {
            ps.setString(1, r.getTipoDoc());
            ps.setString(2, r.getDocumento().trim());
            ps.setString(3, r.getNombre().trim());
            ps.setString(4, r.getTelefono());
            ps.setString(5, r.getCorreo());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = cn.prepareStatement("SELECT last_insert_rowid()");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
