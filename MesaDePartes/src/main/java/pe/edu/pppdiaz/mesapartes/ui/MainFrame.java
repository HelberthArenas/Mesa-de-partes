package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyVetoException;
import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import pe.edu.pppdiaz.mesapartes.db.Respaldo;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Usuario;
import pe.edu.pppdiaz.mesapartes.util.Fechas;

/**
 * Ventana principal (MDI): cada módulo se abre como una ventana interna dentro del escritorio.
 * Menú: Expediente | Reportes | Backup | Ayuda.
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JDesktopPane escritorio = new JDesktopPane();
    private final Map<String, JInternalFrame> ventanas = new HashMap<>();
    private final Usuario usuario;

    public MainFrame(Usuario usuario) {
        super("Sistema de Gestión y Seguimiento de Documentos - Mesa de Partes");
        this.usuario = usuario;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                salir();
            }
        });

        setJMenuBar(crearMenu());
        escritorio.setBackground(Estilo.AZUL_CLARO);
        add(escritorio, BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(1000, 650));
        setSize(1200, 760);
        setLocationRelativeTo(null);

        abrirInicio();
    }

    // ---------------------------------------------------------------- Menú

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu mExpediente = new JMenu("Expediente");
        mExpediente.add(item("Registrar expediente", this::abrirRegistro));
        mExpediente.add(item("Derivar expediente", this::abrirDerivacion));
        mExpediente.add(item("Seguimiento expediente", this::abrirConsulta));
        mExpediente.add(item("Eliminar expediente", this::abrirEliminar));
        mExpediente.addSeparator();
        mExpediente.add(item("Salir", this::salir));

        JMenu mReportes = new JMenu("Reportes");
        mReportes.add(item("Rango de fechas y estado", this::abrirReportes));

        JMenu mBackup = new JMenu("Backup");
        mBackup.add(item("Base de datos", this::copiaSeguridad));

        JMenu mAyuda = new JMenu("Ayuda");
        mAyuda.add(item("Acerca de", this::acercaDe));

        barra.add(mExpediente);
        barra.add(mReportes);
        barra.add(mBackup);
        barra.add(mAyuda);
        return barra;
    }

    private JMenuItem item(String texto, Runnable accion) {
        JMenuItem it = new JMenuItem(texto);
        it.addActionListener(e -> accion.run());
        return it;
    }

    private JPanel crearBarraEstado() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, java.awt.Color.LIGHT_GRAY));
        p.add(new JLabel("Usuario: " + usuario.getNombre() + " (" + usuario.getUsuario() + ")"));
        p.add(new JLabel("   |   Fecha: " + LocalDate.now().format(Fechas.VISTA_DIA)));
        return p;
    }

    // ---------------------------------------------------------------- Ventanas internas

    private void abrirInicio() {
        JInternalFrame f = abrir("inicio", "Resumen", () -> new DashboardPanel(usuario), 760, 380);
        f.setClosable(false); // el Resumen no se cierra (solo se minimiza), así siempre se puede volver a él
    }

    private void abrirRegistro()   { abrir("registro", "Registro de Expedientes", () -> new RegistroPanel(usuario), 960, 640); }
    private void abrirDerivacion() { abrir("derivacion", "Derivación Interna", () -> new DerivacionPanel(usuario), 1000, 600); }
    private void abrirConsulta()   { abrir("consulta", "Consulta y Seguimiento", () -> new ConsultaPanel(usuario), 1060, 660); }
    private void abrirEliminar()   { abrir("eliminar", "Eliminar Expediente", () -> new EliminarPanel(), 1000, 560); }

    private void abrirReportes() {
        abrir("reportes", "Reportes e Histórico", () -> new ReportesPanel(usuario), 1100, 650);
    }

    private JInternalFrame abrir(String clave, String titulo, Supplier<JPanel> fabrica, int ancho, int alto) {
        JInternalFrame f = ventanas.get(clave);
        if (f == null || f.isClosed()) {
            f = new JInternalFrame(titulo, true, true, true, true);
            f.setContentPane(fabrica.get());
            f.setSize(ancho, alto);
            int desplazamiento = (ventanas.size() * 28) % 140;
            f.setLocation(16 + desplazamiento, 8 + desplazamiento);
            escritorio.add(f);
            ventanas.put(clave, f);
            f.setVisible(true);
        } else if (f.getContentPane() instanceof Refrescable) {
            ((Refrescable) f.getContentPane()).refrescar();
        }
        try {
            if (f.isIcon()) {
                f.setIcon(false);
            }
            f.toFront();
            f.setSelected(true);
        } catch (PropertyVetoException ignorada) {
            // la ventana no acepta el cambio de estado; no es un error
        }
        return f;
    }

    // ---------------------------------------------------------------- Acciones

    /** Backup → Copia de seguridad BD: guarda una copia completa de la base de datos donde elija el usuario. */
    private void copiaSeguridad() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar copia de seguridad de la base de datos");
        fc.setFileFilter(new FileNameExtensionFilter("Base de datos SQLite (*.db)", "db"));
        fc.setSelectedFile(new File(Respaldo.nombreSugerido()));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = fc.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".db")) {
            destino = new File(destino.getPath() + ".db");
        }
        if (destino.exists()) {
            int r = JOptionPane.showConfirmDialog(this, "El archivo ya existe. ¿Desea reemplazarlo?",
                    "Copia de seguridad", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            Respaldo.crear(destino);
            Estilo.info(this, "Copia de seguridad creada correctamente.\n\nArchivo: " + destino.getAbsolutePath()
                    + "\nTamaño: " + Math.max(1, destino.length() / 1024) + " KB");
        } catch (MesaPartesException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void salir() {
        int r = JOptionPane.showConfirmDialog(this, "¿Desea salir del sistema?", "Salir",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void acercaDe() {
        JOptionPane.showMessageDialog(this,
                "Sistema de Gestión y Seguimiento de Documentos\n"
                + "Mesa de Partes\n"
                + "I.E.S. Pedro P. Díaz \n"
                + "Programa de Estudios: Desarrollo de Sistemas de Información\n"
                + "EFSRT - Módulo: 01\n"
                + "Supervisor: Ing. Helberth Arenas Paredes\n\n"
                +  "Versión 1.0",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }
}
