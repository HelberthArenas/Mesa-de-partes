# Manual de Usuario – Mesa de Partes IESP "Pedro P. Díaz"

## 1. Ingreso al sistema
Ejecute el programa. En la ventana **Iniciar sesión** escriba su usuario y contraseña
(inicial: `admin` / `admin123`) y presione **Ingresar**.

## 2. Pantalla principal
- **Menú superior**: **Expediente**, **Reportes**, **Backup** y **Ayuda**.
- Cada módulo se abre como una **ventana interna** que puede mover, minimizar, maximizar o cerrar.
- **Resumen**: muestra cuántos expedientes hay en cada estado. Use el botón **Actualizar** para volver a contar. Esta ventana no se cierra (solo se minimiza).

## 3. Registrar un expediente (Expediente → Registrar expediente)
1. Elija **DNI** (8 dígitos) o **RUC** (11 dígitos) y escríbalo. Con **Buscar** se recuperan los datos de un remitente ya registrado.
2. Complete nombre/razón social (obligatorio), teléfono y correo (opcionales, con formato válido).
3. Elija el **tipo de documento** (Oficio, Solicitud, Carta, Informe, Otro), escriba el **asunto** y el **número de folios**.
4. Opcional: en **Derivar a** elija una dependencia para enviarlo de inmediato.
5. Presione **Registrar expediente**. El sistema genera el código (ej. `EXP-2026-00001`) y muestra el **comprobante**; use **Imprimir** para entregarlo al remitente.

## 4. Derivar un expediente (Expediente → Derivar expediente)
1. Busque por código o DNI/RUC, o use **Ver todos los activos**.
2. Seleccione el expediente (solo aparecen los *Pendiente* y *En Proceso*).
3. Elija la **dependencia destino** (Personal, Biblioteca, Caja o Dirección), escriba una observación si desea y presione **Derivar expediente**.
4. El expediente pasa a estado **En Proceso** y queda registrado en su historial.

## 5. Consultar y dar seguimiento (Expediente → Consulta y seguimiento)
1. Busque por **código** (puede ser parcial), **DNI/RUC**, **fecha** (marque la casilla) y/o **estado**.
2. Al seleccionar un expediente se muestra su **historial** completo (registro, derivaciones y cambios de estado).
3. Para cambiar el estado elija **Pendiente, En Proceso, Atendido o Archivado**, agregue una observación y presione **Actualizar estado**.
4. **Ver comprobante** reimprime el comprobante del expediente y **Imprimir expediente** imprime su ficha completa con el historial.

## 6. Eliminar un expediente (Expediente → Eliminar expediente)
1. Busque por código o DNI/RUC y seleccione el expediente.
2. Presione **Eliminar expediente** y confirme en el mensaje.
3. Se elimina el expediente **y su historial**. **No se puede deshacer**: haga antes una copia de seguridad (menú Backup).
El número del expediente eliminado no se vuelve a usar.

## 7. Reportes: listado de trámites por rango de fechas y estado (menú Reportes)
1. Elija el rango de fechas con **Desde** y **Hasta** (ambas fechas se incluyen en el listado).
2. Elija el **estado del documento**: *Todos*, *Pendiente*, *En Proceso*, *Atendido* o *Archivado*.
3. Presione **Generar listado**. En la tabla aparecen los trámites encontrados y, abajo, el total, el conteo por estado y el total de folios.
4. **Rango rápido:** los botones **Hoy**, **Esta semana** (lunes a domingo) y **Este mes** llenan las fechas y generan el listado al instante.
5. **Imprimir listado:** abre la vista previa (A4 horizontal) con el resumen por estado, el resumen por ubicación actual, el detalle de los trámites y espacio para firmas. Presione **Imprimir** para enviarlo a la impresora.
6. **Exportar CSV:** guarda el listado en un archivo que se abre en Excel.

Para guardar el listado en **PDF**, en el cuadro de impresión de Windows elija **Microsoft Print to PDF**.

La ficha completa de **un solo expediente** (con su historial) se imprime desde *Consulta y seguimiento*, con el botón **Imprimir expediente**.

## 8. Copia de seguridad (menú Backup → Copia de seguridad BD)
1. Elija la carpeta y el nombre del archivo (se sugiere uno con fecha y hora, por ejemplo `copia_seguridad_mesapartes_20261121_083000.db`).
2. Presione **Guardar**. El sistema crea una copia completa de la base de datos y muestra su ubicación.
3. Guarde la copia en un lugar seguro (USB, otra carpeta o la nube).

**Restaurar una copia:** cierre el sistema, copie el archivo de respaldo a la carpeta `C:\Users\SU_USUARIO\.mesapartes\` con el nombre `mesapartes.db` (reemplazando el actual) y vuelva a abrir el sistema.

## 9. Salir
Menú **Expediente → Salir**.

## 10. Solución de problemas
| Problema | Solución |
|---|---|
| "No se pudo iniciar la base de datos" | Verifique que el proyecto se cargó con Maven (driver `sqlite-jdbc`) o use `ejecutar_sin_maven`. |
| Olvidó la contraseña | Borre el archivo `~/.mesapartes/mesapartes.db` (se reinicia con `admin`/`admin123`; **se pierden los datos**). |
| Eliminé un expediente por error | Restaure la última copia de seguridad (ver sección 8). |
| No sale el cuadro de impresión | Verifique que haya una impresora instalada (puede usar *Microsoft Print to PDF*). |
| El DNI/RUC es rechazado | DNI = 8 dígitos; RUC = 11 dígitos que empiecen con 10, 15, 17 o 20. |
