package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import pe.edu.pppdiaz.mesapartes.dao.ExpedienteDAO;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Estado;
import pe.edu.pppdiaz.mesapartes.modelo.Usuario;

/** Ventana de inicio: resumen de expedientes por estado. */
public class DashboardPanel extends JPanel implements Refrescable {

    private static final long serialVersionUID = 1L;
    private final ExpedienteDAO dao = new ExpedienteDAO();
    private final JPanel tarjetas = new JPanel(new GridLayout(1, 5, 12, 0));
    private final JLabel lblActualizado = new JLabel(" ");

    public DashboardPanel(Usuario usuario) {
        setLayout(new BorderLayout());
        add(Estilo.encabezado("Resumen de Mesa de Partes", "Bienvenido(a), " + usuario.getNombre(),
                Estilo.logo(76)), BorderLayout.NORTH);
        tarjetas.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));
        add(tarjetas, BorderLayout.CENTER);
        JLabel ayuda = new JLabel("<html>Use el menú <b>Expediente</b> para registrar, derivar, consultar y eliminar trámites.</html>");
        ayuda.setForeground(Estilo.GRIS);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setToolTipText("Vuelve a contar los expedientes por estado");
        btnActualizar.addActionListener(e -> refrescar());
        lblActualizado.setForeground(Estilo.GRIS);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.add(lblActualizado);
        derecha.add(btnActualizar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));
        sur.add(ayuda, BorderLayout.CENTER);
        sur.add(derecha, BorderLayout.EAST);
        add(sur, BorderLayout.SOUTH);
        refrescar();
    }

    @Override
    public void refrescar() {
        tarjetas.removeAll();
        try {
            Map<String, Integer> conteo = dao.contarPorEstado();
            int total = 0;
            for (int n : conteo.values()) total += n;
            tarjetas.add(tarjeta("Total", total, Estilo.AZUL));
            tarjetas.add(tarjeta(Estado.PENDIENTE, conteo.get(Estado.PENDIENTE), new Color(0xB45309)));
            tarjetas.add(tarjeta(Estado.EN_PROCESO, conteo.get(Estado.EN_PROCESO), new Color(0x1D4ED8)));
            tarjetas.add(tarjeta(Estado.ATENDIDO, conteo.get(Estado.ATENDIDO), new Color(0x15803D)));
            tarjetas.add(tarjeta(Estado.ARCHIVADO, conteo.get(Estado.ARCHIVADO), new Color(0x6B7280)));
        } catch (MesaPartesException e) {
            Estilo.error(this, e.getMessage());
        }
        lblActualizado.setText("Última actualización: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        tarjetas.revalidate();
        tarjetas.repaint();
    }

    private JPanel tarjeta(String titulo, int valor, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2), BorderFactory.createEmptyBorder(14, 8, 14, 8)));
        JLabel n = new JLabel(String.valueOf(valor), SwingConstants.CENTER);
        n.setFont(new Font("SansSerif", Font.BOLD, 40));
        n.setForeground(color);
        JLabel t = new JLabel(titulo, SwingConstants.CENTER);
        t.setFont(new Font("SansSerif", Font.BOLD, 14));
        p.add(n, BorderLayout.CENTER);
        p.add(t, BorderLayout.SOUTH);
        return p;
    }
}
