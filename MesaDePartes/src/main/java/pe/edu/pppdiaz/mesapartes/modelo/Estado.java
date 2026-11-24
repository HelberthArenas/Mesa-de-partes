package pe.edu.pppdiaz.mesapartes.modelo;

/** Estados posibles de un expediente. */
public final class Estado {

    public static final String PENDIENTE = "Pendiente";
    public static final String EN_PROCESO = "En Proceso";
    public static final String ATENDIDO = "Atendido";
    public static final String ARCHIVADO = "Archivado";

    public static final String[] TODOS = { PENDIENTE, EN_PROCESO, ATENDIDO, ARCHIVADO };

    private Estado() { }

    /** Un expediente se puede derivar solo si sigue activo. */
    public static boolean esDerivable(String estado) {
        return PENDIENTE.equals(estado) || EN_PROCESO.equals(estado);
    }
}
