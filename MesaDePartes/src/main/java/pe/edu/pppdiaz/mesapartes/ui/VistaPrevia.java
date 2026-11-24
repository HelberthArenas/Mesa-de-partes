package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/** Vista previa de un documento HTML con opción de imprimirlo. */
public class VistaPrevia extends JPanel {

    private static final long serialVersionUID = 1L;
    private final JEditorPane editor = new JEditorPane();

    public VistaPrevia() {
        setLayout(new BorderLayout());
        HTMLEditorKit kit = new HTMLEditorKit();
        editor.setEditorKit(kit);
        editor.setEditable(false);
        editor.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(editor);
        scroll.getViewport().setBackground(new Color(0xB8BEC9));
        scroll.setBorder(BorderFactory.createTitledBorder("Vista previa"));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void setHtml(String html) {
        HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        doc.setAsynchronousLoadPriority(-1); // carga el logo de forma síncrona (necesario para imprimir)
        editor.setDocument(doc);
        editor.setText(html);
        editor.setCaretPosition(0);
    }

    /**
     * Abre el cuadro de impresión del sistema (desde ahí también se puede elegir "Microsoft Print to PDF").
     * @param horizontal true para papel A4 horizontal (reportes anchos)
     */
    public void imprimir(boolean horizontal) {
        PrintRequestAttributeSet atributos = new HashPrintRequestAttributeSet();
        atributos.add(MediaSizeName.ISO_A4);
        atributos.add(horizontal ? OrientationRequested.LANDSCAPE : OrientationRequested.PORTRAIT);
        try {
            editor.print(null, new MessageFormat("Página {0}"), true, null, atributos, true);
        } catch (PrinterException ex) {
            Estilo.error(this, "No se pudo imprimir: " + ex.getMessage());
        }
    }

    /** Acceso al componente para pruebas. */
    public JEditorPane getEditor() { return editor; }
}
