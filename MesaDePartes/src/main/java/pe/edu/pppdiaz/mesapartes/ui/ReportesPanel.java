package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import pe.edu.pppdiaz.mesapartes.dao.ExpedienteDAO;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Estado;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;
import pe.edu.pppdiaz.mesapartes.modelo.Usuario;
import pe.edu.pppdiaz.mesapartes.util.Csv;
import pe.edu.pppdiaz.mesapartes.util.DocumentoHtml;
import pe.edu.pppdiaz.mesapartes.util.Fechas;

/**
 * Módulo de Reportes
 */
public class ReportesPanel extends JPanel implements Refrescable {

    private static final long serialVersionUID = 1L;

    private final ExpedienteDAO dao = new ExpedienteDAO();
    private final Usuario usuario;

    private final JSpinner spnDesde = Estilo.spinnerFecha(LocalDate.now().withDayOfMonth(1));
    private final JSpinner spnHasta = Estilo.spinnerFecha(LocalDate.now());
    private final JComboBox<String> cboEstado = new JComboBox<>();

    private final ExpedienteTableModel modelo = new ExpedienteTableModel();
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblResumen = new JLabel(" ");
    private final JButton btnImprimir = new JButton("Imprimir listado");
    private final JButton btnCsv = new JButton("Exportar CSV");

    /** Listado mostrado actualmente (el que se imprime y se exporta). */
    private List<Expediente> lista = new ArrayList<>();
    private LocalDate desdeGenerado = LocalDate.now();
    private LocalDate hastaGenerado = LocalDate.now();
    private String estadoGenerado;

    public ReportesPanel(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout());
        add(Estilo.encabezado("Reportes",
                "Rango de fechas y estado del documento", Estilo.logo(60)),
                BorderLayout.NORTH);

        cboEstado.addItem("Todos");
        for (String s : Estado.TODOS) {
            cboEstado.addItem(s);
        }

        // --- Fila 1: filtros
        JPanel filaFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaFiltros.add(new JLabel("Desde:"));
        filaFiltros.add(spnDesde);
        filaFiltros.add(new JLabel("Hasta:"));
        filaFiltros.add(spnHasta);
        filaFiltros.add(new JLabel("Estado del documento:"));
        filaFiltros.add(cboEstado);
        JButton btnGenerar = new JButton("Generar listado");
        btnGenerar.addActionListener(e -> generar());
        filaFiltros.add(btnGenerar);

        // --- Fila 2: rangos rápidos y acciones
        JPanel filaAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaAcciones.add(new JLabel("Rango rápido:"));
        JButton btnHoy = new JButton("Hoy");
        btnHoy.addActionListener(e -> aplicarRango(LocalDate.now(), LocalDate.now()));
        JButton btnSemana = new JButton("Esta semana");
        btnSemana.addActionListener(e -> {
            LocalDate lunes = DocumentoHtml.lunesDe(LocalDate.now());
            aplicarRango(lunes, lunes.plusDays(6));
        });
        JButton btnMes = new JButton("Este mes");
        btnMes.addActionListener(e -> {
            LocalDate hoy = LocalDate.now();
            aplicarRango(hoy.withDayOfMonth(1), hoy.withDayOfMonth(hoy.lengthOfMonth()));
        });
        filaAcciones.add(btnHoy);
        filaAcciones.add(btnSemana);
        filaAcciones.add(btnMes);
        filaAcciones.add(new JLabel("      "));
        btnImprimir.addActionListener(e -> imprimir());
        btnCsv.addActionListener(e -> exportarCsv());
        filaAcciones.add(btnImprimir);
        filaAcciones.add(btnCsv);

        JPanel filtros = new JPanel();
        filtros.setLayout(new BoxLayout(filtros, BoxLayout.Y_AXIS));
        filtros.add(filaFiltros);
        filtros.add(filaAcciones);

        Estilo.aplicarTablaExpedientes(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Listado de trámites"));

        lblResumen.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        centro.add(filtros, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
        add(lblResumen, BorderLayout.SOUTH);

        generar();
    }

    @Override
    public void refrescar() {
        generar();
    }

    private void aplicarRango(LocalDate desde, LocalDate hasta) {
        spnDesde.setValue(Fechas.aDate(desde));
        spnHasta.setValue(Fechas.aDate(hasta));
        generar();
    }

    /** Genera el listado con el rango de fechas y el estado elegidos. */
    private void generar() {
        LocalDate desde = Estilo.valorFecha(spnDesde);
        LocalDate hasta = Estilo.valorFecha(spnHasta);
        if (desde.isAfter(hasta)) {
            Estilo.aviso(this, "La fecha \"Desde\" no puede ser posterior a la fecha \"Hasta\".");
            return;
        }
        String estado = cboEstado.getSelectedIndex() <= 0 ? null : (String) cboEstado.getSelectedItem();
        try {
            lista = dao.buscar(null, null, desde, hasta, estado);
            desdeGenerado = desde;
            hastaGenerado = hasta;
            estadoGenerado = estado;
            modelo.setDatos(lista);
            mostrarResumen();
        } catch (MesaPartesException e) {
            Estilo.error(this, e.getMessage());
        }
    }

    private void mostrarResumen() {
        int[] porEstado = new int[Estado.TODOS.length];
        int folios = 0;
        for (Expediente e : lista) {
            folios += e.getFolios();
            for (int i = 0; i < Estado.TODOS.length; i++) {
                if (Estado.TODOS[i].equals(e.getEstado())) {
                    porEstado[i]++;
                }
            }
        }
        StringBuilder sb = new StringBuilder("Del " + desdeGenerado.format(Fechas.VISTA_DIA)
                + " al " + hastaGenerado.format(Fechas.VISTA_DIA) + "   |   Total: " + lista.size() + " trámite(s)");
        for (int i = 0; i < Estado.TODOS.length; i++) {
            sb.append("   |   ").append(Estado.TODOS[i]).append(": ").append(porEstado[i]);
        }
        sb.append("   |   Folios: ").append(folios);
        lblResumen.setText(sb.toString());
    }

    /** Abre la vista previa del listado (A4 horizontal) con la opción de imprimir. */
    private void imprimir() {
        if (lista.isEmpty()) {
            Estilo.aviso(this, "No hay trámites para imprimir.");
            return;
        }
        String html = DocumentoHtml.reporteListado(desdeGenerado, hastaGenerado, lista, estadoGenerado,
                usuario.getNombre());
        new ImpresionDialog(SwingUtilities.getWindowAncestor(this), "Listado de trámites", html, true)
                .setVisible(true);
    }

    private void exportarCsv() {
        if (lista.isEmpty()) {
            Estilo.aviso(this, "No hay trámites para exportar. ");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("listado_tramites_" + desdeGenerado + "_" + hastaGenerado + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = fc.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getPath() + ".csv");
        }
        try {
            Csv.exportar(archivo, lista);
            Estilo.info(this, "Listado exportado en:\n" + archivo.getAbsolutePath());
        } catch (IOException ex) {
            Estilo.error(this, "No se pudo guardar el archivo: " + ex.getMessage());
        }
    }
}
