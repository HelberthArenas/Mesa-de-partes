package pe.edu.pppdiaz.mesapartes.modelo;

import java.time.LocalDateTime;

/** Expediente o trámite documentario registrado en Mesa de Partes. */
public class Expediente extends EntidadBase {

    public static final String UBICACION_INICIAL = "Mesa de Partes";

    private String codigo;
    private Remitente remitente;
    private String tipoDocumento;
    private String asunto;
    private int folios = 1;
    private LocalDateTime fechaRegistro;
    private String estado = Estado.PENDIENTE;
    private Long dependenciaActualId;
    private String dependenciaActual;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Remitente getRemitente() { return remitente; }
    public void setRemitente(Remitente remitente) { this.remitente = remitente; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public int getFolios() { return folios; }
    public void setFolios(int folios) { this.folios = folios; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Long getDependenciaActualId() { return dependenciaActualId; }
    public void setDependenciaActualId(Long dependenciaActualId) { this.dependenciaActualId = dependenciaActualId; }

    /** Nombre de la dependencia donde se encuentra el expediente (o "Mesa de Partes"). */
    public String getDependenciaActual() {
        return dependenciaActual == null ? UBICACION_INICIAL : dependenciaActual;
    }
    public void setDependenciaActual(String dependenciaActual) { this.dependenciaActual = dependenciaActual; }
}
