# MODELO DE DATOS — IMSAS V1
**Sistema de Gestión de Producción — Imagen Marquillas SAS**
**Versión:** 2.0
**Estado:** Aprobado

---

## Convenciones

- Todos los IDs son UUID v4
- Todas las tablas tienen `created_at` y `updated_at` automáticos
- Eliminación lógica universal: campo `activo boolean`
- Nombres de columnas en snake_case
- Todos los consecutivos de código (`P-`, `BD-`, `ART-`) inician en la
  letra `A`, no `S` (ver SDD sección 5.3)

---

## Tablas

### usuarios
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| nombre | VARCHAR(120) | NOT NULL |
| email | VARCHAR(120) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| rol | VARCHAR(20) | NOT NULL, enum |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**Rol enum:** `SUPERADMIN, ADMIN, MANAGER, OPERATOR, SALES_REP`

> Nota: el rol `OFFICER` fue retirado del modelo. Si se requiere en el
> futuro, debe aprobarse explícitamente y documentarse aquí primero.

---

### empresas
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| tipo_documento | VARCHAR(10) | NOT NULL, enum |
| numero_documento | VARCHAR(30) | NOT NULL, UNIQUE |
| razon_social | VARCHAR(200) | NOT NULL |
| tipo_empresa | VARCHAR(20) | NOT NULL, enum |
| direccion | VARCHAR(200) | |
| ciudad | VARCHAR(100) | |
| telefono | VARCHAR(30) | |
| sitio_web | VARCHAR(200) | |
| dia_cierre_contable | SMALLINT | CHECK 1-31 |
| pais | VARCHAR(60) | NOT NULL, default 'Colombia' |
| activo | BOOLEAN | NOT NULL, default TRUE |
| creado_por | UUID | FK → usuarios |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**tipo_documento enum:** `NIT, CC, CE, PASAPORTE`
**tipo_empresa enum:** `SAS, SA, LTDA, PERSONA_NATURAL, OTRO`

---

### contactos
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| empresa_id | UUID | NOT NULL, FK → empresas |
| nombre | VARCHAR(120) | NOT NULL |
| cargo | VARCHAR(100) | |
| telefono | VARCHAR(30) | |
| es_facturacion | BOOLEAN | NOT NULL, default FALSE |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

> `es_facturacion` es un flag informativo. No tiene restricción de
> unicidad por empresa — una empresa puede tener varios contactos
> marcados como facturación si así lo requiere operativamente.

---

### contacto_emails
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| contacto_id | UUID | NOT NULL, FK → contactos |
| email | VARCHAR(120) | NOT NULL |
| es_principal | BOOLEAN | NOT NULL, default FALSE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**Restricción:** UNIQUE (contacto_id, email)

---

### marcas
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| nombre | VARCHAR(120) | NOT NULL |
| empresa_id | UUID | NOT NULL, FK → empresas |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**Restricción:** UNIQUE (nombre, empresa_id)

---

### productos
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| nombre | VARCHAR(120) | NOT NULL, UNIQUE |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

> Lista administrable por ADMIN/SUPERADMIN. Se selecciona en
> solicitudes, no se escribe libre.

---

### artes  *(tabla nueva)*
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| codigo | VARCHAR(15) | NOT NULL, UNIQUE — formato `ART-A####` |
| empresa_id | UUID | NOT NULL, FK → empresas |
| marca_id | UUID | NOT NULL, FK → marcas |
| producto_id | UUID | NOT NULL, FK → productos |
| version_actual | SMALLINT | NOT NULL, default 0 — equivale a `-00` |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**Restricción:** UNIQUE (empresa_id, marca_id, producto_id) — solo puede
existir un Arte vigente por esa combinación exacta.

**Restricción:** CHECK (version_actual BETWEEN 0 AND 99)

> El historial completo de qué solicitud generó cada versión vive en la
> tabla `solicitudes` (campo `version_arte_generada`), no en una tabla
> de versiones separada — replicando el comportamiento del Excel actual.
>
> Al superar la versión 99, se crea un registro `Arte` nuevo (nuevo
> `codigo`, `version_actual` reinicia en 0); el Arte anterior permanece
> en la tabla como histórico inactivo o se relaciona mediante un campo
> `arte_predecesor_id` (a definir en el detalle de implementación).

---

### solicitudes
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| codigo | VARCHAR(20) | UNIQUE — formato `P-[A-Z]{1}[0-9]{5}` |
| codigo_base_datos | VARCHAR(20) | UNIQUE — formato `BD-[A-Z]{1}[0-9]{5}-[0-9]{2}` |
| version_bd | SMALLINT | NOT NULL, default 0 |
| motivo_version_bd | TEXT | obligatorio si version_bd > 0 |
| solicitud_origen_id | UUID | FK → solicitudes (nullable) |
| arte_id | UUID | FK → artes (nullable) |
| version_arte_generada | SMALLINT | nullable — versión de Arte que esta solicitud generó (si aplica) |
| cantidad_versiones_entregadas | SMALLINT | nullable, CHECK ≤ 3 — para solicitudes DISEÑO que generan múltiples versiones |
| asesor_id | UUID | NOT NULL, FK → usuarios |
| empresa_id | UUID | NOT NULL, FK → empresas |
| contacto_id | UUID | FK → contactos (nullable) |
| marca_id | UUID | FK → marcas (nullable) |
| producto_id | UUID | FK → productos (nullable) |
| detalle_producto | VARCHAR(200) | nullable — variante o especificación libre (ej. "STICKER QR") sin afectar la tabla productos |
| tipo_solicitud | VARCHAR(20) | NOT NULL, enum |
| descripcion | TEXT | |
| cantidad | INTEGER | NOT NULL, > 0 |
| tecnica | VARCHAR(20) | NOT NULL, enum |
| ancho | NUMERIC(8,2) | |
| largo | NUMERIC(8,2) | |
| colores_frente | SMALLINT | |
| colores_reverso | SMALLINT | |
| costura | VARCHAR(20) | enum, nullable |
| orden_compra | VARCHAR(60) | |
| precio_referencia | NUMERIC(12,2) | |
| abono | NUMERIC(12,2) | default 0 |
| observaciones | TEXT | |
| estado | VARCHAR(20) | NOT NULL, default 'BORRADOR', enum |
| observacion_cancelacion | TEXT | obligatorio si estado = CANCELADO |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**tipo_solicitud enum:** `DISEÑO, MUESTRAS, PEDIDO, REPOSICION, CORTE, PRESTAMO`

**estado enum:** `BORRADOR, CONFIRMAR, PENDIENTE, COMPLETADO, CANCELADO`

**tecnica enum:** `DIGITAL, SUBLIMACION, FLEXOGRAFIA, LITOGRAFIA, SCREEN, TEJIDA, TRANSFER`

**costura enum:** `ARRIBA, ABAJO, IZQUIERDA, DERECHA, PARA_DOBLAR, TODOS_LOS_LADOS, SIN_COSTURA, TRAPECIO, A_DOS_LADOS`

---

## Relaciones

```
usuarios ──< solicitudes (asesor_id)
empresas ──< contactos
empresas ──< marcas
empresas ──< artes
empresas ──< solicitudes
contactos ──< contacto_emails
contactos ──< solicitudes
marcas ──< artes
marcas ──< solicitudes
productos ──< artes
productos ──< solicitudes
artes ──< solicitudes (arte_id)
solicitudes ──< solicitudes (solicitud_origen_id, self-reference)
```

---

## Flujo de estados de Solicitud

```
BORRADOR → CONFIRMAR → PENDIENTE → COMPLETADO
                    ↘
                     CANCELADO (desde cualquier estado excepto COMPLETADO)
```

---

## Flujo de reutilización y versionado de Arte

Al crear una solicitud tipo `DISEÑO`:

1. Buscar `Arte` por `(empresa_id, marca_id, producto_id)`
2. **Si existe:** vincular `arte_id`; al completar, incrementar
   `artes.version_actual` en +1 (o en el número de
   `cantidad_versiones_entregadas`, máximo 3)
3. **Si no existe:** crear `Arte` nuevo, `version_actual = 0`
4. **Si `version_actual` alcanza 99:** la siguiente entrega crea un
   `Arte` nuevo con código consecutivo siguiente, `version_actual = 0`

Solicitudes que no son tipo `DISEÑO` (PEDIDO, MUESTRAS, REPOSICION, CORTE,
PRESTAMO) solo **referencian** el Arte vigente — no lo modifican.

---

## Reglas de Negocio

| ID | Regla |
|---|---|
| RN-01 | Toda solicitud debe pertenecer a una empresa |
| RN-02 | El número de documento es único por empresa |
| RN-03 | El correo electrónico debe tener formato válido |
| RN-04 | El código de solicitud se genera al pasar de BORRADOR a PENDIENTE |
| RN-05 | El consecutivo nunca se reinicia |
| RN-06 | El código de solicitud es inmutable una vez asignado |
| RN-07 | codigoBaseDatos = "BD-" + sufijo del código + versión. Versión inicia en -00 |
| RN-08 | version_bd incrementa solo por corrección estructural de los datos del cliente con motivo obligatorio |
| RN-09 | Pueden versionar BD: SALES_REP, ADMIN, SUPERADMIN |
| RN-10 | Una empresa puede tener múltiples contactos y marcas |
| RN-11 | Al crear Marca nueva, normalizar (trim+uppercase) y comparar contra existentes de la misma empresa; advertencia no bloqueante si coincide |
| RN-12 | Eliminación lógica únicamente, nunca física |
| RN-13 | Cancelación requiere observacion_cancelacion obligatoria |
| RN-14 | Una solicitud CANCELADO no puede volver a BORRADOR |
| RN-15 | PEDIDO o REPOSICION originados de un DISEÑO deben referenciar solicitud_origen_id |
| RN-16 | codigoSolicitud = "P-" + 1 letra + 5 dígitos, patrón `P-[A-Z]{1}[0-9]{5}`, inicia en `P-A00001` |
| RN-17 | Siglas de documentos: Arte `ART-`, Orden de Compra `OC-`, Solicitud `P-`, Base de Datos `BD-` |
| RN-18 | codigoArte = "ART-" + 1 letra + 4 dígitos + versión, patrón `ART-[A-Z]{1}[0-9]{4}-[0-9]{2}`, inicia en `ART-A0001-00`, versión máxima `-99` |
| RN-19 | Al crear solicitud DISEÑO, buscar Arte existente por Empresa+Marca+Producto antes de crear uno nuevo |
| RN-20 | Una solicitud DISEÑO puede generar máximo 3 versiones de Arte en una sola entrega |
| RN-21 | Solicitudes que no son DISEÑO no modifican versión de Arte, solo lo referencian |
| RN-22 | SUPERADMIN es único e inmutable desde la interfaz; se aprovisiona por script de inicialización backend |
| RN-23 | ADMIN no puede ver ni modificar al SUPERADMIN, ni auto-eliminarse |
| RN-24 | OPERATOR: solo lectura global de solicitudes, sin acceso a empresas/contactos/marcas |
| RN-25 | Empresa.razon_social, Marca.nombre y Producto.nombre se normalizan (trim+uppercase) antes de guardar |

---

## Índices

| Índice | Tabla | Columna |
|---|---|---|
| idx_solicitudes_estado | solicitudes | estado |
| idx_solicitudes_empresa | solicitudes | empresa_id |
| idx_solicitudes_asesor | solicitudes | asesor_id |
| idx_solicitudes_created_at | solicitudes | created_at DESC |
| idx_solicitudes_origen | solicitudes | solicitud_origen_id |
| idx_solicitudes_arte | solicitudes | arte_id |
| idx_contactos_empresa | contactos | empresa_id |
| idx_marcas_empresa | marcas | empresa_id |
| idx_contacto_emails_contacto | contacto_emails | contacto_id |
| idx_artes_combinacion | artes | (empresa_id, marca_id, producto_id) |