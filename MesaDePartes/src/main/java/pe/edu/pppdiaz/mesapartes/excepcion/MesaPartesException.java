package pe.edu.pppdiaz.mesapartes.excepcion;

/** Excepción propia del sistema: envuelve los errores de base de datos y de reglas de negocio. */
public class MesaPartesException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Mensaje mostrado cuando otro programa tiene bloqueado el archivo de la base de datos. */
    public static final String MENSAJE_BLOQUEO =
            "La base de datos está en uso por otro programa (por ejemplo DB Browser for SQLite).\n\n"
            + "Cierre ese programa, o presione \"Write Changes\" / \"Revert Changes\" en él, e intente de nuevo.";

    public MesaPartesException(String mensaje) {
        super(mensaje);
    }

    public MesaPartesException(String mensaje, Throwable causa) {
        super(esBloqueo(causa) ? MENSAJE_BLOQUEO : mensaje, causa);
    }

    /** Indica si el error (o alguna de sus causas) es un bloqueo de la base de datos SQLite. */
    public static boolean esBloqueo(Throwable t) {
        while (t != null) {
            String m = t.getMessage();
            if (m != null && (m.contains("SQLITE_BUSY") || m.contains("SQLITE_LOCKED")
                    || m.contains("database is locked") || m.contains("database table is locked"))) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
