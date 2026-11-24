package pe.edu.pppdiaz.mesapartes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pe.edu.pppdiaz.mesapartes.dao.DependenciaDAO;
import pe.edu.pppdiaz.mesapartes.dao.ExpedienteDAO;
import pe.edu.pppdiaz.mesapartes.dao.UsuarioDAO;
import pe.edu.pppdiaz.mesapartes.db.Conexion;
import pe.edu.pppdiaz.mesapartes.modelo.Dependencia;
import pe.edu.pppdiaz.mesapartes.modelo.Estado;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;
import pe.edu.pppdiaz.mesapartes.modelo.Remitente;

/** Pruebas de integración con una base de datos SQLite temporal. */
class ExpedienteDAOTest {

    private final ExpedienteDAO dao = new ExpedienteDAO();

    @BeforeEach
    void prepararBD(@TempDir Path carpeta) throws Exception {
        System.setProperty("mesapartes.db", new File(carpeta.toFile(), "prueba.db").getAbsolutePath());
        Conexion.inicializar();
    }

    private Expediente nuevo(String dni, String nombre, String asunto) {
        Remitente r = new Remitente();
        r.setTipoDoc("DNI");
        r.setDocumento(dni);
        r.setNombre(nombre);
        Expediente e = new Expediente();
        e.setRemitente(r);
        e.setTipoDocumento("Solicitud");
        e.setAsunto(asunto);
        e.setFolios(2);
        return e;
    }

    @Test
    void loginCorrectoEIncorrecto() throws Exception {
        UsuarioDAO u = new UsuarioDAO();
        assertNotNull(u.autenticar("admin", "admin123"));
        assertNull(u.autenticar("admin", "mala"));
    }

    @Test
    void generaCodigosCorrelativos() throws Exception {
        Expediente a = dao.registrar(nuevo("11111111", "Ana Quispe", "Constancia de estudios"), "admin");
        Expediente b = dao.registrar(nuevo("22222222", "Luis Mamani", "Licencia de estudios"), "admin");
        String anio = String.valueOf(LocalDate.now().getYear());
        assertEquals("EXP-" + anio + "-00001", a.getCodigo());
        assertEquals("EXP-" + anio + "-00002", b.getCodigo());
        assertEquals(Estado.PENDIENTE, a.getEstado());
    }

    @Test
    void remitenteExistenteSeReutiliza() throws Exception {
        dao.registrar(nuevo("33333333", "Rosa Condori", "Primer trámite"), "admin");
        dao.registrar(nuevo("33333333", "Rosa Condori", "Segundo trámite"), "admin");
        assertEquals(2, dao.buscar(null, "33333333", null, null, null).size());
    }

    @Test
    void derivarCambiaUbicacionYEstadoYGuardaHistorial() throws Exception {
        Expediente e = dao.registrar(nuevo("44444444", "Pedro Huamani", "Solicitud de certificado"), "admin");
        Dependencia caja = new DependenciaDAO().listar().stream()
                .filter(d -> d.getNombre().equals("Caja")).findFirst().orElseThrow();
        dao.derivar(e.getId(), caja, "Pago de derechos", "admin");

        Expediente actualizado = dao.porId(e.getId());
        assertEquals("Caja", actualizado.getDependenciaActual());
        assertEquals(Estado.EN_PROCESO, actualizado.getEstado());
        assertEquals(2, dao.movimientos(e.getId()).size());
    }

    @Test
    void cambiarEstadoYBuscarPorFiltros() throws Exception {
        Expediente e = dao.registrar(nuevo("55555555", "Carmen Flores", "Oficio de invitación"), "admin");
        dao.cambiarEstado(e.getId(), Estado.ATENDIDO, "Entregado", "admin");

        List<Expediente> porEstado = dao.buscar(null, null, null, null, Estado.ATENDIDO);
        assertEquals(1, porEstado.size());
        assertEquals(1, dao.buscar(e.getCodigo(), null, null, null, null).size());
        assertEquals(1, dao.buscar(null, "5555", LocalDate.now(), LocalDate.now(), null).size());
        assertTrue(dao.buscar(null, null, LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), null).isEmpty());
        assertEquals(1, dao.contarPorEstado().get(Estado.ATENDIDO));
    }

    @Test
    void eliminarBorraExpedienteEHistorialYNoReutilizaCodigo() throws Exception {
        Expediente a = dao.registrar(nuevo("66666666", "Elena Rojas", "Primer trámite"), "admin");
        Expediente b = dao.registrar(nuevo("77777777", "Mario Paz", "Segundo trámite"), "admin");
        Dependencia dep = new DependenciaDAO().listar().get(0);
        dao.derivar(b.getId(), dep, "obs", "admin");

        dao.eliminar(b.getId());

        assertNull(dao.porId(b.getId()));
        assertTrue(dao.movimientos(b.getId()).isEmpty());
        assertEquals(1, dao.buscar(null, null, null, null, null).size());

        Expediente c = dao.registrar(nuevo("88888888", "Nora Ruiz", "Tercer trámite"), "admin");
        String anio = String.valueOf(LocalDate.now().getYear());
        assertEquals("EXP-" + anio + "-00003", c.getCodigo()); // el 00002 eliminado no se reutiliza
        assertNotNull(a.getCodigo());
    }

    @Test
    void listadoPorRangoDeFechasYEstado() throws Exception {
        Expediente a = dao.registrar(nuevo("90000001", "Uno", "Trámite uno"), "admin");
        Expediente b = dao.registrar(nuevo("90000002", "Dos", "Trámite dos"), "admin");
        dao.cambiarEstado(b.getId(), Estado.ATENDIDO, "ok", "admin");
        LocalDate hoy = LocalDate.now();

        assertEquals(2, dao.buscar(null, null, hoy, hoy, null).size());                       // sin filtrar estado
        assertEquals(1, dao.buscar(null, null, hoy, hoy, Estado.ATENDIDO).size());             // solo atendidos
        assertEquals(1, dao.buscar(null, null, hoy, hoy, Estado.PENDIENTE).size());            // solo pendientes
        assertEquals(0, dao.buscar(null, null, hoy.minusDays(30), hoy.minusDays(10), null).size()); // rango sin datos
        assertEquals(2, dao.buscar(null, null, hoy.minusDays(30), hoy.plusDays(1), null).size());   // rango amplio
        assertNotNull(a.getCodigo());
    }
}
