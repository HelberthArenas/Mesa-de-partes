package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import pe.edu.pppdiaz.mesapartes.modelo.Estado;
import pe.edu.pppdiaz.mesapartes.util.Fechas;

/** Colores, fuentes y utilidades visuales compartidas por todas las ventanas. */
public final class Estilo {

    public static final Color AZUL = new Color(0x1F3A93);
    public static final Color AZUL_CLARO = new Color(0xE8EEF9);
    public static final Color GRIS = new Color(0x5F6B7A);
    public static final Font TITULO = new Font("SansSerif", Font.BOLD, 20);
    public static final Font SUBTITULO = new Font("SansSerif", Font.PLAIN, 12);

    private Estilo() { }

    public static JPanel encabezado(String titulo, String subtitulo) {
        return encabezado(titulo, subtitulo, null);
    }

    /** Encabezado azul con título, subtítulo y (opcional) un ícono alineado a la derecha. */
    public static JPanel encabezado(String titulo, String subtitulo, Icon icono) {
        JPanel p = new JPanel(new java.awt.BorderLayout());
        p.setBackground(AZUL);
        p.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel t = new JLabel(titulo);
        t.setFont(TITULO);
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel(subtitulo);
        s.setFont(SUBTITULO);
        s.setForeground(new Color(0xD6E0F5));
        JPanel textos = new JPanel(new java.awt.GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(t);
        textos.add(s);
        JPanel centrado = new JPanel(new GridBagLayout());
        centrado.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.WEST;
        centrado.add(textos, gc);
        p.add(centrado, java.awt.BorderLayout.CENTER);
        if (icono != null) {
            p.add(new JLabel(icono), java.awt.BorderLayout.EAST);
        }
        return p;
    }

    /** Logo de la carrera (recurso /img/Logo.png) escalado a la altura indicada; null si no existe. */
    public static Icon logo(int alto) {
        java.net.URL url = pe.edu.pppdiaz.mesapartes.util.Recursos.logoUrl();
        if (url == null) {
            return null;
        }
        try (java.io.InputStream in = url.openStream()) {
            java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(in);
            int ancho = Math.max(1, original.getWidth() * alto / original.getHeight());
            java.awt.Image escalada = original.getScaledInstance(ancho, alto, java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(escalada);
        } catch (java.io.IOException e) {
            return null;
        }
    }

    /** Agrega "etiqueta + componente" en una fila de un formulario GridBagLayout. */
    public static void fila(JPanel panel, String etiqueta, JComponent campo, int fila) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.gridy = fila;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        JLabel l = new JLabel(etiqueta);
        panel.add(l, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(campo, c);
    }

    public static JPanel formulario(String titulo) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        return p;
    }

    public static JSpinner spinnerFecha(LocalDate inicial) {
        JSpinner sp = new JSpinner(new SpinnerDateModel(Fechas.aDate(inicial), null, null, java.util.Calendar.DAY_OF_MONTH));
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        return sp;
    }

    public static LocalDate valorFecha(JSpinner sp) {
        return Fechas.aLocalDate((Date) sp.getValue());
    }

    public static void aplicarTabla(JTable t) {
        t.setRowHeight(24);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        t.setGridColor(new Color(0xD9DEE7));
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setFont(h.getFont().deriveFont(Font.BOLD));
    }

    /** Tabla de expedientes: anchos de columna y colores por estado. */
    public static void aplicarTablaExpedientes(JTable t) {
        aplicarTabla(t);
        int[] anchos = {125, 125, 190, 100, 80, 200, 50, 90, 140};
        for (int i = 0; i < anchos.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        t.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean sel,
                                                           boolean foco, int fila, int col) {
                Component c = super.getTableCellRendererComponent(tabla, valor, sel, foco, fila, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    String v = String.valueOf(valor);
                    if (Estado.PENDIENTE.equals(v)) c.setForeground(new Color(0xB45309));
                    else if (Estado.EN_PROCESO.equals(v)) c.setForeground(new Color(0x1D4ED8));
                    else if (Estado.ATENDIDO.equals(v)) c.setForeground(new Color(0x15803D));
                    else c.setForeground(new Color(0x6B7280));
                }
                return c;
            }
        });
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}
