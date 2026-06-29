# MODELO DE DATOS — IMSAS V1
**Sistema de Gestión de Producción — Imagen Marquillas SAS**
**Versión:** 2.1
**Estado:** Aprobado (núcleo) · Borrador pendiente de aprobación (sección nueva, módulos Fase 2 de negocio)

---

## Convenciones

- Todos los IDs son UUID v4
- Todas las tablas tienen `created_at` y `updated_at` automáticos
- Eliminación lógica universal: campo `activo boolean`
- Nombres de columnas en snake_case
- Todos los consecutivos de código (`P-`, `BD-`, `ART-`) inician en la
  letra `A`, no `S` (ver SDD sección 5.3)

---

## Tablas (núcleo — implementado y en producción)

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
> Etiquetas visuales (frontend, no afectan este enum): ADMIN → "Gerente",
> MANAGER → "Operario líder".

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

> `es_facturacion` es un flag informativo, sin restricción de unicidad por empresa.

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
| descripcion | TEXT | nullable |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

> Lista administrable por ADMIN/SUPERADMIN. Catálogo de **productos terminados**
> que se ofrecen al cliente — NO debe confundirse con `insumos` (materia prima
> interna, ver sección nueva más abajo).

---

### artes
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| codigo | VARCHAR(15) | NOT NULL, UNIQUE — formato `ART-A####` |
| empresa_id | UUID | NOT NULL, FK → empresas |
| marca_id | UUID | NOT NULL, FK → marcas |
| producto_id | UUID | NOT NULL, FK → productos |
| version_actual | SMALLINT | NOT NULL, default 0 |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**Restricción:** Índice único PARCIAL `WHERE activo = true` — solo puede existir un Arte activo por combinación (empresa_id, marca_id, producto_id). Múltiples artes históricos con `activo = false` son válidos para la misma combinación.
**Restricción:** CHECK (version_actual BETWEEN 0 AND 99)

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
| version_arte_generada | SMALLINT | nullable |
| cantidad_versiones_entregadas | SMALLINT | nullable, CHECK ≤ 3 |
| asesor_id | UUID | NOT NULL, FK → usuarios |
| empresa_id | UUID | NOT NULL, FK → empresas |
| contacto_id | UUID | FK → contactos (nullable) |
| marca_id | UUID | FK → marcas (nullable) |
| producto_id | UUID | FK → productos (nullable) |
| detalle_producto | VARCHAR(200) | nullable |
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

## Relaciones (núcleo)

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
2. **Si existe:** vincular `arte_id`; al completar, incrementar `artes.version_actual`
3. **Si no existe:** crear `Arte` nuevo, `version_actual = 0`
4. **Si `version_actual` alcanza 99:** crear `Arte` nuevo con código consecutivo siguiente

Solicitudes no-DISEÑO solo **referencian** el Arte vigente, no lo modifican.

---

## Reglas de Negocio (núcleo)

| ID | Regla |
|---|---|
| RN-01 | Toda solicitud debe pertenecer a una empresa |
| RN-02 | El número de documento es único por empresa |
| RN-03 | El correo electrónico debe tener formato válido |
| RN-04 | El código de solicitud se genera al pasar de BORRADOR a CONFIRMAR |
| RN-05 | El consecutivo nunca se reinicia |
| RN-06 | El código de solicitud es inmutable una vez asignado |
| RN-07 | codigoBaseDatos = "BD-" + sufijo del código + versión. Versión inicia en -00 |
| RN-08 | version_bd incrementa solo por corrección estructural con motivo obligatorio |
| RN-09 | Pueden versionar BD: SALES_REP, ADMIN, SUPERADMIN |
| RN-10 | Una empresa puede tener múltiples contactos y marcas |
| RN-11 | Al crear Marca, normalizar (trim+uppercase) y advertir si coincide con existente |
| RN-12 | Eliminación lógica únicamente, nunca física |
| RN-13 | Cancelación requiere observacion_cancelacion obligatoria |
| RN-14 | Una solicitud CANCELADO no puede volver a BORRADOR |
| RN-15 | PEDIDO/REPOSICION originados de DISEÑO deben referenciar solicitud_origen_id |
| RN-16 | codigoSolicitud = "P-" + 1 letra + 5 dígitos, inicia en `P-A00001` |
| RN-17 | Siglas: Arte `ART-`, Orden de Compra `OC-`, Solicitud `P-`, Base de Datos `BD-` |
| RN-18 | codigoArte = "ART-" + 1 letra + 4 dígitos + versión, inicia en `ART-A0001-00` |
| RN-19 | Al crear solicitud DISEÑO, buscar Arte existente por Empresa+Marca+Producto antes de crear uno nuevo |
| RN-20 | Una solicitud DISEÑO puede generar máximo 3 versiones de Arte en una entrega |
| RN-21 | Solicitudes no-DISEÑO no modifican versión de Arte, solo lo referencian |
| RN-22 | SUPERADMIN es único e inmutable desde la interfaz |
| RN-23 | ADMIN no puede ver ni modificar al SUPERADMIN, ni auto-eliminarse |
| RN-24 | OPERATOR: solo lectura global de solicitudes, sin acceso a empresas/contactos/marcas |
| RN-25 | razon_social, Marca.nombre y Producto.nombre se normalizan (trim+uppercase) antes de guardar |

---

## Índices (núcleo)

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

---
---

## PROPUESTA — Módulos Fase 2 de negocio

> ⚠️ **Estado: BORRADOR — NO implementado, no aprobado, no enviar a Cursor sin revisión de Lukas primero.**
> Generado: 28 Jun 2026. Diseño preliminar para discusión, sujeto a cambios antes de convertirse en migración Flyway real.
> Orden de implementación recomendado: Insumos → Ingresos → Descargas → Inventario → (resolver almacenamiento de archivos) → Diseño.

### Catálogo nuevo: `insumos`
Necesario porque `productos` representa el catálogo de **productos terminados** ofrecidos al cliente — los insumos (materia prima: vinilos, tintas, bobinas, etc.) son conceptualmente distintos y no deben mezclarse en la misma tabla.

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| nombre | VARCHAR(150) | NOT NULL, UNIQUE |
| unidad_medida | VARCHAR(20) | NOT NULL, enum: `UNIDAD, METRO, KILOGRAMO, LITRO, ROLLO` (ajustar según insumos reales) |
| stock_minimo | NUMERIC(12,2) | nullable — para alertas futuras, opcional en V1 |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at / updated_at | TIMESTAMPTZ | NOT NULL |

---

### `ingresos_material`
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| insumo_id | UUID | NOT NULL, FK → insumos |
| cantidad | NUMERIC(12,2) | NOT NULL, > 0 |
| proveedor_empresa_id | UUID | FK → empresas, nullable (si el proveedor está registrado como Empresa) |
| proveedor_nombre_libre | VARCHAR(200) | nullable — si el proveedor no está en `empresas` |
| fecha_ingreso | DATE | NOT NULL |
| registrado_por | UUID | NOT NULL, FK → usuarios |
| observaciones | TEXT | nullable |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at / updated_at | TIMESTAMPTZ | NOT NULL |

---

### `descargas_material`
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| insumo_id | UUID | NOT NULL, FK → insumos |
| cantidad | NUMERIC(12,2) | NOT NULL, > 0 |
| motivo | VARCHAR(20) | NOT NULL, enum: `PRODUCCION, VENTA_INSUMO, MERMA, AJUSTE` |
| solicitud_id | UUID | FK → solicitudes, nullable (obligatorio si motivo = PRODUCCION, a validar en backend) |
| registrado_por | UUID | NOT NULL, FK → usuarios |
| fecha_descarga | DATE | NOT NULL |
| observaciones | TEXT | nullable |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at / updated_at | TIMESTAMPTZ | NOT NULL |

> **Regla propuesta (a confirmar):** la descarga no debe permitir dejar el stock calculado en negativo, salvo que se marque explícitamente como `AJUSTE`. El stock actual se calcula como `SUM(ingresos) - SUM(descargas)` por insumo — no se almacena como campo persistente para evitar inconsistencias (se recalcula en consulta).

---

### `inventario_conteos` + `inventario_conteo_detalle`
Diseño tipo snapshot — un conteo manual no debe sobreescribir conteos anteriores, cada uno es un registro histórico independiente.

**`inventario_conteos`**
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| fecha_conteo | DATE | NOT NULL |
| registrado_por | UUID | NOT NULL, FK → usuarios |
| estado | VARCHAR(20) | NOT NULL, default `BORRADOR`, enum: `BORRADOR, CONFIRMADO` |
| observaciones | TEXT | nullable |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at / updated_at | TIMESTAMPTZ | NOT NULL |

**`inventario_conteo_detalle`**
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| conteo_id | UUID | NOT NULL, FK → inventario_conteos |
| insumo_id | UUID | NOT NULL, FK → insumos |
| cantidad_contada | NUMERIC(12,2) | NOT NULL, >= 0 |
| cantidad_sistema | NUMERIC(12,2) | nullable — snapshot de lo calculado (ingresos-descargas) al momento del conteo, para auditar diferencias |
| created_at / updated_at | TIMESTAMPTZ | NOT NULL |

**Restricción:** UNIQUE (conteo_id, insumo_id)

---

### Módulo de Diseño — asignación tipo "reclamar tarea"

> **Dependencia bloqueante:** los entregables de diseño son archivos — este módulo no puede completarse hasta resolver almacenamiento (Supabase Storage vs MinIO/VPS, ver `DECISIONES_DESPLIEGUE.md` §2.7). El modelo de datos de abajo puede implementarse antes (estados de asignación), pero el campo de entregable quedaría sin uso real hasta esa decisión.

**`diseno_asignaciones`**
| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| solicitud_id | UUID | NOT NULL, UNIQUE, FK → solicitudes (solo aplica a tipo DISEÑO) |
| disenador_id | UUID | FK → usuarios, nullable (null = disponible para reclamar) |
| estado | VARCHAR(20) | NOT NULL, default `DISPONIBLE`, enum: `DISPONIBLE, RECLAMADO, EN_PROCESO, ENTREGADO` |
| reclamado_en | TIMESTAMPTZ | nullable |
| entregado_en | TIMESTAMPTZ | nullable |
| entregable_url | VARCHAR(500) | nullable — URL del archivo en el storage que se elija |
| notas_disenador | TEXT | nullable |
| activo | BOOLEAN | NOT NULL, default TRUE |
| created_at / updated_at | TIMESTAMPTZ | NOT NULL |

**Reglas propuestas (a confirmar antes de implementar):**
- Solo se crea una fila `diseno_asignaciones` cuando una Solicitud tipo DISEÑO pasa a estado `CONFIRMAR` (o `PENDIENTE`, a decidir)
- "Reclamar" = cualquier usuario con rol de diseñador hace `PATCH` que asigna `disenador_id` = su propio ID y cambia estado a `RECLAMADO` — debe ser atómico para evitar que dos diseñadores reclamen la misma tarea simultáneamente (usar lock optimista o `WHERE disenador_id IS NULL` en el UPDATE)
- ¿Rol "diseñador" nuevo, o reutilizar `OPERATOR`? — **pendiente de decisión de Lukas**, no asumir

---

## Pendiente de decisión antes de aprobar esta sección

1. ¿Unidades de medida reales de los insumos de IMSAS? (la lista enum arriba es genérica, ajustar a la realidad de la planta)
2. ¿Debe existir alerta de stock mínimo en V1, o se difiere?
3. ¿Rol "Diseñador" nuevo en el enum `Rol`, o se reutiliza un rol existente?
4. Confirmar storage de archivos antes de tocar `entregable_url` en serio
