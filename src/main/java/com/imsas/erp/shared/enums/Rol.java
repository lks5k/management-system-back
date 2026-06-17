package com.imsas.erp.shared.enums;

/**
 * Roles de usuario del sistema.
 *
 * <p>Define los niveles de acceso y las capacidades de cada tipo de usuario.
 * Spring Security usa estos valores con el prefijo {@code ROLE_} internamente,
 * pero en la BD y en los DTOs se almacenan y exponen sin dicho prefijo.
 *
 * <p>Jerarquía de permisos (mayor a menor):
 * <ol>
 *   <li>{@link #SUPERADMIN} — Control total, gestión de usuarios y roles</li>
 *   <li>{@link #ADMIN} — Gestión completa, edición de cualquier registro</li>
 *   <li>{@link #MANAGER} — Supervisión, aprobación y reportes globales</li>
 *   <li>{@link #OPERATOR} — CRUD de maestros y solicitudes</li>
 *   <li>{@link #OFFICER} — Gestión de solicitudes propias</li>
 *   <li>{@link #SALES_REP} — Gestión comercial de su cartera de clientes</li>
 * </ol>
 */
public enum Rol {

    /**
     * Control total del sistema.
     * Incluye gestión de usuarios, roles y acceso indiscutible a cualquier registro.
     */
    SUPERADMIN,

    /**
     * Gestión completa del sistema.
     * Puede editar cualquier registro y cambiar cualquier estado.
     */
    ADMIN,

    /**
     * Supervisión y aprobación.
     * Accede a reportes globales y puede aprobar o rechazar solicitudes.
     */
    MANAGER,

    /**
     * Gestión operativa.
     * CRUD completo de empresas, contactos, marcas, productos y solicitudes.
     * Puede versionar el código de base de datos (RN-09).
     */
    OPERATOR,

    /**
     * Gestión de solicitudes propias.
     * Puede crear, editar sus propias solicitudes y adjuntar archivos.
     */
    OFFICER,

    /**
     * Representante comercial.
     * Gestiona su cartera de clientes y sus propias solicitudes.
     * Puede versionar el código BD de sus solicitudes (RN-09).
     */
    SALES_REP
}
