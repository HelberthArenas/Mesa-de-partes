package pe.edu.pppdiaz.mesapartes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pe.edu.pppdiaz.mesapartes.dao.ExpedienteDAO;
import pe.edu.pppdiaz.mesapartes.db.Conexion;
import pe.edu.pppdiaz.mesapartes.db.Respaldo;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;
import pe.edu.pppdiaz.mesapartes.modelo.Remitente;

/** Prueba de la copia de seguridad de la base de datos. */
class RespaldoTest {

    @BeforeEach
    void prepararBD(@TempDir Path carpeta) throws Exception {
        System.setProperty("mesapartes.db", new File(carpeta.toFile(), "origen.db").getAbsolutePath());
        Conexion.inicializar();
    }

    @Test
    void laCopiaContieneLosMismosDatos(@TempDir Path destinoDir) throws Exception {
        Remitente r = new Remitente();
        r.setDocumento("12345678");
        r.setNombre("Persona de prueba");
        Expediente e = new Expediente();
        e.setRemitente(r);
        e.setTipoDocumento("Carta");
        e.setAsunto("Asunto de prueba");
        new ExpedienteDAO().registrar(e, "admin");

        File copia = new File(destinoDir.toFile(), "copia.db");
        Respaldo.crear(copia);
        assertTrue(copia.exists());

        try (Connection cn = DriverManager.getConnection("jdbc:sqlite:" + copia.getAbsolutePath());
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM expediente")) {
            rs.next();
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void nombreSugeridoTerminaEnDb() {
        assertTrue(Respaldo.nombreSugerido().endsWith(".db"));
    }
}
