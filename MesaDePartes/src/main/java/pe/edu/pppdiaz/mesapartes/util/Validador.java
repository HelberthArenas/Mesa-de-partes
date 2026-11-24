package pe.edu.pppdiaz.mesapartes.util;

import java.util.regex.Pattern;

/** Validaciones de formularios (DNI, RUC, teléfono, correo, campos obligatorios). */
public final class Validador {

    private static final Pattern DNI = Pattern.compile("\\d{8}");
    private static final Pattern RUC = Pattern.compile("(10|15|17|20)\\d{9}");
    private static final Pattern TELEFONO = Pattern.compile("\\d{6,12}");
    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Validador() { }

    public static boolean esDni(String s) { return s != null && DNI.matcher(s.trim()).matches(); }

    public static boolean esRuc(String s) { return s != null && RUC.matcher(s.trim()).matches(); }

    public static boolean esTelefono(String s) { return s != null && TELEFONO.matcher(s.trim()).matches(); }

    public static boolean esCorreo(String s) { return s != null && CORREO.matcher(s.trim()).matches(); }

    public static boolean vacio(String s) { return s == null || s.isBlank(); }

    /**
     * Valida todo el formulario de registro.
     * @return mensaje del primer error encontrado, o {@code null} si todo es correcto.
     */
    public static String validarRegistro(String tipoDoc, String documento, String nombre,
                                         String telefono, String correo, String asunto) {
        if ("RUC".equals(tipoDoc)) {
            if (!esRuc(documento)) return "El RUC debe tener 11 dígitos y empezar con 10, 15, 17 o 20.";
        } else {
            if (!esDni(documento)) return "El DNI debe tener exactamente 8 dígitos numéricos.";
        }
        if (vacio(nombre) || nombre.trim().length() < 3) return "Ingrese el nombre o razón social del remitente.";
        if (!vacio(telefono) && !esTelefono(telefono)) return "El teléfono debe tener entre 6 y 12 dígitos.";
        if (!vacio(correo) && !esCorreo(correo)) return "El correo electrónico no tiene un formato válido.";
        if (vacio(asunto) || asunto.trim().length() < 5) return "Ingrese el asunto del documento (mínimo 5 caracteres).";
        return null;
    }
}
