package com.pasteleria.facturacion.modelo.base;

import org.hibernate.Hibernate;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Raíz de la jerarquía: toda entidad tiene identidad (id). Igualdad por tipo + id.
 * Cada subclase indica el nombre de su columna con @AttributeOverride.
 */
@MappedSuperclass
public abstract class EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || Hibernate.getClass(this) != Hibernate.getClass(otro)) {
            return false;
        }
        EntidadBase that = (EntidadBase) otro;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
