package pe.edu.pppdiaz.mesapartes.modelo;

/** Persona o institución que presenta un trámite (DNI o RUC). */
public class Remitente extends EntidadBase {

    private String tipoDoc = "DNI";
    private String documento;
    private String nombre;
    private String telefono;
    private String correo;

    public String getTipoDoc() { return tipoDoc; }
    public void setTipoDoc(String tipoDoc) { this.tipoDoc = tipoDoc; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
