package pe.edu.pppdiaz.mesapartes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;
import pe.edu.pppdiaz.mesapartes.modelo.Movimiento;
import pe.edu.pppdiaz.mesapartes.modelo.Remitente;
import pe.edu.pppdiaz.mesapartes.util.DocumentoHtml;

class DocumentoHtmlTest {

    private Expediente ejemplo() {
        Remitente r = new Remitente();
        r.setDocumento("45678912");
        r.setNombre("Ana <Quispe> & Cía");
        Expediente e = new Expediente();
        e.setCodigo("EXP-2026-00001");
        e.setRemitente(r);
        e.setTipoDocumento("Solicitud");
        e.setAsunto("Constancia de estudios");
        e.setFolios(3);
        e.setFechaRegistro(java.time.LocalDateTime.of(2026, 11, 18, 10, 30));
        return e;
    }

    @Test
    void lunesDeLaSemana() {
        // 20/11/2026 es viernes -> lunes 16/11/2026
        assertEquals(LocalDate.of(2026, 11, 16), DocumentoHtml.lunesDe(LocalDate.of(2026, 11, 20)));
        assertEquals(LocalDate.of(2026, 11, 16), DocumentoHtml.lunesDe(LocalDate.of(2026, 11, 16)));
        assertEquals(LocalDate.of(2026, 11, 16), DocumentoHtml.lunesDe(LocalDate.of(2026, 11, 22)));
    }

    @Test
    void escapaCaracteresEspeciales() {
        assertEquals("a &lt;b&gt; &amp; c", DocumentoHtml.esc("a <b> & c"));
        assertEquals("", DocumentoHtml.esc(null));
    }

    @Test
    void fichaContieneLosDatosDelExpediente() {
        List<Movimiento> movs = new ArrayList<>();
        String html = DocumentoHtml.ficha(ejemplo(), movs, "admin");
        assertTrue(html.contains("EXP-2026-00001"));
        assertTrue(html.contains("Ana &lt;Quispe&gt; &amp; Cía"));
        assertFalse(html.contains("<Quispe>"));
    }

    @Test
    void listadoPorRangoDeFechasYEstado() {
        List<Expediente> lista = new ArrayList<>();
        lista.add(ejemplo());
        String html = DocumentoHtml.reporteListado(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30),
                lista, "Pendiente", "admin");
        assertTrue(html.contains("LISTADO DE TRÁMITES"));
        assertTrue(html.contains("Del 01/11/2026 al 30/11/2026"));
        assertTrue(html.contains("Pendiente"));            // estado filtrado
        assertTrue(html.contains("EXP-2026-00001"));
        assertTrue(html.contains("Resumen por ubicación actual"));
        assertTrue(html.contains("Mesa de Partes"));        // ubicación inicial del expediente
    }

    @Test
    void listadoSinDocumentos() {
        String html = DocumentoHtml.reporteListado(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 7),
                new ArrayList<>(), null, "admin");
        assertTrue(html.contains("No se registraron documentos en el rango de fechas seleccionado"));
        assertTrue(html.contains("Todos"));                 // sin filtro de estado
    }
}
