package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import pe.edu.pppdiaz.mesapartes.dao.ExpedienteDAO;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;

/** Módulo Eliminar expediente: busca, confirma y elimina el expediente junto con su historial. */
public class EliminarPanel extends JPanel implements Refrescable {

    private static final long serialVersionUID = 1L;

    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtDocumento = new JTextField(10);
    private final ExpedienteTableModel modelo = new ExpedienteTableModel();
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblSeleccion = new JLabel("Seleccione el expediente que desea eliminar.");
    private final JButton btnEliminar = new JButton("Eliminar expediente");
    private final ExpedienteDAO dao = new ExpedienteDAO();

    public EliminarPanel() {
        setLayout(new BorderLayout());
        add(Estilo.encabezado("Eliminar Expediente", "Busque el expediente y elimínelo junto con su historial"),
                BorderLayout.NORTH);

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Código:"));
        filtro.add(txtCodigo);
        filtro.add(new JLabel("DNI:"));
        filtro.add(txtDocumento);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscar());
        JButton btnTodos = new JButton("Ver todos");
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
        scroll.setBorder(BorderFactory.createTitledBorder("Expedientes registrados"));

        JLabel advertencia = new JLabel("<html><b>Atención:</b> al eliminar un expediente también se borra su historial "
                + "y la acción <b>no se puede deshacer</b>. </html>");
        advertencia.setForeground(new Color(0x9B1C1C));
        btnEliminar.setEnabled(false);
        btnEliminar.addActionListener(e -> eliminar());

        JPanel accion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accion.add(lblSeleccion);
        accion.add(btnEliminar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        sur.add(advertencia, BorderLayout.NORTH);
        sur.add(accion, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        centro.add(filtro, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        buscar();
    }

    @Override
    public void refrescar() {
        buscar();
    }

    private void buscar() {
        try {
            modelo.setDatos(dao.buscar(txtCodigo.getText(), txtDocumento.getText(), null, null, null));
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
            lblSeleccion.setText("Seleccione el expediente que desea eliminar.");
            btnEliminar.setEnabled(false);
        } else {
            lblSeleccion.setText(e.getCodigo() + " — " + e.getRemitente().getNombre());
            btnEliminar.setEnabled(true);
        }
    }

    private void eliminar() {
        Expediente e = seleccionado();
        if (e == null) {
            return;
        }
        String mensaje = "¿Está seguro de eliminar este expediente?\n\n"
                + "Código: " + e.getCodigo() + "\n"
                + "Remitente: " + e.getRemitente().getNombre() + " (" + e.getRemitente().getDocumento() + ")\n"
                + "Asunto: " + e.getAsunto() + "\n"
                + "Estado: " + e.getEstado() + "\n\n"
                + "Se eliminará también de la base de datos.";
        Object[] opciones = {"Eliminar", "Cancelar"};
        int r = JOptionPane.showOptionDialog(this, mensaje, "Confirmar eliminación",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, opciones, opciones[1]);
        if (r != 0) {
            return;
        }
        try {
            dao.eliminar(e.getId());
            Estilo.info(this, "El expediente " + e.getCodigo() + " fue eliminado.");
            buscar();
        } catch (MesaPartesException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }
}
