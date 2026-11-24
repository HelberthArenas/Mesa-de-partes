package pe.edu.pppdiaz.mesapartes.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;

/** Exportación de listados de expedientes a CSV (se abre directamente en Excel). */
public final class Csv {

    public static final String[] COLUMNAS = {
        "Código", "Fecha registro", "Remitente", "DNI/RUC", "Tipo", "Asunto", "Folios", "Estado", "Ubicación"
    };

    private Csv() { }

    private static String campo(Object valor) {
        return "\"" + String.valueOf(valor).replace("\"", "\"\"") + "\"";
    }

    public static void exportar(File archivo, List<Expediente> lista) throws IOException {
        try (PrintWriter pw = new PrintWriter(archivo, StandardCharsets.UTF_8)) {
            pw.write('\uFEFF'); // BOM para que Excel respete las tildes
            pw.println(String.join(";", COLUMNAS));
            for (Expediente e : lista) {
                pw.println(String.join(";",
                        campo(e.getCodigo()), campo(Fechas.paraVista(e.getFechaRegistro())),
                        campo(e.getRemitente().getNombre()), campo(e.getRemitente().getDocumento()),
                        campo(e.getTipoDocumento()), campo(e.getAsunto()), campo(e.getFolios()),
                        campo(e.getEstado()), campo(e.getDependenciaActual())));
            }
        }
    }
}
