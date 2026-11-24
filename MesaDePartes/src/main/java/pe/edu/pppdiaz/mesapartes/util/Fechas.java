package pe.edu.pppdiaz.mesapartes.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/** Formatos y conversiones de fecha usados en todo el sistema. */
public final class Fechas {

    /** Formato guardado en la base de datos. */
    public static final DateTimeFormatter BD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** Formato mostrado al usuario. */
    public static final DateTimeFormatter VISTA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter VISTA_DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Fechas() { }

    public static String paraVista(LocalDateTime f) { return f == null ? "" : f.format(VISTA); }

    public static Date aDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate aLocalDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
