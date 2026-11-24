package pe.edu.pppdiaz.mesapartes.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import pe.edu.pppdiaz.mesapartes.dao.UsuarioDAO;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Usuario;

/** Ventana de inicio de sesión. Usuario inicial: admin  |  Contraseña: 975114*/
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private final JTextField txtUsuario = new JTextField(16);
    private final JPasswordField txtClave = new JPasswordField(16);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public LoginFrame() {
        super("Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = Estilo.formulario("Credenciales");
        Estilo.fila(form, "Usuario:", txtUsuario, 0);
        Estilo.fila(form, "Contraseña:", txtClave, 1);

        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.addActionListener(e -> ingresar());
        JButton btnSalir = new JButton("Salir");
        btnSalir.addActionListener(e -> System.exit(0));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnIngresar);
        botones.add(btnSalir);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        centro.add(form, BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(botones, BorderLayout.SOUTH);
        centro.add(sur, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(Estilo.encabezado("Mesa de Partes", "I.E.S. \"Pedro P. Díaz\""),
                BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        getRootPane().setDefaultButton(btnIngresar);
        setMinimumSize(new Dimension(460, 300));
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        String usuario = txtUsuario.getText();
        String clave = new String(txtClave.getPassword());
        if (usuario.isBlank() || clave.isEmpty()) {
            Estilo.aviso(this, "Ingrese usuario y contraseña.");
            return;
        }
        try {
            Usuario u = usuarioDAO.autenticar(usuario, clave);
            if (u == null) {
                Estilo.error(this, "Usuario o contraseña incorrectos.");
                txtClave.setText("");
                txtClave.requestFocus();
                return;
            }
            dispose();
            new MainFrame(u).setVisible(true);
        } catch (MesaPartesException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }
}
