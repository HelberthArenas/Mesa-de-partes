package pe.edu.pppdiaz.mesapartes.modelo;

import java.time.LocalDateTime;

/** Registro del historial de un expediente (registro, derivación, cambio de estado). */
public class Movimiento extends EntidadBase {

    private long expedienteId;
    private LocalDateTime fecha;
    private String tipo;
    private String detalle;
    private String usuario;

    public long getExpedienteId() { return expedienteId; }
    public void setExpedienteId(long expedienteId) { this.expedienteId = expedienteId; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
