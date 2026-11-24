package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import pe.edu.pppdiaz.mesapartes.dao.DependenciaDAO;
import pe.edu.pppdiaz.mesapartes.dao.ExpedienteDAO;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Dependencia;
import pe.edu.pppdiaz.mesapartes.modelo.Estado;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;
import pe.edu.pppdiaz.mesapartes.modelo.Usuario;

/** Módulo de Derivación Interna hacia Personal, Biblioteca, Caja, Dirección. */
public class DerivacionPanel extends JPanel implements Refrescable {

    private static final long serialVersionUID = 1L;

    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtDocumento = new JTextField(10);
    private final ExpedienteTableModel modelo = new ExpedienteTableModel();
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblSeleccion = new JLabel("Seleccione un expediente de la lista.");
    private final JComboBox<Dependencia> cboDestino = new JComboBox<>();
    private final JTextArea txtObservacion = new JTextArea(3, 30);
    private final JButton btnDerivar = new JButton("Derivar expediente");

    private final ExpedienteDAO expedienteDAO = new ExpedienteDAO();
    private final DependenciaDAO dependenciaDAO = new DependenciaDAO();
    private final Usuario usuario;

    public DerivacionPanel(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout());
        add(Estilo.encabezado("Derivación Interna", "Envío de expedientes a las dependencias receptoras"),
                BorderLayout.NORTH);

        // --- Filtro
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Código:"));
        filtro.add(txtCodigo);
        filtro.add(new JLabel("DNI:"));
        filtro.add(txtDocumento);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscar());
        JButton btnTodos = new JButton("Ver todos los activos");
        btnTodos.addActionListener(e -> {
            txtCodigo.setText("");
            txtDocumento.setText("");
            buscar();
        });
        filtro.add(btnBuscar);
        filtro.add(btnTodos);

        Estilo.aplicarTablaExpedientes(tabla);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostrarSeleccion();
        });
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Expedientes pendientes o en proceso"));

        // --- Panel de derivación
        txtObservacion.setLineWrap(true);
        txtObservacion.setWrapStyleWord(true);
        JPanel form = Estilo.formulario("Derivar");
        Estilo.fila(form, "Expediente:", lblSeleccion, 0);
        Estilo.fila(form, "Dependencia destino:", cboDestino, 1);
        Estilo.fila(form, "Observación:", new JScrollPane(txtObservacion), 2);
        btnDerivar.setEnabled(false);
        btnDerivar.addActionListener(e -> derivar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnDerivar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        sur.add(form, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        centro.add(filtro, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        refrescar();
    }

    @Override
    public void refrescar() {
        try {
            cboDestino.removeAllItems();
            for (Dependencia d : dependenciaDAO.listar()) {
                cboDestino.addItem(d);
            }
        } catch (MesaPartesException e) {
            Estilo.error(this, e.getMessage());
        }
        buscar();
    }

    private void buscar() {
        try {
            List<Expediente> activos = new ArrayList<>();
            for (Expediente e : expedienteDAO.buscar(txtCodigo.getText(), txtDocumento.getText(), null, null, null)) {
                if (Estado.esDerivable(e.getEstado())) {
                    activos.add(e);
                }
            }
            modelo.setDatos(activos);
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
            lblSeleccion.setText("Seleccione un expediente de la lista.");
            btnDerivar.setEnabled(false);
        } else {
            lblSeleccion.setText(e.getCodigo() + " — " + e.getRemitente().getNombre()
                    + " (ubicación actual: " + e.getDependenciaActual() + ")");
            btnDerivar.setEnabled(true);
        }
    }

    private void derivar() {
        Expediente e = seleccionado();
        Dependencia destino = (Dependencia) cboDestino.getSelectedItem();
        if (e == null || destino == null) {
            Estilo.aviso(this, "Seleccione un expediente y la dependencia destino.");
            return;
        }
        if (e.getDependenciaActualId() != null && e.getDependenciaActualId() == destino.getId()) {
            Estilo.aviso(this, "El expediente ya se encuentra en " + destino.getNombre() + ".");
            return;
        }
        try {
            expedienteDAO.derivar(e.getId(), destino, txtObservacion.getText(), usuario.getUsuario());
            Estilo.info(this, "Expediente " + e.getCodigo() + " derivado a " + destino.getNombre() + ".");
            txtObservacion.setText("");
            buscar();
        } catch (MesaPartesException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }
}
