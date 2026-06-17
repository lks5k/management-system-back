# SDD — Sistema de Gestión de Producción
## Imagen Marquillas SAS
**Versión:** 2.0
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
registro de clientes hasta el seguimiento de solicitudes de producción.

---

# 2. Alcance

## Incluido en V1

- Autenticación con roles diferenciados
- Gestión de empresas
- Gestión de contactos con múltiples correos
- Gestión de marcas con detección de duplicados
- Gestión de productos (lista administrable)
- Gestión de solicitudes con flujo de estados
- Vinculación entre solicitudes (Diseño → Pedido → Reposición)
- Consulta y seguimiento de solicitudes

## No incluido en V1

- Módulo de diseño gráfico
- Módulo de producción
- Módulo de inventario
- Módulo de despachos
- Indicadores y reportes avanzados
- Notificaciones automáticas
- Integraciones externas
- Gestión documental avanzada
- Cotizaciones formales
- Módulo financiero/contable

---

# 3. Roles

## SUPERADMIN
Control total del sistema.

**Permisos:**
- Todo lo de ADMIN
- Gestión de usuarios y roles
- Acceso indiscutible a cualquier registro
- Versionar código de base de datos

## ADMIN
Gestión completa del sistema.

**Permisos:**
- Todo lo de MANAGER
- Editar cualquier registro
- Cambiar cualquier estado
- Versionar código de base de datos

## MANAGER
Supervisión y aprobación.

**Permisos:**
- Aprobar y rechazar solicitudes
- Consultar información global
- Acceder a reportes globales

## OPERATOR
Gestión operativa de maestros y solicitudes.

**Permisos:**
- CRUD completo de empresas, contactos, marcas, productos
- Crear y editar solicitudes
- Versionar código de base de datos

## OFFICER
Gestión de solicitudes propias.

**Permisos:**
- Crear y editar sus propias solicitudes
- Adjuntar archivos a solicitudes

## SALES_REP
Gestión comercial de su cartera.

**Permisos:**
- Consultar sus clientes asignados
- Crear y editar sus propias solicitudes
- Versionar código de base de datos de sus solicitudes

---

# 4. Conceptos del Negocio

## Empresa
Entidad jurídica con la cual se realiza la relación comercial.

Ejemplos: FANALCA, NCS MODA, PRACTIPOLOS

## Contacto
Persona perteneciente a una empresa con quien se tiene relación directa.
Una empresa puede tener múltiples contactos.
Un contacto puede tener múltiples correos electrónicos.

Ejemplos de cargo: Comprador, Diseñador, Coordinador de producción

## Marca
Marca comercial asociada a una empresa.
Una empresa puede tener múltiples marcas.

Ejemplos: HONDA, QUEST, VÉLEZ

## Producto
Tipo de producto que IMSAS produce.
Lista administrable por ADMIN/SUPERADMIN.
Se selecciona en la solicitud — no se escribe libremente.

Ejemplos: Etiqueta tejida, Etiqueta digital, Marquilla corte

## Solicitud
Requerimiento comercial que será gestionado por los procesos de diseño
y producción. Es la entidad central del sistema.

Una solicitud siempre pertenece a una empresa.
Puede estar vinculada a un contacto, marca y producto.
Puede originarse desde otra solicitud (Diseño → Pedido → Reposición).

---

# 5. Flujo de Solicitudes

## 5.1 Tipos de Solicitud

| Tipo | Descripción |
|---|---|
| DISEÑO | Primera solicitud. Se pide diseño antes de producir. |
| MUESTRAS | Solicitud de muestras. No necesariamente precede un pedido en firme. |
| PEDIDO | Producción en firme. Puede originarse desde un DISEÑO aprobado. |
| REPOSICION | Repetición de un pedido anterior. Puede heredar el código BD original o generar uno nuevo si la base de datos cambió. |
| CORTE | El cliente entrega un rollo de marquillas impreso para corte. |
| PRESTAMO | Préstamo de material o insumos al cliente, a un compañero o a otro proveedor. |

## 5.2 Estados

| Estado | Descripción |
|---|---|
| BORRADOR | Solicitud incompleta. El asesor la guarda para continuar después. |
| CONFIRMAR | Esperando confirmación del cliente para proceder. |
| PENDIENTE | Cliente confirmó. Producción puede proceder. |
| COMPLETADO | Solicitud finalizada. Estado terminal. |
| CANCELADO | Solicitud anulada. Requiere observación obligatoria. |

## 5.3 Diagrama de Estados

```
BORRADOR → CONFIRMAR → PENDIENTE → COMPLETADO
                    ↘
                     CANCELADO
```

> Una solicitud CANCELADO no puede volver a BORRADOR.
> CANCELADO puede aplicarse desde cualquier estado excepto COMPLETADO.

## 5.4 Flujo "Convertir a Pedido"

Cuando una solicitud tipo DISEÑO es aprobada por el cliente:

1. El asesor abre la solicitud DISEÑO y hace clic en "Convertir a Pedido"
2. El sistema crea automáticamente una nueva solicitud tipo PEDIDO
   copiando todos los campos del DISEÑO original
3. El asesor revisa y ajusta solo lo que cambie (cantidad, medidas, etc.)
4. La nueva solicitud queda vinculada al DISEÑO original via `solicitud_origen_id`
5. Ambos registros permanecen históricos e intactos

**Beneficio:** el sistema puede reportar cuántos diseños se convirtieron en
pedido y cuántos no, sin perder el histórico del proceso.

El mismo mecanismo aplica para REPOSICION.

---

# 6. Casos de Uso

## CU-01 Iniciar Sesión
**Actores:** Todos los roles

1. El usuario ingresa correo y contraseña
2. El sistema valida credenciales
3. El sistema genera sesión autenticada con JWT
4. El usuario accede al dashboard según su rol

## CU-02 Registrar Empresa
**Actores:** SALES_REP, OFFICER, OPERATOR, ADMIN, SUPERADMIN

1. Ingresar tipo y número de documento
2. El sistema valida que el número de documento no exista (RN-02)
3. Completar datos de la empresa
4. Guardar

## CU-03 Registrar Contacto
**Actores:** SALES_REP, OFFICER, OPERATOR, ADMIN, SUPERADMIN

1. Seleccionar empresa
2. Registrar datos del contacto
3. Agregar uno o más correos electrónicos
4. Indicar si es contacto de facturación
5. Guardar

## CU-04 Registrar Marca
**Actores:** SALES_REP, OFFICER, OPERATOR, ADMIN, SUPERADMIN

1. Seleccionar empresa
2. Ingresar nombre de la marca
3. El sistema busca marcas similares en la misma empresa (RN-11)
4. Confirmar y guardar

## CU-05 Crear Solicitud
**Actores:** SALES_REP, OFFICER, OPERATOR, ADMIN, SUPERADMIN

1. Seleccionar empresa
2. Seleccionar contacto (opcional)
3. Seleccionar marca (opcional)
4. Seleccionar tipo de solicitud
5. Seleccionar producto de la lista
6. Completar datos de producción
7. Guardar como BORRADOR o enviar a CONFIRMAR

## CU-06 Convertir Diseño a Pedido
**Actores:** SALES_REP, OFFICER, OPERATOR, ADMIN, SUPERADMIN

1. Abrir solicitud tipo DISEÑO
2. Hacer clic en "Convertir a Pedido"
3. El sistema crea un PEDIDO copiando los datos del DISEÑO
4. El asesor ajusta las diferencias
5. Guardar — el PEDIDO queda vinculado al DISEÑO original

## CU-07 Cambiar Estado de Solicitud
**Actores:** Según permisos por rol

1. Abrir solicitud
2. Seleccionar nuevo estado
3. Si es CANCELADO: ingresar observación obligatoria
4. Si versiona código BD: ingresar motivo obligatorio
5. Guardar

## CU-08 Consultar Solicitudes
**Actores:** Todos los roles (filtrado según permisos)

1. Acceder al listado de solicitudes
2. Filtrar por estado, tipo, empresa, asesor, fecha
3. Ver detalle de la solicitud
4. Consultar solicitudes vinculadas (origen o derivadas)

---

# 7. Reglas de Negocio

| ID | Regla |
|---|---|
| RN-01 | Toda solicitud debe pertenecer a una empresa |
| RN-02 | El número de documento es único por empresa |
| RN-03 | El correo electrónico debe tener formato válido |
| RN-04 | El código de solicitud se genera al pasar de BORRADOR a PENDIENTE |
| RN-05 | El consecutivo de solicitudes nunca se reinicia |
| RN-06 | El código de solicitud es inmutable una vez asignado |
| RN-07 | codigoBaseDatos = "BD-" + sufijo del código. Versión inicia en -00 |
| RN-08 | version_bd incrementa solo por corrección estructural (tallas, cantidades, colores, referencias) con motivo obligatorio |
| RN-09 | Pueden versionar código BD: SALES_REP, OPERATOR, ADMIN, SUPERADMIN |
| RN-10 | Una empresa puede tener múltiples contactos y marcas |
| RN-11 | Validar duplicados de marca por empresa antes de crear |
| RN-12 | Eliminación lógica únicamente, nunca física |
| RN-13 | Cancelación requiere observacion_cancelacion obligatoria |
| RN-14 | Una solicitud CANCELADO no puede volver a BORRADOR |
| RN-15 | PEDIDO o REPOSICION originados de un DISEÑO deben referenciar solicitud_origen_id |
| RN-16 | La lista de productos es administrada por ADMIN/SUPERADMIN — no se escribe libremente |

---

# 8. Pantallas

## Login
- Correo y contraseña
- Validación de credenciales

## Dashboard
- Total empresas activas
- Total solicitudes por estado
- Solicitudes recientes

## Empresas
- Listado con filtros
- Crear / Editar / Desactivar

## Contactos
- Listado filtrado por empresa
- Crear / Editar con múltiples correos

## Marcas
- Listado filtrado por empresa
- Crear con detección de duplicados

## Productos
- Listado administrable
- Crear / Editar / Desactivar (solo ADMIN/SUPERADMIN)

## Solicitudes
- Listado con filtros por estado, tipo, empresa, asesor, fecha
- Crear / Editar
- Convertir a Pedido (desde DISEÑO aprobado)
- Cambio de estado

## Detalle de Solicitud
- Datos generales
- Solicitud origen (si aplica)
- Solicitudes derivadas (pedidos o reposiciones originados aquí)
- Historial de cambios de estado

---

# 9. Stack Tecnológico

## Frontend
- HTML5
- Tailwind CSS (CDN)
- Alpine.js (CDN)

## Backend
- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA

## Base de Datos
- PostgreSQL 16
- Flyway (migraciones versionadas)

## Infraestructura
- Docker Compose (desarrollo local)
- Nginx (producción)
- VPS Linux

## Control de Versiones
- Git / GitHub

---

# 10. Criterios de Finalización V1

La versión 1 se considera terminada cuando:

- Los usuarios pueden autenticarse con su rol
- Las empresas pueden registrarse y consultarse
- Los contactos pueden registrarse con múltiples correos
- Las marcas pueden registrarse con detección de duplicados
- Los productos pueden administrarse desde el sistema
- Las solicitudes pueden crearse, editarse y consultarse
- El flujo de estados funciona correctamente
- La conversión Diseño → Pedido funciona con un clic
- Toda la información queda almacenada en PostgreSQL
- Los permisos por rol están correctamente aplicados