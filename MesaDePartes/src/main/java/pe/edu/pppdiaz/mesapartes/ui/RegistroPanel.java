package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import pe.edu.pppdiaz.mesapartes.dao.DependenciaDAO;
import pe.edu.pppdiaz.mesapartes.dao.ExpedienteDAO;
import pe.edu.pppdiaz.mesapartes.dao.RemitenteDAO;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Dependencia;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;
import pe.edu.pppdiaz.mesapartes.modelo.Remitente;
import pe.edu.pppdiaz.mesapartes.modelo.Usuario;
import pe.edu.pppdiaz.mesapartes.util.Validador;

/** Módulo de Registro de Expedientes. */
public class RegistroPanel extends JPanel implements Refrescable {

    private static final long serialVersionUID = 1L;
    private static final String SIN_DERIVAR = "— No derivar (queda en Mesa de Partes) —";

    private final JComboBox<String> cboTipoDoc = new JComboBox<>(new String[] {"DNI", "RUC"});
    private final JTextField txtDocumento = new JTextField(14);
    private final JTextField txtNombre = new JTextField(24);
    private final JTextField txtTelefono = new JTextField(14);
    private final JTextField txtCorreo = new JTextField(24);

    private final JComboBox<String> cboTipoDocumento =
            new JComboBox<>(new String[] {"Oficio", "Solicitud", "Carta", "Informe", "Otro"});
    private final JTextArea txtAsunto = new JTextArea(4, 24);
    private final JSpinner spnFolios = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JComboBox<Object> cboDestino = new JComboBox<>();

    private final ExpedienteTableModel modelo = new ExpedienteTableModel();
    private final JTable tabla = new JTable(modelo);

    private final ExpedienteDAO expedienteDAO = new ExpedienteDAO();
    private final RemitenteDAO remitenteDAO = new RemitenteDAO();
    private final DependenciaDAO dependenciaDAO = new DependenciaDAO();
    private final Usuario usuario;

    public RegistroPanel(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout());
        add(Estilo.encabezado("Registro de Expedientes", "Ingreso de información del documento"),
                BorderLayout.NORTH);

        // --- Datos del remitente
        JPanel pRemitente = Estilo.formulario("Datos del remitente");
        JPanel filaDoc = new JPanel(new BorderLayout(6, 0));
        filaDoc.add(cboTipoDoc, BorderLayout.WEST);
        filaDoc.add(txtDocumento, BorderLayout.CENTER);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setToolTipText("Busca si el remitente ya fue registrado antes");
        btnBuscar.addActionListener(e -> buscarRemitente());
        filaDoc.add(btnBuscar, BorderLayout.EAST);
        Estilo.fila(pRemitente, "DNI / RUC *:", filaDoc, 0);
        Estilo.fila(pRemitente, "Nombre / Razón social *:", txtNombre, 1);
        Estilo.fila(pRemitente, "Teléfono:", txtTelefono, 2);
        Estilo.fila(pRemitente, "Correo:", txtCorreo, 3);

        // --- Datos del documento
        txtAsunto.setLineWrap(true);
        txtAsunto.setWrapStyleWord(true);
        JPanel pDocumento = Estilo.formulario("Datos del documento");
        Estilo.fila(pDocumento, "Tipo de documento *:", cboTipoDocumento, 0);
        Estilo.fila(pDocumento, "Asunto *:", new JScrollPane(txtAsunto), 1);
        Estilo.fila(pDocumento, "N.º de folios *:", spnFolios, 2);
        Estilo.fila(pDocumento, "Derivar a:", cboDestino, 3);

        JPanel formularios = new JPanel(new GridLayout(1, 2, 10, 0));
        formularios.add(pRemitente);
        formularios.add(pDocumento);

        JButton btnRegistrar = new JButton("Registrar expediente");
        btnRegistrar.addActionListener(e -> registrar());
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnLimpiar);
        botones.add(btnRegistrar);

        JPanel superior = new JPanel(new BorderLayout());
        superior.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        superior.add(formularios, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        Estilo.aplicarTablaExpedientes(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Últimos expedientes registrados"));

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(superior, BorderLayout.NORTH);
        JPanel contTabla = new JPanel(new BorderLayout());
        contTabla.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        contTabla.add(scroll, BorderLayout.CENTER);
        centro.add(contTabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        refrescar();
    }

    @Override
    public void refrescar() {
        try {
            cboDestino.removeAllItems();
            cboDestino.addItem(SIN_DERIVAR);
            for (Dependencia d : dependenciaDAO.listar()) {
                cboDestino.addItem(d);
            }
            modelo.setDatos(expedienteDAO.ultimos(10));
        } catch (MesaPartesException e) {
            Estilo.error(this, e.getMessage());
        }
    }

    private void buscarRemitente() {
        String doc = txtDocumento.getText().trim();
        if (doc.isEmpty()) {
            Estilo.aviso(this, "Escriba el DNI a buscar.");
            return;
        }
        try {
            Remitente r = remitenteDAO.buscarPorDocumento(doc);
            if (r == null) {
                Estilo.info(this, "El remitente no está registrado. Complete sus datos manualmente.");
                return;
            }
            cboTipoDoc.setSelectedItem(r.getTipoDoc());
            txtNombre.setText(r.getNombre());
            txtTelefono.setText(r.getTelefono() == null ? "" : r.getTelefono());
            txtCorreo.setText(r.getCorreo() == null ? "" : r.getCorreo());
        } catch (MesaPartesException e) {
            Estilo.error(this, e.getMessage());
        }
    }

    private void registrar() {
        String tipoDoc = (String) cboTipoDoc.getSelectedItem();
        String error = Validador.validarRegistro(tipoDoc, txtDocumento.getText(), txtNombre.getText(),
                txtTelefono.getText(), txtCorreo.getText(), txtAsunto.getText());
        if (error != null) {
            Estilo.aviso(this, error);
            return;
        }
        Remitente r = new Remitente();
        r.setTipoDoc(tipoDoc);
        r.setDocumento(txtDocumento.getText().trim());
        r.setNombre(txtNombre.getText().trim());
        r.setTelefono(txtTelefono.getText().trim());
        r.setCorreo(txtCorreo.getText().trim());

        Expediente e = new Expediente();
        e.setRemitente(r);
        e.setTipoDocumento((String) cboTipoDocumento.getSelectedItem());
        e.setAsunto(txtAsunto.getText().trim());
        e.setFolios((Integer) spnFolios.getValue());

        try {
            Expediente registrado = expedienteDAO.registrar(e, usuario.getUsuario());
            Object destino = cboDestino.getSelectedItem();
            if (destino instanceof Dependencia) {
                expedienteDAO.derivar(registrado.getId(), (Dependencia) destino,
                        "Derivación inmediata al registrar", usuario.getUsuario());
                registrado = expedienteDAO.porId(registrado.getId());
            }
            Estilo.info(this, "Expediente registrado correctamente.\nCódigo de seguimiento: " + registrado.getCodigo());
            new ComprobanteDialog(SwingUtilities.getWindowAncestor(this), registrado).setVisible(true);
            limpiar();
            refrescar();
        } catch (MesaPartesException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void limpiar() {
        cboTipoDoc.setSelectedIndex(0);
        txtDocumento.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        cboTipoDocumento.setSelectedIndex(0);
        txtAsunto.setText("");
        spnFolios.setValue(1);
        if (cboDestino.getItemCount() > 0) cboDestino.setSelectedIndex(0);
        txtDocumento.requestFocus();
    }
}
