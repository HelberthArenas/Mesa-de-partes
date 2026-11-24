package pe.edu.pppdiaz.mesapartes.util;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pe.edu.pppdiaz.mesapartes.modelo.Estado;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;
import pe.edu.pppdiaz.mesapartes.modelo.Movimiento;

/**
 * Genera los documentos imprimibles (en HTML simple, compatible con Swing):
 * la ficha de un expediente y el reporte semanal de documentación.
 */
public final class DocumentoHtml {

    private static final String AZUL = "#1F3A93";
    private static final String CLARO = "#E8EEF9";

    private DocumentoHtml() { }

    /** Lunes de la semana que contiene la fecha indicada. */
    public static LocalDate lunesDe(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /** Escapa caracteres especiales de HTML. */
    public static String esc(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String nvl(String s) {
        return s == null || s.isBlank() ? "-" : esc(s);
    }

    // ------------------------------------------------------------------ piezas comunes

    private static String abrir(int ptTablas) {
        return "<html><head><style>"
             + "body { font-family: SansSerif; font-size: 11pt; }"
             + "td { font-family: SansSerif; font-size: " + ptTablas + "pt; }"
             + "th { font-family: SansSerif; font-size: " + ptTablas + "pt; }"
             + "</style></head><body>"
             + "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"14\"><tr><td>";
    }

    private static String cerrar() {
        return "</td></tr></table></body></html>";
    }

    private static String cabecera(String titulo, String subtitulo) {
        URL logo = Recursos.logoUrl();
        StringBuilder sb = new StringBuilder();
        sb.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\"><tr>");
        if (logo != null) {
            sb.append("<td width=\"84\" valign=\"middle\"><img src=\"").append(logo)
              .append("\" width=\"72\" height=\"72\"></td>");
        }
        sb.append("<td valign=\"middle\"><b><font size=\"4\">INSTITUTO DE EDUCACIÓN SUPERIOR PÚBLICO “PEDRO P. DÍAZ”</font></b><br>")
          .append("Programa de Estudios: Desarrollo de Sistemas de Información<br>")
          .append("Mesa de Partes</td>");
        sb.append("<td align=\"right\" valign=\"middle\"><b><font size=\"5\" color=\"").append(AZUL).append("\">")
          .append(esc(titulo)).append("</font></b><br>").append(esc(subtitulo)).append("</td>");
        sb.append("</tr></table><hr>");
        return sb.toString();
    }

    private static String pie(String usuario) {
        return "<br><font size=\"2\" color=\"#555555\">Impreso por " + esc(usuario) + " el "
             + Fechas.paraVista(LocalDateTime.now()) + " - Sistema de Gestión y Seguimiento de Documentos</font>";
    }

    private static String fila(String etiqueta, String valor) {
        return "<tr><td width=\"26%\" bgcolor=\"" + CLARO + "\"><b>" + etiqueta + "</b></td><td>" + valor + "</td></tr>";
    }

    // ------------------------------------------------------------------ FICHA DE EXPEDIENTE

    /** Ficha completa de un expediente: datos, remitente, documento e historial. */
    public static String ficha(Expediente e, List<Movimiento> movimientos, String usuario) {
        StringBuilder sb = new StringBuilder(abrir(10));
        sb.append(cabecera("FICHA DE EXPEDIENTE", e.getCodigo()));

        sb.append("<p><b>DATOS DEL EXPEDIENTE</b></p>");
        sb.append("<table width=\"100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"5\">");
        sb.append(fila("Código de seguimiento", "<b>" + esc(e.getCodigo()) + "</b>"));
        sb.append(fila("Fecha y hora de registro", Fechas.paraVista(e.getFechaRegistro())));
        sb.append(fila("Estado actual", "<b>" + esc(e.getEstado()) + "</b>"));
        sb.append(fila("Ubicación actual", esc(e.getDependenciaActual())));
        sb.append("</table>");

        sb.append("<p><b>DATOS DEL REMITENTE</b></p>");
        sb.append("<table width=\"100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"5\">");
        sb.append(fila(esc(e.getRemitente().getTipoDoc()), esc(e.getRemitente().getDocumento())));
        sb.append(fila("Nombre / Razón social", esc(e.getRemitente().getNombre())));
        sb.append(fila("Teléfono", nvl(e.getRemitente().getTelefono())));
        sb.append(fila("Correo", nvl(e.getRemitente().getCorreo())));
        sb.append("</table>");

        sb.append("<p><b>DATOS DEL DOCUMENTO</b></p>");
        sb.append("<table width=\"100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"5\">");
        sb.append(fila("Tipo de documento", esc(e.getTipoDocumento())));
        sb.append(fila("N.º de folios", String.valueOf(e.getFolios())));
        sb.append(fila("Asunto", esc(e.getAsunto())));
        sb.append("</table>");

        sb.append("<p><b>HISTORIAL DE MOVIMIENTOS</b></p>");
        sb.append("<table width=\"100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"5\">");
        sb.append("<tr bgcolor=\"").append(CLARO).append("\"><th width=\"16%\">Fecha</th><th width=\"15%\">Movimiento</th><th>Detalle</th><th width=\"10%\">Usuario</th></tr>");
        for (Movimiento m : movimientos) {
            sb.append("<tr><td>").append(Fechas.paraVista(m.getFecha()).replace(" ", "&nbsp;")).append("</td><td>")
              .append(esc(m.getTipo())).append("</td><td>").append(esc(m.getDetalle()))
              .append("</td><td>").append(esc(m.getUsuario())).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<br><br><br><table width=\"100%\" border=\"0\"><tr>")
          .append("<td align=\"center\">______________________________<br>Responsable de Mesa de Partes</td>")
          .append("<td align=\"center\">______________________________<br>Recibí conforme</td></tr></table>");
        sb.append(pie(usuario));
        sb.append(cerrar());
        return sb.toString();
    }

    // ------------------------------------------------------------------ LISTADO DE TRÁMITES

    /**
     * Listado de trámites por rango de fechas y estado del documento.
     * @param desde primer día del rango (inclusive)
     * @param hasta último día del rango (inclusive)
     * @param estadoFiltro estado aplicado a la consulta, o {@code null} para todos
     */
    public static String reporteListado(LocalDate desde, LocalDate hasta, List<Expediente> lista,
                                        String estadoFiltro, String usuario) {
        List<Expediente> orden = ordenar(lista);

        // Resumen por ubicación actual (de mayor a menor cantidad)
        Map<String, Integer> porUbicacion = new LinkedHashMap<>();
        for (Expediente e : orden) {
            porUbicacion.merge(e.getDependenciaActual(), 1, Integer::sum);
        }
        List<Map.Entry<String, Integer>> entradas = new ArrayList<>(porUbicacion.entrySet());
        entradas.sort(Map.Entry.<String, Integer>comparingByValue().reversed()
                .thenComparing(Map.Entry.comparingByKey()));
        List<String[]> filas = new ArrayList<>();
        for (Map.Entry<String, Integer> en : entradas) {
            filas.add(new String[] {en.getKey(), String.valueOf(en.getValue())});
        }
        if (filas.isEmpty()) {
            filas.add(new String[] {"-", "0"});
        }

        StringBuilder sb = new StringBuilder(abrir(9));
        sb.append(cabecera("LISTADO DE TRÁMITES",
                "Del " + desde.format(Fechas.VISTA_DIA) + " al " + hasta.format(Fechas.VISTA_DIA)));
        sb.append(totales(orden, estadoFiltro));
        sb.append(resumenDoble(orden, "Resumen por ubicación actual", new String[] {"Ubicación", "Cantidad"}, filas));
        sb.append(detalle(orden, "el rango de fechas seleccionado"));
        sb.append(firmas("Jefatura / Coordinación Académica"));
        sb.append(pie(usuario));
        sb.append(cerrar());
        return sb.toString();
    }

    // ------------------------------------------------------------------ piezas de los reportes

    private static List<Expediente> ordenar(List<Expediente> lista) {
        List<Expediente> orden = new ArrayList<>(lista);
        orden.sort(Comparator.comparing(Expediente::getFechaRegistro).thenComparingLong(Expediente::getId));
        return orden;
    }

    private static String totales(List<Expediente> orden, String estadoFiltro) {
        int folios = 0;
        for (Expediente e : orden) {
            folios += e.getFolios();
        }
        return "<p>Estado filtrado: <b>" + (estadoFiltro == null ? "Todos" : esc(estadoFiltro))
             + "</b> &nbsp;&nbsp;|&nbsp;&nbsp; Total de documentos: <b>" + orden.size()
             + "</b> &nbsp;&nbsp;|&nbsp;&nbsp; Total de folios: <b>" + folios + "</b></p>";
    }

    /** Dos tablas lado a lado: resumen por estado (izquierda) y resumen por período (derecha). */
    private static String resumenDoble(List<Expediente> orden, String tituloDerecha, String[] columnas,
                                       List<String[]> filasDerecha) {
        Map<String, Integer> porEstado = new LinkedHashMap<>();
        for (String s : Estado.TODOS) {
            porEstado.put(s, 0);
        }
        for (Expediente e : orden) {
            porEstado.merge(e.getEstado(), 1, Integer::sum);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td valign=\"top\" width=\"48%\">");
        sb.append("<b>Resumen por estado</b><table width=\"100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"2\">");
        sb.append("<tr bgcolor=\"").append(CLARO).append("\"><th align=\"left\">Estado</th><th>Cantidad</th></tr>");
        for (Map.Entry<String, Integer> en : porEstado.entrySet()) {
            sb.append("<tr><td>").append(esc(en.getKey())).append("</td><td align=\"center\">")
              .append(en.getValue()).append("</td></tr>");
        }
        sb.append("</table></td><td width=\"4%\">&nbsp;</td><td valign=\"top\" width=\"48%\">");
        sb.append("<b>").append(esc(tituloDerecha)).append("</b><table width=\"100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"2\">");
        sb.append("<tr bgcolor=\"").append(CLARO).append("\">");
        for (int c = 0; c < columnas.length; c++) {
            sb.append(c == 0 ? "<th align=\"left\">" : "<th>").append(esc(columnas[c])).append("</th>");
        }
        sb.append("</tr>");
        for (String[] f : filasDerecha) {
            sb.append("<tr>");
            for (int c = 0; c < f.length; c++) {
                sb.append(c == 0 ? "<td>" : "<td align=\"center\">").append(esc(f[c])).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table></td></tr></table>");
        return sb.toString();
    }

    private static String detalle(List<Expediente> orden, String periodo) {
        StringBuilder sb = new StringBuilder("<p><b>Detalle de documentos recibidos</b></p>");
        if (orden.isEmpty()) {
            sb.append("<p>No se registraron documentos en ").append(periodo).append(".</p>");
            return sb.toString();
        }
        sb.append("<table width=\"100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"4\">");
        sb.append("<tr bgcolor=\"").append(CLARO).append("\"><th width=\"3%\">#</th><th width=\"13%\">Código</th>")
          .append("<th width=\"9%\">Fecha</th><th width=\"13%\">Remitente</th><th width=\"9%\">DNI/RUC</th>")
          .append("<th width=\"7%\">Tipo</th><th>Asunto</th><th width=\"5%\">Folios</th>")
          .append("<th width=\"8%\">Estado</th><th width=\"10%\">Ubicación</th></tr>");
        int n = 1;
        for (Expediente e : orden) {
            sb.append("<tr><td align=\"center\">").append(n++).append("</td><td>").append(esc(e.getCodigo()))
              .append("</td><td>").append(e.getFechaRegistro().format(Fechas.VISTA_DIA)).append("<br>")
              .append(e.getFechaRegistro().toLocalTime().toString().substring(0, 5))
              .append("</td><td>").append(esc(e.getRemitente().getNombre()))
              .append("</td><td>").append(esc(e.getRemitente().getDocumento()))
              .append("</td><td>").append(esc(e.getTipoDocumento()))
              .append("</td><td>").append(esc(e.getAsunto()))
              .append("</td><td align=\"center\">").append(e.getFolios())
              .append("</td><td>").append(esc(e.getEstado()))
              .append("</td><td>").append(esc(e.getDependenciaActual())).append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static String firmas(String derecha) {
        return "<br><br><br><table width=\"100%\" border=\"0\"><tr>"
             + "<td align=\"center\">______________________________<br>Responsable de Mesa de Partes</td>"
             + "<td align=\"center\">______________________________<br>" + esc(derecha) + "</td></tr></table>";
    }
}
