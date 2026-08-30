package com.logitrack.services;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

import com.logitrack.entities.OrdenCompra;
import com.logitrack.enums.EstadoOrdenCompra;
import com.logitrack.exceptions.ResourceNotFoundException;
import com.logitrack.repositories.OrdenCompraRepository;

import jakarta.transaction.Transactional;

@Service
public class PdfOrdenService {

    private static final PDFont FUENTE = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FUENTE_NEGRITA = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private final OrdenCompraRepository ordenCompraRepository;
    private final Clock clock;

    public PdfOrdenService(OrdenCompraRepository ordenCompraRepository, Clock clock) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.clock = clock;
    }

    @Transactional
    public byte[] generar(Long ordenId) {
        OrdenCompra orden = buscarOrden(ordenId);
        byte[] pdf = generarDocumento(orden);

        orden.setPdf(pdf);
        orden.setFechaGeneracionPdf(LocalDateTime.now(clock));
        ordenCompraRepository.save(orden);
        return pdf;
    }

    public byte[] obtener(Long ordenId) {
        OrdenCompra orden = buscarOrden(ordenId);
        if (orden.getPdf() == null || orden.getPdf().length == 0) {
            throw new ResourceNotFoundException("La orden de compra no tiene un PDF generado.");
        }
        return orden.getPdf();
    }

    private OrdenCompra buscarOrden(Long ordenId) {
        return ordenCompraRepository.findById(ordenId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "Orden de compra no encontrada con id: " + ordenId));
    }

    private byte[] generarDocumento(OrdenCompra orden) {
        try (PDDocument documento = new PDDocument();
                        ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                escribirContenido(contenido, orden);
                if (orden.getEstado() == EstadoOrdenCompra.BORRADOR) {
                    escribirMarcaDeAgua(contenido, pagina);
                }
            }

            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el PDF de la orden.", exception);
        }
    }

    private void escribirContenido(PDPageContentStream contenido, OrdenCompra orden) throws IOException {
        float margenIzquierdo = 60;
        float y = 780;

        contenido.beginText();
        contenido.setFont(FUENTE_NEGRITA, 18);
        contenido.newLineAtOffset(margenIzquierdo, y);
        contenido.showText("Orden de compra #" + orden.getId());
        contenido.endText();

        String[] lineas = {
                "Fecha de creación: " + orden.getFechaCreacion(),
                "Estado: " + orden.getEstado(),
                "Proveedor: " + orden.getProveedor().getNombre(),
                "Producto: " + orden.getProducto().getNombre(),
                "Cantidad: " + orden.getCantidad(),
                "Precio unitario: " + formatoDinero(orden.getPrecioUnitario()),
                "Total: " + formatoDinero(orden.getTotal()),
                "Bodega destino: " + orden.getBodegaDestino().getNombre()
        };

        contenido.beginText();
        contenido.setFont(FUENTE, 12);
        contenido.setLeading(20);
        contenido.newLineAtOffset(margenIzquierdo, y - 45);
        for (String linea : lineas) {
            contenido.showText(linea);
            contenido.newLine();
        }
        contenido.endText();
    }

    private void escribirMarcaDeAgua(PDPageContentStream contenido, PDPage pagina) throws IOException {
        PDExtendedGraphicsState transparencia = new PDExtendedGraphicsState();
        transparencia.setNonStrokingAlphaConstant(0.25f);
        contenido.saveGraphicsState();
        contenido.setGraphicsStateParameters(transparencia);
        contenido.setNonStrokingColor(new Color(180, 0, 0));
        contenido.transform(Matrix.getRotateInstance(
                        Math.toRadians(45),
                        pagina.getMediaBox().getWidth() / 2,
                        pagina.getMediaBox().getHeight() / 2));
        contenido.beginText();
        contenido.setFont(FUENTE_NEGRITA, 52);
        contenido.newLineAtOffset(130, 330);
        contenido.showText("BORRADOR");
        contenido.endText();
        contenido.restoreGraphicsState();
    }

    private String formatoDinero(BigDecimal valor) {
        return valor.toPlainString();
    }
}
