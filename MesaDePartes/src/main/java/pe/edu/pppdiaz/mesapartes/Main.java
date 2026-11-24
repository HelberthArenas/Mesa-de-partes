package pe.edu.pppdiaz.mesapartes;

import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import pe.edu.pppdiaz.mesapartes.db.Conexion;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.ui.LoginFrame;

/**
 * Sistema de Gestión y Seguimiento de Documentos para Mesa de Partes e Incidencias.
 * IESP "Pedro P. Díaz" - EFSRT - Desarrollo de Sistemas de Información.
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.put("nimbusBase", new Color(0x1F3A93));
            UIManager.put("nimbusFocus", new Color(0x4C6FD6));
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignorada) {
            // si Nimbus no está disponible se usa el aspecto por defecto
        }

        try {
            Conexion.inicializar();
        } catch (Exception e) {
            String mensaje = MesaPartesException.esBloqueo(e)
                    ? MesaPartesException.MENSAJE_BLOQUEO
                    : "No se pudo iniciar la base de datos:\n" + e.getMessage()
                      + "\n\nVerifique que el driver sqlite-jdbc esté en las dependencias del proyecto.";
            JOptionPane.showMessageDialog(null, mensaje, "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
