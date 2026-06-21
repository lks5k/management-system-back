package com.imsas.erp.modules.artes;

import com.imsas.erp.modules.empresas.Empresa;
import com.imsas.erp.modules.marcas.Marca;
import com.imsas.erp.modules.productos.Producto;
import com.imsas.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entidad que representa el "Arte Base" de una combinación empresa/marca/producto.
 *
 * <h3>Concepto</h3>
 * <p>Un Arte agrupa todas las versiones de diseño entregadas para una combinación
 * específica de empresa, marca y producto. Solo puede existir un Arte <em>vigente</em>
 * (activo = true) por combinación; esto está garantizado por el índice único parcial
 * {@code uq_arte_combinacion_vigente} en la BD.
 *
 * <h3>Ciclo de vida de versiones</h3>
 * <p>Cada vez que se entrega una nueva versión del Arte, {@code versionActual} se
 * incrementa. Cuando {@code versionActual} llega a {@code 99} y se requiere continuar,
 * se crea un nuevo Arte (con {@code versionActual = 0}) y el Arte anterior se desactiva
 * ({@code activo = false}), permitiendo así múltiples Artes históricos por combinación.
 *
 * <h3>Código</h3>
 * <p>Formato: {@code ART-[A-Z]\d{4}} — p. ej. {@code ART-A0001} (RN-18).
 * Generado por {@link com.imsas.erp.shared.utils.CodeGenerator#generateCodigoArte(long)}
 * desde la secuencia {@code seq_arte_consecutivo} de PostgreSQL.
 *
 * <h3>Reglas críticas</h3>
 * <ul>
 *   <li>RN-18: código generado desde consecutivo propio independiente del código de solicitud.</li>
 *   <li>RN-19: antes de crear un Arte nuevo, la capa de servicio verifica si ya existe uno
 *       vigente para la combinación empresa/marca/producto.</li>
 *   <li>RN-20: máximo 3 versiones de Arte por solicitud DISEÑO.</li>
 * </ul>
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "artes")
public class Arte extends BaseEntity {

    /**
     * Código único del Arte. Formato {@code ART-[A-Z]\d{4}} (p. ej. {@code ART-A0001}).
     * Inmutable una vez asignado ({@code updatable = false}).
     */
    @Column(unique = true, updatable = false, nullable = false, length = 15)
    private String codigo;

    /**
     * Empresa propietaria del Arte. FK obligatoria → {@code empresas}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_arte_empresa")
    )
    private Empresa empresa;

    /**
     * Marca vinculada al Arte. FK obligatoria → {@code marcas}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "marca_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_arte_marca")
    )
    private Marca marca;

    /**
     * Producto al que aplica el Arte. FK obligatoria → {@code productos}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "producto_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_arte_producto")
    )
    private Producto producto;

    /**
     * Número de la última versión entregada. Rango [0, 99].
     * Inicia en {@code 0} al crear el Arte; incrementa en la capa de servicio.
     * Al llegar a {@code 99} el Arte se desactiva y se crea uno nuevo.
     */
    @Builder.Default
    @Column(name = "version_actual", nullable = false)
    private Short versionActual = 0;
}
