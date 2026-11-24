package pe.edu.pppdiaz.mesapartes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import pe.edu.pppdiaz.mesapartes.util.Seguridad;
import pe.edu.pppdiaz.mesapartes.util.Validador;

class ValidadorTest {

    @Test
    void dniValidoTiene8Digitos() {
        assertTrue(Validador.esDni("12345678"));
        assertFalse(Validador.esDni("1234567"));
        assertFalse(Validador.esDni("1234567A"));
        assertFalse(Validador.esDni(null));
    }

    @Test
    void rucValidoTiene11DigitosYPrefijo() {
        assertTrue(Validador.esRuc("20123456789"));
        assertFalse(Validador.esRuc("30123456789"));
        assertFalse(Validador.esRuc("2012345678"));
    }

    @Test
    void correoYTelefono() {
        assertTrue(Validador.esCorreo("mesa@iespppd.edu.pe"));
        assertFalse(Validador.esCorreo("mesa@"));
        assertTrue(Validador.esTelefono("959123456"));
        assertFalse(Validador.esTelefono("12-34"));
    }

    @Test
    void formularioCompletoSinErrores() {
        assertNull(Validador.validarRegistro("DNI", "45678912", "Juan Perez", "959123456",
                "juan@correo.com", "Solicitud de constancia de estudios"));
    }

    @Test
    void formularioConErrores() {
        assertNotNull(Validador.validarRegistro("DNI", "123", "Juan Perez", "", "", "Solicitud de constancia"));
        assertNotNull(Validador.validarRegistro("DNI", "45678912", "", "", "", "Solicitud de constancia"));
        assertNotNull(Validador.validarRegistro("DNI", "45678912", "Juan Perez", "", "", "ab"));
        assertNotNull(Validador.validarRegistro("RUC", "45678912", "Empresa SAC", "", "", "Oficio de invitación"));
    }

    @Test
    void sha256EsDeterminista() {
        assertEquals(Seguridad.sha256("admin123"), Seguridad.sha256("admin123"));
        assertEquals(64, Seguridad.sha256("x").length());
    }
}
