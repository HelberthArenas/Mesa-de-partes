package pe.edu.pppdiaz.mesapartes.modelo;

/** Clase base de todas las entidades persistentes (herencia). */
public abstract class EntidadBase {

    private long id;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
}
