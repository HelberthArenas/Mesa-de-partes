# Sistema de Gestión y Seguimiento de Documentos para Mesa de Partes e Incidencias

**IESP "Pedro P. Díaz" – Programa de Estudios: Desarrollo de Sistemas de Información**
EFSRT – Módulo Formativo: Programación de aplicaciones informáticas – 2.º semestre (turno noche)
Docente supervisor: Helberth Paul Arenas Paredes

Software de escritorio en **Java 17 + Swing + JDBC + SQLite** (proyecto **Maven**, listo para **Apache NetBeans**).

---

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| JDK (OpenJDK / Oracle) | **17 o superior** |
| Apache NetBeans | 17 o superior (cualquiera con soporte Maven) |
| Internet | Solo la **primera vez** (Maven descarga el driver SQLite y JUnit) |

No necesita instalar MySQL ni ningún servidor: la base de datos SQLite se crea sola.

## 2. Abrir y ejecutar en NetBeans

1. Descomprima `MesaDePartes_NetBeans.zip`.
2. En NetBeans: **Archivo → Abrir proyecto…** y seleccione la carpeta `MesaDePartes` (se reconoce por el ícono de Maven).
3. Espere a que NetBeans descargue las dependencias (barra de progreso inferior derecha).
4. Presione **F6** (Ejecutar proyecto) o clic derecho sobre el proyecto → **Run**.
5. Ingrese con **usuario `admin`** y **contraseña `admin123`**.

Si NetBeans pide elegir la clase principal, seleccione `pe.edu.pppdiaz.mesapartes.Main`.

**Generar el JAR ejecutable:** clic derecho → *Clean and Build* (Mayús+F11). Se crea `target/MesaDePartes-1.0.jar` con todo incluido; ejecútelo con doble clic o `java -jar target/MesaDePartes-1.0.jar`.

**Sin Maven / sin internet:** ejecute `ejecutar_sin_maven.bat` (Windows) o `ejecutar_sin_maven.sh` (Linux/Mac). Usa el driver incluido en la carpeta `lib/`.

## 3. Base de datos

- Archivo SQLite: `<carpeta del usuario>/.mesapartes/mesapartes.db` (se crea automáticamente con las tablas y datos iniciales).
- Para usar otra ruta: ejecutar con `-Dmesapartes.db=C:\ruta\mi_base.db`.
- Dependencias iniciales: Oficina de Personal, Biblioteca, Caja, Dirección.
- Para **reiniciar** el sistema desde cero basta con borrar ese archivo `.db`.

Tablas: `usuario`, `dependencia`, `remitente`, `expediente`, `movimiento` (historial), `correlativo` (último número por año).

**Copia de seguridad:** menú *Backup → Copia de seguridad BD*. **Restaurar:** cierre el sistema y reemplace el archivo `mesapartes.db` por la copia (con el mismo nombre).

## 4. Menú y módulos (ventanas) según el proyecto de EFSRT

| Menú | Opción | Ventana / acción | Clase |
|---|---|---|---|
| — | Inicio de sesión | Login | `ui.LoginFrame` |
| — | Ventana principal (escritorio MDI) | Principal | `ui.MainFrame` |
| — | Resumen por estado, con botón **Actualizar** | Resumen | `ui.DashboardPanel` |
| **Expediente** | Registrar expediente | Registro de Expedientes (DNI/RUC, remitente, tipo, asunto, folios, código correlativo) | `ui.RegistroPanel`, `ui.ComprobanteDialog` |
| | Derivar expediente | Derivación Interna (Personal, Biblioteca, Caja, Dirección) | `ui.DerivacionPanel` |
| | Consulta y seguimiento | Búsqueda por código, fecha o DNI; historial; cambio de estado; imprimir expediente | `ui.ConsultaPanel` |
| | Eliminar expediente | Elimina el expediente y su historial (con confirmación) | `ui.EliminarPanel` |
| | Salir | Cierra el sistema | `ui.MainFrame` |
| **Reportes** | Listado de trámites por rango de fechas y estado | Módulo de Reportes e Histórico: listado por **rango de fechas** y **estado del documento**, con impresión (vista previa A4 horizontal) y exportación a CSV | `ui.ReportesPanel`, `util.DocumentoHtml`, `util.Csv` |
| **Backup** | Copia de seguridad BD | Guarda una copia completa de la base de datos (`.db`) | `db.Respaldo` |
| **Ayuda** | Acerca de | Información del sistema | `ui.MainFrame` |

La impresión de la **ficha completa de un expediente** (datos e historial) está en *Consulta y seguimiento*, con el botón **Imprimir expediente**.

Código de seguimiento con formato **EXP-AAAA-00001** (correlativo por año; el número de un expediente eliminado no se reutiliza).

## 5. Estructura del código (para la evaluación del Docente Supervisor)

```
src/main/java/pe/edu/pppdiaz/mesapartes/
├── Main.java                  Punto de entrada (Look & Feel + inicialización de BD)
├── modelo/                    POO: EntidadBase (herencia) → Dependencia, Remitente, Expediente,
│                              Movimiento, Usuario; Estado. Atributos privados + getters/setters (encapsulamiento)
├── db/                        Conexion (JDBC y creación de tablas) y Respaldo (copia de seguridad con VACUUM INTO)
├── dao/                       Patrón DAO: UsuarioDAO, DependenciaDAO, RemitenteDAO, ExpedienteDAO
│                              (PreparedStatement, transacciones con commit/rollback)
├── excepcion/                 MesaPartesException (manejo de excepciones)
├── util/                      Validador (DNI, RUC, teléfono, correo), Seguridad (SHA-256), Fechas, Csv,
│                              DocumentoHtml (ficha de expediente y listado de trámites imprimibles), Recursos (logo)
└── ui/                        Interfaz gráfica Swing (ventanas y modelos de tabla)
src/test/java/...              Pruebas unitarias JUnit 5 (ValidadorTest, ExpedienteDAOTest, RespaldoTest, DocumentoHtmlTest)
```

Pruebas unitarias: en NetBeans, clic derecho sobre el proyecto → **Test** (Alt+F6), o `mvn test`.

## 6. Cambiar a MySQL (opcional)

1. En `pom.xml` agregue la dependencia `com.mysql:mysql-connector-j`.
2. En `db/Conexion.java` cambie la URL de `DriverManager.getConnection(...)` a `jdbc:mysql://localhost:3306/mesapartes` con usuario y clave.
3. Cambie `INTEGER PRIMARY KEY AUTOINCREMENT` por `INT AUTO_INCREMENT PRIMARY KEY`, y `INSERT OR IGNORE` por `INSERT IGNORE`, y `last_insert_rowid()` por `LAST_INSERT_ID()`.
