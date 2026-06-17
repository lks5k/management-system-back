# MODELO DE DATOS — IMSAS V1
**Sistema de Gestión de Producción — Imagen Marquillas SAS**
**Versión:** 1.1
**Estado:** Aprobado

---

## Convenciones

- Todos los IDs son UUID v4
- Todas las tablas tienen `created_at` y `updated_at` automáticos
- Eliminación lógica universal: campo `activo boolean`
- Nombres de columnas en snake_case

---

## Tablas

### usuarios
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK, default gen_random_uuid() |
| nombre | VARCHAR(120) | NOT NULL |
| email | VARCHAR(120) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| rol | VARCHAR(20) | NOT NULL, enum |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at | TIMESTAMPTZ | NOT NULL, default NOW() |
| updated_at | TIMESTAMPTZ | NOT NULL, default NOW() |

**Rol enum:** `SUPERADMIN, ADMIN, MANAGER, OPERATOR, OFFICER, SALES_REP`

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

> Lista administrable por ADMIN/SUPERADMIN. Se selecciona en solicitudes, no se escribe libre.

---

### solicitudes
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| codigo | VARCHAR(20) | UNIQUE, nullable hasta BORRADOR→PENDIENTE |
| codigo_base_datos | VARCHAR(20) | UNIQUE, autoderivado |
| version_bd | SMALLINT | NOT NULL, default 0 |
| motivo_version_bd | TEXT | obligatorio si version_bd > 0 |
| solicitud_origen_id | UUID | FK → solicitudes (nullable) |
| asesor_id | UUID | NOT NULL, FK → usuarios |
| empresa_id | UUID | NOT NULL, FK → empresas |
| contacto_id | UUID | FK → contactos (nullable) |
| marca_id | UUID | FK → marcas (nullable) |
| producto_id | UUID | FK → productos (nullable) |
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
empresas ──< solicitudes
contactos ──< contacto_emails
contactos ──< solicitudes
marcas ──< solicitudes
productos ──< solicitudes
solicitudes ──< solicitudes (solicitud_origen_id, self-reference)
```

---

## Flujo de estados

```
BORRADOR → CONFIRMAR → PENDIENTE → COMPLETADO
                    ↘
                     CANCELADO (desde cualquier estado excepto COMPLETADO)
```

---

## Flujo "Convertir a Pedido"

Cuando una solicitud tipo `DISEÑO` es aprobada:
1. El asesor hace clic en "Convertir a Pedido"
2. El sistema crea una nueva solicitud tipo `PEDIDO` copiando todos los campos
3. El nuevo registro queda vinculado via `solicitud_origen_id` al DISEÑO original
4. El asesor ajusta solo lo que cambie (cantidad, medidas, etc.)
5. Ambos registros quedan históricos e intactos

El mismo mecanismo aplica para `REPOSICION`.

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
| RN-07 | codigoBaseDatos = "BD-" + sufijo del código. Versión inicia en -00 |
| RN-08 | version_bd incrementa solo por corrección estructural con motivo obligatorio |
| RN-09 | Pueden versionar BD: SALES_REP, OPERATOR, ADMIN, SUPERADMIN |
| RN-10 | Una empresa puede tener múltiples contactos y marcas |
| RN-11 | Validar duplicados de marca por empresa antes de crear |
| RN-12 | Eliminación lógica únicamente, nunca física |
| RN-13 | Cancelación requiere observacion_cancelacion obligatoria |
| RN-14 | Una solicitud CANCELADA no puede volver a BORRADOR |
| RN-15 | PEDIDO o REPOSICION originados de un DISEÑO deben referenciar solicitud_origen_id |

---

## Índices

| Índice | Tabla | Columna |
|---|---|---|
| idx_solicitudes_estado | solicitudes | estado |
| idx_solicitudes_empresa | solicitudes | empresa_id |
| idx_solicitudes_asesor | solicitudes | asesor_id |
| idx_solicitudes_created_at | solicitudes | created_at DESC |
| idx_solicitudes_origen | solicitudes | solicitud_origen_id |
| idx_contactos_empresa | contactos | empresa_id |
| idx_marcas_empresa | marcas | empresa_id |
| idx_contacto_emails_contacto | contacto_emails | contacto_id |