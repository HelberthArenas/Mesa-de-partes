package pe.edu.pppdiaz.mesapartes.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import pe.edu.pppdiaz.mesapartes.util.Seguridad;

/**
 * Conexión JDBC a SQLite. La base de datos se crea sola la primera vez en:
 * {carpeta del usuario}/.mesapartes/mesapartes.db
 * (se puede cambiar con -Dmesapartes.db=ruta/archivo.db).
 */
public final class Conexion {

    private Conexion() { }

    public static String rutaBD() {
        String personalizada = System.getProperty("mesapartes.db");
        if (personalizada != null && !personalizada.isBlank()) {
            return personalizada;
        }
        return System.getProperty("user.home") + File.separator + ".mesapartes"
                + File.separator + "mesapartes.db";
    }

    public static Connection obtener() throws SQLException {
        Connection cn = DriverManager.getConnection("jdbc:sqlite:" + rutaBD());
        try (Statement st = cn.createStatement()) {
            st.execute("PRAGMA busy_timeout = 5000"); // si la base está ocupada, espera hasta 5 s
            st.execute("PRAGMA foreign_keys = ON");
        }
        return cn;
    }

    /** Crea la carpeta, las tablas y los datos iniciales si no existen. */
    public static void inicializar() throws SQLException {
        File carpeta = new File(rutaBD()).getAbsoluteFile().getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        try (Connection cn = obtener(); Statement st = cn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS usuario ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "usuario TEXT NOT NULL UNIQUE,"
                    + "clave_hash TEXT NOT NULL,"
                    + "nombre TEXT NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS dependencia ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "nombre TEXT NOT NULL UNIQUE)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS remitente ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "tipo_doc TEXT NOT NULL,"
                    + "documento TEXT NOT NULL UNIQUE,"
                    + "nombre TEXT NOT NULL,"
                    + "telefono TEXT,"
                    + "correo TEXT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS expediente ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "codigo TEXT NOT NULL UNIQUE,"
                    + "remitente_id INTEGER NOT NULL REFERENCES remitente(id),"
                    + "tipo_documento TEXT NOT NULL,"
                    + "asunto TEXT NOT NULL,"
                    + "folios INTEGER NOT NULL DEFAULT 1,"
                    + "fecha_registro TEXT NOT NULL,"
                    + "estado TEXT NOT NULL DEFAULT 'Pendiente',"
                    + "dependencia_actual_id INTEGER REFERENCES dependencia(id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS movimiento ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "expediente_id INTEGER NOT NULL REFERENCES expediente(id),"
                    + "fecha TEXT NOT NULL,"
                    + "tipo TEXT NOT NULL,"
                    + "detalle TEXT NOT NULL,"
                    + "usuario TEXT NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS correlativo ("
                    + "anio INTEGER PRIMARY KEY,"
                    + "ultimo INTEGER NOT NULL)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_exp_fecha ON expediente(fecha_registro)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_exp_estado ON expediente(estado)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_mov_exp ON movimiento(expediente_id)");

            // Datos iniciales
            for (String dep : new String[] {"Dirección","Secretaria Académica", "Administracción", "Jefatura Unidad Academica", "Jefatura Empleabilidad", "Área de Calidad", "P.E. Agropecuaria","P.E. Construcción Civil", "P.E. Elictricidad", "P.E. Desarrollo de Sistemas","P.E. Electronica Industrial","P.E. Mecatronica Automotriz", "P.E. Mecánica Producción"}) {
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT OR IGNORE INTO dependencia(nombre) VALUES (?)")) {
                    ps.setString(1, dep);
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT OR IGNORE INTO usuario(usuario, clave_hash, nombre) VALUES (?,?,?)")) {
                ps.setString(1, "admin");
                ps.setString(2, Seguridad.sha256("975114"));
                ps.setString(3, "Administrador Mesa de Partes");
                ps.executeUpdate();
            }
        }
    }
}
