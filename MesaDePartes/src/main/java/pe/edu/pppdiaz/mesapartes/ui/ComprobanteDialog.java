package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.print.PrinterException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;
import pe.edu.pppdiaz.mesapartes.util.Fechas;

/** Ventana con el comprobante de recepción del expediente, lista para imprimir. */
public class ComprobanteDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JTextArea texto = new JTextArea();

    public ComprobanteDialog(Window padre, Expediente e) {
        super(padre, "Comprobante de expediente " + e.getCodigo(), ModalityType.APPLICATION_MODAL);
        texto.setEditable(false);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        texto.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        texto.setText(construirTexto(e));
        texto.setCaretPosition(0);

        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.addActionListener(ev -> imprimir());
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(ev -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnImprimir);
        botones.add(btnCerrar);

        setLayout(new BorderLayout());
        add(new JScrollPane(texto), BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        setSize(560, 560);
        setLocationRelativeTo(padre);
    }

    /** Texto del comprobante (público para poder probarlo). */
    public static String construirTexto(Expediente e) {
        String linea = "=".repeat(52);
        String sep = "-".repeat(52);
        StringBuilder sb = new StringBuilder();
        sb.append(linea).append('\n');
        sb.append("  INSTITUTO DE EDUCACION SUPERIOR PUBLICO\n");
        sb.append("  \"PEDRO P. DIAZ\"\n");
        sb.append("  MESA DE PARTES - COMPROBANTE DE EXPEDIENTE\n");
        sb.append(linea).append("\n\n");
        sb.append(String.format("  CODIGO DE SEGUIMIENTO : %s%n", e.getCodigo()));
        sb.append(String.format("  FECHA Y HORA          : %s%n", Fechas.paraVista(e.getFechaRegistro())));
        sb.append(String.format("  ESTADO                : %s%n", e.getEstado()));
        sb.append(String.format("  UBICACION ACTUAL      : %s%n", e.getDependenciaActual()));
        sb.append('\n').append(sep).append('\n');
        sb.append("  DATOS DEL REMITENTE\n").append(sep).append('\n');
        sb.append(String.format("  %-4s                  : %s%n", e.getRemitente().getTipoDoc(), e.getRemitente().getDocumento()));
        sb.append(String.format("  NOMBRE / RAZON SOCIAL : %s%n", e.getRemitente().getNombre()));
        sb.append(String.format("  TELEFONO              : %s%n", nvl(e.getRemitente().getTelefono())));
        sb.append(String.format("  CORREO                : %s%n", nvl(e.getRemitente().getCorreo())));
        sb.append('\n').append(sep).append('\n');
        sb.append("  DATOS DEL DOCUMENTO\n").append(sep).append('\n');
        sb.append(String.format("  TIPO                  : %s%n", e.getTipoDocumento()));
        sb.append(String.format("  FOLIOS                : %d%n", e.getFolios()));
        sb.append("  ASUNTO                :\n");
        for (String l : ajustar(e.getAsunto(), 46)) {
            sb.append("    ").append(l).append('\n');
        }
        sb.append('\n').append(linea).append('\n');
        sb.append("  Conserve este comprobante. Con el codigo de\n");
        sb.append("  seguimiento puede consultar el estado de su\n");
        sb.append("  tramite en Mesa de Partes.\n");
        sb.append(linea).append('\n');
        return sb.toString();
    }

    private static String nvl(String s) { return s == null || s.isBlank() ? "-" : s; }

    private static java.util.List<String> ajustar(String texto, int ancho) {
        java.util.List<String> lineas = new java.util.ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.trim().split("\\s+")) {
            if (actual.length() + palabra.length() + 1 > ancho && actual.length() > 0) {
                lineas.add(actual.toString());
                actual.setLength(0);
            }
            if (actual.length() > 0) actual.append(' ');
            actual.append(palabra);
        }
        if (actual.length() > 0) lineas.add(actual.toString());
        return lineas;
    }

    private void imprimir() {
        try {
            texto.print();
        } catch (PrinterException ex) {
            Estilo.error(this, "No se pudo imprimir: " + ex.getMessage());
        }
    }
}
