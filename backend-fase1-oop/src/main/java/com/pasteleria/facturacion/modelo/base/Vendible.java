package com.pasteleria.facturacion.modelo.base;

import java.math.BigDecimal;

/** Contrato de lo que se puede vender: tiene precio y stock, y sabe descontarse/restaurarse. */
public interface Vendible {

    BigDecimal getPrecio();

    int getStock();

    boolean tieneStockPara(int cantidad);

    void descontarStock(int cantidad);

    void restaurarStock(int cantidad);

    void validarDisponibleParaVenta();
}
