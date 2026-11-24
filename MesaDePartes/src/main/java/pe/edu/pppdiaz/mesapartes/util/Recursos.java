package pe.edu.pppdiaz.mesapartes.util;

import java.net.URL;

/** Acceso a los recursos (imágenes) incluidos en el proyecto. */
public final class Recursos {

    private Recursos() { }

    /**
     * URL del logo del instituto, o {@code null} si no existe.
     * Acepta el archivo como "Logo.png" o "logo.png" dentro de src/main/resources/img.
     */
    public static URL logoUrl() {
        for (String nombre : new String[] {"/img/Logo.png", "/img/logo.png"}) {
            URL url = Recursos.class.getResource(nombre);
            if (url != null) {
                return url;
            }
        }
        return null;
    }
}
