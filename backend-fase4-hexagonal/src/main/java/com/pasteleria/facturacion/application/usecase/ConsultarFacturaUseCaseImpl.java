package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.exception.FacturaNoEncontradaException;
import com.pasteleria.facturacion.domain.port.input.ConsultarFacturaUseCase;
import com.pasteleria.facturacion.domain.port.output.FacturaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConsultarFacturaUseCaseImpl implements ConsultarFacturaUseCase {

    private final FacturaRepositoryPort facturas;

    public ConsultarFacturaUseCaseImpl(FacturaRepositoryPort facturas) {
        this.facturas = facturas;
    }

    @Override
    public Factura ejecutar(Long idFactura) {
        return facturas.buscarPorId(idFactura).orElseThrow(() -> new FacturaNoEncontradaException(idFactura));
    }
}
