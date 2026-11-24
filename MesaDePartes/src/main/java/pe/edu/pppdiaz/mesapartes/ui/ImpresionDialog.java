package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/** Ventana de vista previa e impresión de un documento. */
public class ImpresionDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public ImpresionDialog(Window padre, String titulo, String html, boolean horizontal) {
        super(padre, titulo, ModalityType.APPLICATION_MODAL);
        VistaPrevia vista = new VistaPrevia();
        vista.setHtml(html);

        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.addActionListener(e -> vista.imprimir(horizontal));
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnImprimir);
        botones.add(btnCerrar);

        setLayout(new BorderLayout());
        add(vista, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        setSize(horizontal ? 1150 : 850, 720);
        setLocationRelativeTo(padre);
    }
}
