package dev.com.producto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoService service;

    @Test
    void crearNormalizaSkuYGuardaElProducto() {
        ProductoRequest request = request("  Teclado  ", " ab-123 ");
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto result = service.crear(request);

        assertThat(result.getNombre()).isEqualTo("Teclado");
        assertThat(result.getSku()).isEqualTo("AB-123");
        verify(repository).existsBySku("AB-123");
        verify(repository).save(any(Producto.class));
    }

    @Test
    void crearRechazaSkuExistente() {
        when(repository.existsBySku("AB-123")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request("Teclado", "ab-123")))
                .isInstanceOf(DatoDuplicadoException.class)
                .hasMessageContaining("AB-123");

        verify(repository, never()).save(any(Producto.class));
    }

    @Test
    void crearTraduceColisionDeIndiceUnico() {
        when(repository.save(any(Producto.class))).thenThrow(new org.springframework.dao.DuplicateKeyException("duplicate"));

        assertThatThrownBy(() -> service.crear(request("Teclado", "ab-123")))
                .isInstanceOf(DatoDuplicadoException.class);
    }

    @Test
    void listarDevuelveLosProductosDelRepositorio() {
        List<Producto> productos = List.of(producto("1", "Teclado", "AB-123"));
        when(repository.findAll()).thenReturn(productos);

        assertThat(service.listar()).containsExactlyElementsOf(productos);
    }

    @Test
    void obtenerDevuelveElProductoCuandoExiste() {
        Producto producto = producto("1", "Teclado", "AB-123");
        when(repository.findById("1")).thenReturn(Optional.of(producto));

        assertThat(service.obtener("1")).isSameAs(producto);
    }

    @Test
    void obtenerFallaCuandoNoExiste() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener("missing"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void actualizarModificaLosCamposYConservaSkuExistente() {
        Producto producto = producto("1", "Teclado", "AB-123");
        when(repository.findById("1")).thenReturn(Optional.of(producto));
        when(repository.save(producto)).thenReturn(producto);

        Producto result = service.actualizar("1", request(" Teclado mecánico ", "ab-123"));

        assertThat(result.getNombre()).isEqualTo("Teclado mecánico");
        assertThat(result.getPrecio()).isEqualByComparingTo("25.50");
        verify(repository, never()).existsBySku("AB-123");
    }

    @Test
    void actualizarRechazaSkuDeOtroProducto() {
        when(repository.findById("1")).thenReturn(Optional.of(producto("1", "Teclado", "AB-123")));
        when(repository.existsBySku("CD-456")).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar("1", request("Teclado", "cd-456")))
                .isInstanceOf(DatoDuplicadoException.class);

        verify(repository, never()).save(any(Producto.class));
    }

    @Test
    void actualizarTraduceColisionDeIndiceUnico() {
        Producto producto = producto("1", "Teclado", "AB-123");
        when(repository.findById("1")).thenReturn(Optional.of(producto));
        when(repository.existsBySku("CD-456")).thenReturn(false);
        when(repository.save(producto)).thenThrow(new org.springframework.dao.DuplicateKeyException("duplicate"));

        assertThatThrownBy(() -> service.actualizar("1", request("Teclado", "cd-456")))
                .isInstanceOf(DatoDuplicadoException.class);
    }

    @Test
    void eliminarQuitaElProductoExistente() {
        Producto producto = producto("1", "Teclado", "AB-123");
        when(repository.findById("1")).thenReturn(Optional.of(producto));

        service.eliminar("1");

        verify(repository).delete(producto);
    }

    @Test
    void eliminarFallaCuandoElProductoNoExiste() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar("missing"))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repository, never()).delete(any(Producto.class));
    }

    private ProductoRequest request(String nombre, String sku) {
        return new ProductoRequest(nombre, sku, new BigDecimal("25.50"), "Descripción");
    }

    private Producto producto(String id, String nombre, String sku) {
        Producto producto = new Producto(nombre, sku, new BigDecimal("25.50"), "Descripción");
        producto.setId(id);
        return producto;
    }
}
