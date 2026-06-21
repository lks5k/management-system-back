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
 *   <li>{@link #OPERATOR} — Operaciones de producción, acceso restringido</li>
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
     * Puede editar cualquier registro, cambiar cualquier estado y versionar
     * el código de base de datos de solicitudes (RN-09).
     */
    ADMIN,

    /**
     * Supervisión y aprobación.
     * Accede a reportes globales y puede aprobar o rechazar solicitudes.
     */
    MANAGER,

    /**
     * Operaciones de producción.
     * Solo lectura sobre solicitudes; sin acceso a maestros de
     * empresas, contactos ni marcas. No puede versionar código BD (RN-09).
     */
    OPERATOR,

    /**
     * Representante comercial.
     * Gestiona su cartera de clientes y sus propias solicitudes.
     * Puede versionar el código BD de sus propias solicitudes (RN-09).
     */
    SALES_REP
}
