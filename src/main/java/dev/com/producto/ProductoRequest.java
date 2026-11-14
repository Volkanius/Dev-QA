package dev.com.producto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,
        @NotBlank(message = "El SKU es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9-]{3,30}$", message = "El SKU debe tener entre 3 y 30 caracteres alfanuméricos o guiones")
        String sku,
        @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 10 enteros y 2 decimales")
        BigDecimal precio,
        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String descripcion) {
}
