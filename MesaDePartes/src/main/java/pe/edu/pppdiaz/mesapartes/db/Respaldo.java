package pe.edu.pppdiaz.mesapartes.db;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;

/** Copia de seguridad de la base de datos SQLite. */
public final class Respaldo {

    private Respaldo() { }

    /** Nombre sugerido para el archivo de respaldo, con fecha y hora. */
    public static String nombreSugerido() {
        return "copia_seguridad_mesapartes_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".db";
    }

    /**
     * Crea una copia consistente de la base de datos en el archivo indicado
     * (usa VACUUM INTO, que genera una copia completa y compacta).
     * Si el archivo ya existe, se reemplaza.
     */
    public static void crear(File destino) throws MesaPartesException {
        try {
            File origen = new File(Conexion.rutaBD());
            if (destino.getCanonicalFile().equals(origen.getCanonicalFile())) {
                throw new MesaPartesException("El destino no puede ser la misma base de datos en uso.");
            }
            File carpeta = destino.getAbsoluteFile().getParentFile();
            if (carpeta != null && !carpeta.exists() && !carpeta.mkdirs()) {
                throw new MesaPartesException("No se pudo crear la carpeta de destino.");
            }
            Files.deleteIfExists(destino.toPath());
            String ruta = destino.getAbsolutePath().replace("'", "''");
            try (Connection cn = Conexion.obtener(); Statement st = cn.createStatement()) {
                st.execute("VACUUM INTO '" + ruta + "'");
            }
        } catch (SQLException e) {
            throw new MesaPartesException("No se pudo crear la copia de seguridad: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MesaPartesException("No se pudo escribir en el destino: " + e.getMessage(), e);
        }
    }
}
