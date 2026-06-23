# SDD — Sistema de Gestión de Producción
## Imagen Marquillas SAS
**Versión:** 3.0
**Estado:** Aprobado para desarrollo

---

# 1. Propósito

El Sistema de Gestión de Producción centraliza y controla la información
comercial y operativa de las solicitudes de producción de Imagen Marquillas SAS.

El sistema actual opera sobre archivos Excel independientes y cadenas de
correo/WhatsApp, generando duplicidad de información, pérdida de trazabilidad,
inconsistencias en los datos y dificultades para el seguimiento por parte de
los diferentes actores del proceso.

La versión 1 reemplaza ese flujo con una plataforma única que cubre desde el
registro de clientes hasta el seguimiento de solicitudes de producción y
la gestión de artes/diseños.

---

# 2. Alcance

## Incluido en V1

- Autenticación con roles diferenciados
- Gestión de empresas
- Gestión de contactos con múltiples correos
- Gestión de marcas con detección de duplicados
- Gestión de productos (lista administrable)
- Gestión de artes (cabecera + versión vigente)
- Gestión de solicitudes con flujo de estados
- Vinculación entre solicitudes (Diseño → Pedido → Reposición)
- Reutilización automática de Arte por Empresa+Marca+Producto
- Consulta y seguimiento de solicitudes

## No incluido en V1

- Módulo de producción física
- Módulo de inventario
- Módulo de despachos
- Indicadores y reportes avanzados
- Notificaciones automáticas
- Integraciones externas
- Gestión documental avanzada (almacenamiento de archivos CDR/PNG)
- Cotizaciones formales
- Módulo financiero/contable
- Portal de autoservicio para diseñadores externos

---

# 3. Roles

## SUPERADMIN
Control total del sistema. Único e inmutable desde la interfaz —
se aprovisiona exclusivamente mediante script de inicialización
en el backend (Data Seed). No puede ser creado, editado ni eliminado
desde la UI por ningún otro rol.

**Permisos:**
- Todo lo de ADMIN
- Gestión de usuarios y roles (incluyendo ADMIN)
- Acceso indiscutible a cualquier registro
- Acceso a logs de auditoría sin restricciones

## ADMIN
Gestión completa del negocio.

**Permisos:**
- Todo lo de MANAGER
- CRUD completo de empresas, contactos, marcas, productos, artes
- Editar cualquier solicitud
- Cambiar cualquier estado
- Gestionar usuarios de rol MANAGER, OPERATOR, SALES_REP
- No puede ver ni modificar al SUPERADMIN
- No puede auto-eliminarse

## MANAGER
Supervisión y aprobación.

**Permisos:**
- Consultar todas las solicitudes de todos los asesores (solo lectura)
- Aprobar y rechazar solicitudes
- Acceder a reportes globales

## OPERATOR
Ejecución operativa de producción. No gestiona datos comerciales.

**Permisos:**
- Consultar (solo lectura) todas las solicitudes de todos los asesores
- Sin acceso a empresas, contactos ni marcas
- Actualizar únicamente los campos propios de su proceso (a definir
  cuando se construya el módulo de producción en V2)

## SALES_REP
Gestión comercial de su cartera.

**Permisos:**
- Consultar y gestionar sus clientes asignados (empresas, contactos, marcas)
- Crear y editar sus propias solicitudes mientras estén en BORRADOR
- Versionar el código de Base de Datos (la base de datos del cliente —
  Excel con tallas, colores, referencias — NO la base de datos de la
  aplicación) cuando el cliente reporta un error en los datos entregados
  inicialmente

---

# 4. Conceptos del Negocio

## Empresa
Entidad jurídica con la cual se realiza la relación comercial.

Ejemplos: FANALCA, NCS MODA, PRACTIPOLOS

## Contacto
Persona perteneciente a una empresa con quien se tiene relación directa.
Una empresa puede tener múltiples contactos.
Un contacto puede tener múltiples correos electrónicos.

## Marca
Marca comercial asociada a una empresa.
Una empresa puede tener múltiples marcas.

Ejemplos: HONDA, QUEST, VÉLEZ

## Producto
Tipo de producto que IMSAS produce.
Lista administrable por ADMIN/SUPERADMIN.
Se selecciona en la solicitud — no se escribe libremente.

## Arte
Diseño gráfico asociado a una combinación única de Empresa + Marca + Producto.

Un Arte tiene un código fijo (`ART-A####`) y una **versión vigente**
(`-00` a `-99`). Cuando el diseñador entrega una actualización del diseño,
la versión vigente del Arte se incrementa.

**Reutilización automática:** al crear una solicitud tipo DISEÑO, el sistema
verifica si ya existe un Arte para esa combinación exacta de Empresa+Marca+
Producto:
- Si existe, la solicitud reutiliza ese Arte y genera la siguiente versión
- Si no existe, se crea un Arte nuevo en versión `-00`

**Múltiples versiones en una sola entrega:** un diseñador puede entregar
hasta 3 versiones distintas de un mismo Arte en respuesta a una sola
solicitud DISEÑO (por ejemplo, 3 propuestas visuales diferentes). La
solicitud debe registrar cuántas versiones fueron generadas para que el
sistema sepa cuál es la última versión vigente al recibir la siguiente
solicitud relacionada.

**Versionado:** cada versión documenta una corrección, actualización o
variación del diseño — todas las versiones son válidas y se conservan en
el historial de solicitudes; no se eliminan versiones "incorrectas".

**Límite de versión:** al llegar a `-99`, la siguiente solicitud genera
un Arte completamente nuevo con código distinto, comenzando en `-00`.

## Solicitud
Requerimiento comercial que será gestionado por los procesos de diseño
y producción. Es la entidad central del sistema.

Una solicitud siempre pertenece a una empresa.
Puede estar vinculada a un contacto, marca, producto y arte.
Puede originarse desde otra solicitud (Diseño → Pedido → Reposición).

---

# 5. Sistema de Códigos

## 5.1 Siglas por tipo de documento

| Sigla | Documento |
|---|---|
| `P-` | Solicitud o Pedido |
| `BD-` | Base de Datos (del cliente, para producción) |
| `ART-` | Arte / Diseño |
| `OC-` | Orden de Compra |

## 5.2 Formato de códigos

**Solicitud:** `P-` + 1 letra + 5 dígitos, ejemplo `P-A00001`
Patrón: `P-[A-Z]{1}[0-9]{5}`
Consecutivo global, inicia en `P-A00001`, nunca se reinicia.

**Base de Datos:** `BD-` + el mismo bloque alfanumérico de la solicitud
+ versión, ejemplo `BD-A00001-00`
Patrón: `BD-[A-Z]{1}[0-9]{5}-[0-9]{2}`
La versión inicia en `-00` e incrementa solo por corrección estructural
de los datos del cliente (tallas, cantidades, colores, referencias).

**Arte:** `ART-` + 1 letra + 4 dígitos + versión, ejemplo `ART-A2570-00`
Patrón: `ART-[A-Z]{1}[0-9]{4}-[0-9]{2}`
Consecutivo propio (independiente del código de solicitud), inicia en
`ART-A0001`. La versión inicia en `-00`, incrementa con cada entrega de
diseño nueva o corregida, hasta un máximo de `-99`. Al superar ese límite,
se asigna un nuevo código de Arte.

## 5.3 Por qué se reinicia desde A

Los códigos históricos de IMSAS iniciaron en la letra `S` por motivos
de imagen comercial (dar impresión de trayectoria desde el inicio de la
empresa). Con el sistema profesional nuevo y la trayectoria ya consolidada
ante los clientes, los consecutivos reinician desde `A` para todos los
tipos de documento.

---

# 6. Flujo de Solicitudes

## 6.1 Tipos de Solicitud

| Tipo | Descripción |
|---|---|
| DISEÑO | Solicita diseño nuevo o actualización de un Arte existente. |
| MUESTRAS | Solicitud de muestras. No necesariamente precede un pedido en firme. |
| PEDIDO | Producción en firme. Puede originarse desde un DISEÑO aprobado. |
| REPOSICION | Repetición de un pedido anterior. Puede heredar el código BD original o generar uno nuevo si la base de datos del cliente cambió. |
| CORTE | El cliente entrega un rollo de marquillas impreso para corte. |
| PRESTAMO | Préstamo de material o insumos al cliente, a un compañero o a otro proveedor. |

## 6.2 Estados

| Estado | Descripción |
|---|---|
| BORRADOR | Solicitud incompleta. El asesor la guarda para continuar después. |
| CONFIRMAR | Esperando confirmación del cliente para proceder. |
| PENDIENTE | Cliente confirmó. Producción puede proceder. |
| COMPLETADO | Solicitud finalizada. Estado terminal. |
| CANCELADO | Solicitud anulada. Requiere observación obligatoria. |

## 6.3 Diagrama de Estados

```
BORRADOR → CONFIRMAR → PENDIENTE → COMPLETADO
                    ↘
                     CANCELADO
```

> Una solicitud CANCELADO no puede volver a BORRADOR.
> CANCELADO puede aplicarse desde cualquier estado excepto COMPLETADO.

## 6.4 Flujo "Convertir a Pedido"

Cuando una solicitud tipo DISEÑO es aprobada por el cliente:

1. El asesor abre la solicitud DISEÑO y hace clic en "Convertir a Pedido"
2. El sistema crea automáticamente una nueva solicitud tipo PEDIDO
   copiando todos los campos del DISEÑO original, incluyendo el Arte
   y su versión vigente
3. El asesor revisa y ajusta solo lo que cambie (cantidad, medidas, etc.)
4. La nueva solicitud queda vinculada al DISEÑO original via
   `solicitud_origen_id`
5. Ambos registros permanecen históricos e intactos

El mismo mecanismo aplica para REPOSICION.

## 6.5 Flujo de reutilización de Arte

Al crear una solicitud tipo DISEÑO:

1. El sistema busca un Arte existente para la combinación exacta de
   Empresa + Marca + Producto
2. **Si existe:** la solicitud se vincula a ese Arte; al completarse el
   diseño, la versión del Arte incrementa en +1 (o el número de versiones
   entregadas, hasta 3 por solicitud)
3. **Si no existe:** se crea un Arte nuevo con código consecutivo siguiente,
   versión inicial `-00`
4. **Si la versión llega a `-99`:** la siguiente entrega genera un Arte
   completamente nuevo, versión `-00`

Las solicitudes tipo PEDIDO, MUESTRAS, REPOSICION, CORTE y PRESTAMO
**no generan ni modifican** versiones de Arte — solo lo referencian si
ya existe uno asociado a la combinación Empresa+Marca+Producto.

---

# 7. Casos de Uso

## CU-01 Iniciar Sesión
**Actores:** Todos los roles

1. El usuario ingresa correo y contraseña
2. El sistema valida credenciales
3. El sistema genera sesión autenticada con JWT
4. El usuario accede al dashboard según su rol

## CU-02 Registrar Empresa
**Actores:** SALES_REP, ADMIN, SUPERADMIN

## CU-03 Registrar Contacto
**Actores:** SALES_REP, ADMIN, SUPERADMIN

## CU-04 Registrar Marca
**Actores:** SALES_REP, ADMIN, SUPERADMIN

1. Seleccionar empresa
2. Ingresar nombre de la marca
3. El sistema busca marcas similares en la misma empresa (RN-11)
4. Confirmar y guardar

## CU-05 Crear Solicitud tipo DISEÑO
**Actores:** SALES_REP, ADMIN, SUPERADMIN

1. Seleccionar empresa, contacto, marca, producto
2. El sistema verifica si existe Arte para esa combinación
3. Si existe: muestra el código y versión vigente del Arte
4. Si no existe: indica que se creará un Arte nuevo
5. Completar datos de producción
6. Guardar como BORRADOR o enviar a CONFIRMAR

## CU-06 Convertir Diseño a Pedido
**Actores:** SALES_REP, ADMIN, SUPERADMIN

1. Abrir solicitud tipo DISEÑO completada
2. Hacer clic en "Convertir a Pedido"
3. El sistema crea un PEDIDO copiando los datos del DISEÑO, incluyendo
   el Arte vinculado
4. El asesor ajusta las diferencias
5. Guardar — el PEDIDO queda vinculado al DISEÑO original

## CU-07 Versionar Base de Datos del cliente
**Actores:** SALES_REP, ADMIN, SUPERADMIN

1. Abrir solicitud con código BD existente
2. Indicar que los datos del cliente (tallas, colores, referencias)
   tienen un error o actualización
3. Ingresar motivo obligatorio
4. El sistema incrementa la versión del código BD

## CU-08 Cambiar Estado de Solicitud
**Actores:** Según permisos por rol

1. Abrir solicitud
2. Seleccionar nuevo estado
3. Si es CANCELADO: ingresar observación obligatoria
4. Guardar

## CU-09 Consultar Solicitudes
**Actores:** Todos los roles (filtrado según permisos)

1. Acceder al listado de solicitudes
2. Filtrar por estado, tipo, empresa, asesor, fecha
3. Ver detalle de la solicitud
4. Consultar solicitudes vinculadas (origen o derivadas) y Arte asociado

---

# 8. Reglas de Negocio

| ID | Regla |
|---|---|
| RN-01 | Toda solicitud debe pertenecer a una empresa |
| RN-02 | El número de documento de la empresa es único |
| RN-03 | El correo electrónico debe tener formato válido |
| RN-04 | El código de solicitud se genera al pasar de BORRADOR a CONFIRMAR |
| RN-05 | El consecutivo de solicitudes nunca se reinicia |
| RN-06 | El código de solicitud es inmutable una vez asignado |
| RN-07 | codigoBaseDatos = "BD-" + sufijo del código de solicitud + versión. Versión inicia en -00 |
| RN-08 | version_bd incrementa solo por corrección estructural de los datos del cliente (tallas, cantidades, colores, referencias) con motivo obligatorio |
| RN-09 | Pueden versionar código BD: SALES_REP, ADMIN, SUPERADMIN |
| RN-10 | Una empresa puede tener múltiples contactos y marcas |
| RN-11 | Al crear una Marca nueva, normalizar el nombre (trim + uppercase) y comparar contra marcas existentes de la misma empresa; si hay coincidencia exacta tras normalizar, mostrar advertencia no bloqueante al usuario |
| RN-12 | Eliminación lógica únicamente, nunca física |
| RN-13 | Cancelación requiere observacion_cancelacion obligatoria |
| RN-14 | Una solicitud CANCELADO no puede volver a BORRADOR |
| RN-15 | PEDIDO o REPOSICION originados de un DISEÑO deben referenciar solicitud_origen_id |
| RN-16 | El código de solicitud usa el patrón `P-[A-Z]{1}[0-9]{5}`, inicia en `P-A00001` |
| RN-17 | Siglas de documentos: Arte `ART-`, Orden de Compra `OC-`, Solicitud `P-`, Base de Datos `BD-` |
| RN-18 | El código de Arte usa el patrón `ART-[A-Z]{1}[0-9]{4}-[0-9]{2}`, inicia en `ART-A0001-00`, versión máxima `-99` |
| RN-19 | Al crear una solicitud DISEÑO, el sistema busca un Arte existente para la combinación Empresa+Marca+Producto antes de crear uno nuevo |
| RN-20 | Una solicitud DISEÑO puede generar hasta 3 versiones de Arte en una sola entrega |
| RN-21 | Solicitudes que no son tipo DISEÑO no generan ni modifican versiones de Arte, solo lo referencian |
| RN-22 | El SUPERADMIN es único e inmutable desde la interfaz; se aprovisiona mediante script de inicialización en el backend |
| RN-23 | ADMIN no puede ver ni modificar al SUPERADMIN, ni auto-eliminarse |
| RN-24 | OPERATOR tiene acceso de solo lectura a todas las solicitudes; sin acceso a empresas, contactos ni marcas |
| RN-25 | Los campos Empresa.razonSocial, Marca.nombre y Producto.nombre se normalizan (trim + uppercase) antes de guardar en base de datos, para evitar duplicados por variación de mayúsculas o espacios |

---

# 9. Pantallas

## Login
## Dashboard
## Empresas
## Contactos
## Marcas
## Productos
## Artes
- Listado con código, versión vigente, empresa, marca, producto
- Historial de solicitudes que generaron cada versión

## Solicitudes
- Listado con filtros por estado, tipo, empresa, asesor, fecha
- Crear / Editar
- Convertir a Pedido (desde DISEÑO aprobado)
- Versionar Base de Datos del cliente
- Cambio de estado

## Detalle de Solicitud
- Datos generales
- Arte asociado (si aplica) con código y versión
- Solicitud origen (si aplica)
- Solicitudes derivadas
- Historial de cambios de estado

---

# 10. Stack Tecnológico

## Frontend
- HTML5, Tailwind CSS (CDN), Alpine.js (CDN)

## Backend
- Java 17, Spring Boot 3, Spring Security + JWT, Spring Data JPA

## Base de Datos
- PostgreSQL 16, Flyway

## Infraestructura
- Docker Compose (desarrollo), Nginx (producción), VPS Linux

## Control de Versiones
- Git / GitHub

---

# 11. Criterios de Finalización V1

- Los usuarios pueden autenticarse con su rol
- Las empresas, contactos, marcas y productos pueden gestionarse
- Las solicitudes pueden crearse, editarse y consultarse
- El flujo de estados funciona correctamente
- La conversión Diseño → Pedido funciona con un clic
- La reutilización y versionado de Artes funciona automáticamente
- El versionado de Base de Datos del cliente funciona con motivo obligatorio
- Toda la información queda almacenada en PostgreSQL
- Los permisos por rol están correctamente aplicados y verificados