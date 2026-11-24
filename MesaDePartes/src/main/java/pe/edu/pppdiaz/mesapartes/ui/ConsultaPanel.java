package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import pe.edu.pppdiaz.mesapartes.dao.ExpedienteDAO;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Estado;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;
import pe.edu.pppdiaz.mesapartes.modelo.Usuario;
import pe.edu.pppdiaz.mesapartes.util.DocumentoHtml;

/** Módulo de Consulta y Seguimiento: búsqueda por código, fecha o DNI */
public class ConsultaPanel extends JPanel implements Refrescable {

    private static final long serialVersionUID = 1L;

    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtDocumento = new JTextField(10);
    private final JCheckBox chkFecha = new JCheckBox("Fecha:");
    private final JSpinner spnFecha = Estilo.spinnerFecha(LocalDate.now());
    private final JComboBox<String> cboEstadoFiltro = new JComboBox<>();

    private final ExpedienteTableModel modelo = new ExpedienteTableModel();
    private final JTable tabla = new JTable(modelo);
    private final MovimientoTableModel modeloMov = new MovimientoTableModel();
    private final JTable tablaMov = new JTable(modeloMov);

    private final JLabel lblSeleccion = new JLabel("Seleccione un expediente para ver su historial.");
    private final JComboBox<String> cboNuevoEstado = new JComboBox<>(Estado.TODOS);
    private final JTextField txtObservacion = new JTextField(26);
    private final JButton btnEstado = new JButton("Actualizar estado");
    private final JButton btnComprobante = new JButton("Ver comprobante");
    private final JButton btnImprimirFicha = new JButton("Imprimir expediente");

    private final ExpedienteDAO expedienteDAO = new ExpedienteDAO();
    private final Usuario usuario;

    public ConsultaPanel(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout());
        add(Estilo.encabezado("Consulta y Seguimiento", "Búsqueda por Código, Fecha o DNI"),
                BorderLayout.NORTH);

        cboEstadoFiltro.addItem("Todos");
        for (String s : Estado.TODOS) cboEstadoFiltro.addItem(s);

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Código:"));
        filtro.add(txtCodigo);
        filtro.add(new JLabel("DNI:"));
        filtro.add(txtDocumento);
        filtro.add(chkFecha);
        filtro.add(spnFecha);
        filtro.add(new JLabel("Estado:"));
        filtro.add(cboEstadoFiltro);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscar());
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiar());
        filtro.add(btnBuscar);
        filtro.add(btnLimpiar);

        Estilo.aplicarTablaExpedientes(tabla);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostrarSeleccion();
        });
        JScrollPane scrollExp = new JScrollPane(tabla);
        scrollExp.setBorder(BorderFactory.createTitledBorder("Resultados"));

        Estilo.aplicarTabla(tablaMov);
        tablaMov.setAutoCreateRowSorter(false);
        tablaMov.getColumnModel().getColumn(0).setPreferredWidth(110);
        tablaMov.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablaMov.getColumnModel().getColumn(2).setPreferredWidth(480);
        tablaMov.getColumnModel().getColumn(3).setPreferredWidth(80);
        JScrollPane scrollMov = new JScrollPane(tablaMov);
        scrollMov.setBorder(BorderFactory.createTitledBorder("Historial del expediente"));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(lblSeleccion);
        acciones.add(new JLabel("  Nuevo estado:"));
        acciones.add(cboNuevoEstado);
        acciones.add(new JLabel("Obs.:"));
        acciones.add(txtObservacion);
        btnEstado.addActionListener(e -> cambiarEstado());
        btnComprobante.addActionListener(e -> verComprobante());
        acciones.add(btnEstado);
        acciones.add(btnComprobante);
        btnImprimirFicha.addActionListener(e -> imprimirFicha());
        acciones.add(btnImprimirFicha);
        btnEstado.setEnabled(false);
        btnComprobante.setEnabled(false);
        btnImprimirFicha.setEnabled(false);

        JPanel inferior = new JPanel(new BorderLayout());
        inferior.add(scrollMov, BorderLayout.CENTER);
        inferior.add(acciones, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollExp, inferior);
        split.setResizeWeight(0.55);
        split.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(filtro, BorderLayout.NORTH);
        centro.add(split, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
        buscar();
    }

    @Override
    public void refrescar() {
        buscar();
    }

    private void limpiar() {
        txtCodigo.setText("");
        txtDocumento.setText("");
        chkFecha.setSelected(false);
        cboEstadoFiltro.setSelectedIndex(0);
        buscar();
    }

    private void buscar() {
        buscarYSeleccionar(-1);
    }

    private void buscarYSeleccionar(long idSeleccionar) {
        try {
            LocalDate fecha = chkFecha.isSelected() ? Estilo.valorFecha(spnFecha) : null;
            String estado = cboEstadoFiltro.getSelectedIndex() <= 0 ? null : (String) cboEstadoFiltro.getSelectedItem();
            modelo.setDatos(expedienteDAO.buscar(txtCodigo.getText(), txtDocumento.getText(), fecha, fecha, estado));
            if (idSeleccionar >= 0) {
                int i = modelo.indiceDe(idSeleccionar);
                if (i >= 0) {
                    int vista = tabla.convertRowIndexToView(i);
                    tabla.getSelectionModel().setSelectionInterval(vista, vista);
                    return;
                }
            }
            mostrarSeleccion();
        } catch (MesaPartesException e) {
            Estilo.error(this, e.getMessage());
        }
    }

    private Expediente seleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : modelo.get(tabla.convertRowIndexToModel(fila));
    }

    private void mostrarSeleccion() {
        Expediente e = seleccionado();
        if (e == null) {
            lblSeleccion.setText("Seleccione un expediente para ver su historial.");
            modeloMov.setDatos(null);
            btnEstado.setEnabled(false);
            btnComprobante.setEnabled(false);
            btnImprimirFicha.setEnabled(false);
            return;
        }
        lblSeleccion.setText(e.getCodigo());
        cboNuevoEstado.setSelectedItem(e.getEstado());
        btnEstado.setEnabled(true);
        btnComprobante.setEnabled(true);
        btnImprimirFicha.setEnabled(true);
        try {
            modeloMov.setDatos(expedienteDAO.movimientos(e.getId()));
        } catch (MesaPartesException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void cambiarEstado() {
        Expediente e = seleccionado();
        if (e == null) return;
        String nuevo = (String) cboNuevoEstado.getSelectedItem();
        if (nuevo.equals(e.getEstado())) {
            Estilo.aviso(this, "El expediente ya tiene el estado \"" + nuevo + "\".");
            return;
        }
        try {
            expedienteDAO.cambiarEstado(e.getId(), nuevo, txtObservacion.getText(), usuario.getUsuario());
            txtObservacion.setText("");
            buscarYSeleccionar(e.getId());
        } catch (MesaPartesException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void imprimirFicha() {
        Expediente e = seleccionado();
        if (e == null) return;
        try {
            String html = DocumentoHtml.ficha(e, expedienteDAO.movimientos(e.getId()), usuario.getNombre());
            new ImpresionDialog(SwingUtilities.getWindowAncestor(this),
                    "Impresión del expediente " + e.getCodigo(), html, false).setVisible(true);
        } catch (MesaPartesException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void verComprobante() {
        Expediente e = seleccionado();
        if (e != null) {
            new ComprobanteDialog(SwingUtilities.getWindowAncestor(this), e).setVisible(true);
        }
    }
}
