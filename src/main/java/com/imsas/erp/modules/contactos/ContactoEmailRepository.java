package com.imsas.erp.modules.contactos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link ContactoEmail}.
 *
 * <p>{@code ContactoEmail} no tiene campo {@code activo} (no extiende {@link com.imsas.erp.shared.entity.BaseEntity}),
 * por lo que la eliminación de correos se hace eliminando el registro físicamente
 * o bien desde la colección del {@link Contacto} padre.
 */
@Repository
public interface ContactoEmailRepository extends JpaRepository<ContactoEmail, UUID> {

    /**
     * Recupera todos los correos electrónicos de un contacto.
     *
     * @param contactoId ID del contacto
     * @return lista de correos del contacto
     */
    List<ContactoEmail> findAllByContactoId(UUID contactoId);

    /**
     * Verifica si ya existe el email para el contacto dado.
     * Refuerza la constraint {@code uq_contacto_email} desde la capa de servicio.
     *
     * @param contactoId ID del contacto
     * @param email      dirección de correo a verificar
     * @return {@code true} si el email ya está registrado para ese contacto
     */
    boolean existsByContactoIdAndEmail(UUID contactoId, String email);

    /**
     * Elimina físicamente todos los correos de un contacto.
     *
     * <p>Usado durante la actualización de un contacto para implementar el
     * reemplazo completo de la lista de emails. {@link ContactoEmail} no tiene
     * valor histórico propio (no extiende {@code BaseEntity} ni tiene campo
     * {@code activo}), por lo que la eliminación física no viola RN-12.
     *
     * @param contactoId ID del contacto cuyos correos se eliminarán
     */
    void deleteAllByContactoId(UUID contactoId);
}
