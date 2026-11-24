package pe.edu.pppdiaz.mesapartes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pe.edu.pppdiaz.mesapartes.db.Conexion;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Dependencia;
import pe.edu.pppdiaz.mesapartes.modelo.Estado;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;
import pe.edu.pppdiaz.mesapartes.modelo.Movimiento;
import pe.edu.pppdiaz.mesapartes.modelo.Remitente;
import pe.edu.pppdiaz.mesapartes.util.Fechas;

/** Acceso a datos de expedientes: registro, derivación, consulta, estados e historial. */
public class ExpedienteDAO {

    private static final String SELECT_BASE =
            "SELECT e.id, e.codigo, e.tipo_documento, e.asunto, e.folios, e.fecha_registro, e.estado, "
            + "e.dependencia_actual_id, d.nombre AS dependencia, "
            + "r.id AS rid, r.tipo_doc, r.documento, r.nombre, r.telefono, r.correo "
            + "FROM expediente e "
            + "JOIN remitente r ON r.id = e.remitente_id "
            + "LEFT JOIN dependencia d ON d.id = e.dependencia_actual_id ";

    // ---------------------------------------------------------------- REGISTRO

    /**
     * Registra un expediente (y su remitente) en una sola transacción y genera el código correlativo
     * con el formato EXP-AAAA-00001.
     */
    public Expediente registrar(Expediente e, String usuario) throws MesaPartesException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                long remitenteId = RemitenteDAO.guardar(cn, e.getRemitente());
                e.getRemitente().setId(remitenteId);

                LocalDateTime ahora = LocalDateTime.now().withNano(0);
                String codigo = siguienteCodigo(cn, ahora.getYear());

                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO expediente(codigo, remitente_id, tipo_documento, asunto, folios, "
                        + "fecha_registro, estado, dependencia_actual_id) VALUES (?,?,?,?,?,?,?,NULL)")) {
                    ps.setString(1, codigo);
                    ps.setLong(2, remitenteId);
                    ps.setString(3, e.getTipoDocumento());
                    ps.setString(4, e.getAsunto().trim());
                    ps.setInt(5, e.getFolios());
                    ps.setString(6, ahora.format(Fechas.BD));
                    ps.setString(7, Estado.PENDIENTE);
                    ps.executeUpdate();
                }
                long expId;
                try (PreparedStatement ps = cn.prepareStatement("SELECT last_insert_rowid()");
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    expId = rs.getLong(1);
                }
                insertarMovimiento(cn, expId, ahora, "REGISTRO",
                        "Expediente registrado en Mesa de Partes (" + e.getTipoDocumento()
                        + ", " + e.getFolios() + " folio(s)).", usuario);
                cn.commit();

                e.setId(expId);
                e.setCodigo(codigo);
                e.setFechaRegistro(ahora);
                e.setEstado(Estado.PENDIENTE);
                e.setDependenciaActualId(null);
                e.setDependenciaActual(null);
                return e;
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new MesaPartesException("No se pudo registrar el expediente: " + ex.getMessage(), ex);
        }
    }

    /**
     * Genera el siguiente código del año. Usa una tabla de correlativos para que el número de un
     * expediente eliminado nunca se vuelva a asignar a otro.
     */
    private String siguienteCodigo(Connection cn, int anio) throws SQLException {
        String prefijo = "EXP-" + anio + "-";
        int maxExistente = 0;
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT MAX(CAST(SUBSTR(codigo, ?) AS INTEGER)) FROM expediente WHERE codigo LIKE ?")) {
            ps.setInt(1, prefijo.length() + 1);
            ps.setString(2, prefijo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maxExistente = rs.getInt(1);
                }
            }
        }
        int ultimo = 0;
        try (PreparedStatement ps = cn.prepareStatement("SELECT ultimo FROM correlativo WHERE anio = ?")) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ultimo = rs.getInt(1);
                }
            }
        }
        int siguiente = Math.max(maxExistente, ultimo) + 1;
        try (PreparedStatement ps = cn.prepareStatement("INSERT OR REPLACE INTO correlativo(anio, ultimo) VALUES (?,?)")) {
            ps.setInt(1, anio);
            ps.setInt(2, siguiente);
            ps.executeUpdate();
        }
        return String.format("%s%05d", prefijo, siguiente);
    }

    // ---------------------------------------------------------------- ELIMINAR

    /** Elimina el expediente y todo su historial de movimientos (en una sola transacción). */
    public void eliminar(long expedienteId) throws MesaPartesException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement("DELETE FROM movimiento WHERE expediente_id = ?")) {
                    ps.setLong(1, expedienteId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cn.prepareStatement("DELETE FROM expediente WHERE id = ?")) {
                    ps.setLong(1, expedienteId);
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("El expediente no existe.");
                    }
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new MesaPartesException("No se pudo eliminar el expediente: " + ex.getMessage(), ex);
        }
    }

    // ---------------------------------------------------------------- DERIVACIÓN / ESTADO

    public void derivar(long expedienteId, Dependencia destino, String observacion, String usuario)
            throws MesaPartesException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE expediente SET dependencia_actual_id = ?, estado = ? WHERE id = ?")) {
                    ps.setLong(1, destino.getId());
                    ps.setString(2, Estado.EN_PROCESO);
                    ps.setLong(3, expedienteId);
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("El expediente no existe.");
                    }
                }
                String detalle = "Derivado a " + destino.getNombre()
                        + (observacion == null || observacion.isBlank() ? "" : ". Obs.: " + observacion.trim());
                insertarMovimiento(cn, expedienteId, LocalDateTime.now().withNano(0), "DERIVACIÓN", detalle, usuario);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new MesaPartesException("No se pudo derivar el expediente: " + ex.getMessage(), ex);
        }
    }

    public void cambiarEstado(long expedienteId, String nuevoEstado, String observacion, String usuario)
            throws MesaPartesException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement("UPDATE expediente SET estado = ? WHERE id = ?")) {
                    ps.setString(1, nuevoEstado);
                    ps.setLong(2, expedienteId);
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("El expediente no existe.");
                    }
                }
                String detalle = "Estado cambiado a " + nuevoEstado
                        + (observacion == null || observacion.isBlank() ? "" : ". Obs.: " + observacion.trim());
                insertarMovimiento(cn, expedienteId, LocalDateTime.now().withNano(0), "ESTADO", detalle, usuario);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new MesaPartesException("No se pudo cambiar el estado: " + ex.getMessage(), ex);
        }
    }

    private void insertarMovimiento(Connection cn, long expId, LocalDateTime fecha, String tipo,
                                    String detalle, String usuario) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO movimiento(expediente_id, fecha, tipo, detalle, usuario) VALUES (?,?,?,?,?)")) {
            ps.setLong(1, expId);
            ps.setString(2, fecha.format(Fechas.BD));
            ps.setString(3, tipo);
            ps.setString(4, detalle);
            ps.setString(5, usuario);
            ps.executeUpdate();
        }
    }

    // ---------------------------------------------------------------- CONSULTAS

    /**
     * Búsqueda con filtros opcionales (pasar {@code null} o vacío para ignorar un filtro).
     * Se usa en Derivación, Consulta y Reportes.
     */
    public List<Expediente> buscar(String codigo, String documento, LocalDate desde, LocalDate hasta,
                                   String estado) throws MesaPartesException {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (codigo != null && !codigo.isBlank()) {
            sql.append("AND e.codigo LIKE ? ");
            params.add("%" + codigo.trim().toUpperCase() + "%");
        }
        if (documento != null && !documento.isBlank()) {
            sql.append("AND r.documento LIKE ? ");
            params.add("%" + documento.trim() + "%");
        }
        if (desde != null) {
            sql.append("AND date(e.fecha_registro) >= ? ");
            params.add(desde.toString());
        }
        if (hasta != null) {
            sql.append("AND date(e.fecha_registro) <= ? ");
            params.add(hasta.toString());
        }
        if (estado != null && !estado.isBlank()) {
            sql.append("AND e.estado = ? ");
            params.add(estado);
        }
        sql.append("ORDER BY e.id DESC");
        return consultar(sql.toString(), params);
    }

    public List<Expediente> ultimos(int cantidad) throws MesaPartesException {
        List<Object> params = new ArrayList<>();
        params.add(cantidad);
        return consultar(SELECT_BASE + "ORDER BY e.id DESC LIMIT ?", params);
    }

    public Expediente porId(long id) throws MesaPartesException {
        List<Object> params = new ArrayList<>();
        params.add(id);
        List<Expediente> lista = consultar(SELECT_BASE + "WHERE e.id = ?", params);
        return lista.isEmpty() ? null : lista.get(0);
    }

    private List<Expediente> consultar(String sql, List<Object> params) throws MesaPartesException {
        List<Expediente> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new MesaPartesException("Error al consultar expedientes: " + e.getMessage(), e);
        }
    }

    private Expediente mapear(ResultSet rs) throws SQLException {
        Remitente r = new Remitente();
        r.setId(rs.getLong("rid"));
        r.setTipoDoc(rs.getString("tipo_doc"));
        r.setDocumento(rs.getString("documento"));
        r.setNombre(rs.getString("nombre"));
        r.setTelefono(rs.getString("telefono"));
        r.setCorreo(rs.getString("correo"));

        Expediente e = new Expediente();
        e.setId(rs.getLong("id"));
        e.setCodigo(rs.getString("codigo"));
        e.setRemitente(r);
        e.setTipoDocumento(rs.getString("tipo_documento"));
        e.setAsunto(rs.getString("asunto"));
        e.setFolios(rs.getInt("folios"));
        e.setFechaRegistro(LocalDateTime.parse(rs.getString("fecha_registro"), Fechas.BD));
        e.setEstado(rs.getString("estado"));
        long depId = rs.getLong("dependencia_actual_id");
        e.setDependenciaActualId(rs.wasNull() ? null : depId);
        e.setDependenciaActual(rs.getString("dependencia"));
        return e;
    }

    public List<Movimiento> movimientos(long expedienteId) throws MesaPartesException {
        List<Movimiento> lista = new ArrayList<>();
        String sql = "SELECT id, expediente_id, fecha, tipo, detalle, usuario FROM movimiento "
                + "WHERE expediente_id = ? ORDER BY id";
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Movimiento m = new Movimiento();
                    m.setId(rs.getLong("id"));
                    m.setExpedienteId(rs.getLong("expediente_id"));
                    m.setFecha(LocalDateTime.parse(rs.getString("fecha"), Fechas.BD));
                    m.setTipo(rs.getString("tipo"));
                    m.setDetalle(rs.getString("detalle"));
                    m.setUsuario(rs.getString("usuario"));
                    lista.add(m);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new MesaPartesException("Error al cargar el historial: " + e.getMessage(), e);
        }
    }

    /** Cantidad de expedientes por estado (incluye los estados sin expedientes con valor 0). */
    public Map<String, Integer> contarPorEstado() throws MesaPartesException {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (String s : Estado.TODOS) {
            mapa.put(s, 0);
        }
        try (Connection cn = Conexion.obtener(); Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT estado, COUNT(*) FROM expediente GROUP BY estado")) {
            while (rs.next()) {
                mapa.put(rs.getString(1), rs.getInt(2));
            }
            return mapa;
        } catch (SQLException e) {
            throw new MesaPartesException("Error al contar expedientes: " + e.getMessage(), e);
        }
    }
}
