package pe.edu.pppdiaz.mesapartes.modelo;

/** Dependencia receptora de expedientes (Personal, Biblioteca, Caja, Dirección...). */
public class Dependencia extends EntidadBase {

    private String nombre;

    public Dependencia() { }

    public Dependencia(long id, String nombre) {
        setId(id);
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() { return nombre; }
}
