package pe.edu.pppdiaz.mesapartes.modelo;

/** Usuario del sistema (personal de Mesa de Partes). */
public class Usuario extends EntidadBase {

    private String usuario;
    private String nombre;

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
