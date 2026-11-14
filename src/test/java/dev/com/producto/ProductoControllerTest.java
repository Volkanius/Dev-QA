package dev.com.producto;

import dev.com.api.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(ProductoController.class)
@Import(GlobalExceptionHandler.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService service;

    @Test
    void crearDevuelve201YLaRepresentacionJson() throws Exception {
        when(service.crear(any(ProductoRequest.class))).thenReturn(producto("p-1"));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/productos/p-1"))
                .andExpect(jsonPath("$.nombre").value("Teclado"))
                .andExpect(jsonPath("$.sku").value("AB-123"))
                .andExpect(jsonPath("$.precio").value(25.50));
    }

    @Test
    void crearConDatosInvalidosDevuelve400ConDetalles() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"", "sku":"?", "precio":-2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La solicitud contiene datos inválidos"))
                .andExpect(jsonPath("$.detalles.nombre").exists())
                .andExpect(jsonPath("$.detalles.sku").exists())
                .andExpect(jsonPath("$.detalles.precio").exists());
    }

    @Test
    void crearConJsonMalformadoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud no es un JSON válido"));
    }

    @Test
    void listarDevuelveLaColeccionSerializada() throws Exception {
        when(service.listar()).thenReturn(List.of(producto("p-1")));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p-1"))
                .andExpect(jsonPath("$[0].nombre").value("Teclado"));
    }

    @Test
    void obtenerInexistenteDevuelve404Estructurado() throws Exception {
        when(service.obtener("missing")).thenThrow(new RecursoNoEncontradoException("Producto no encontrado"));

        mockMvc.perform(get("/api/productos/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/productos/missing"))
                .andExpect(jsonPath("$.message").value("Producto no encontrado"));
    }

    @Test
    void skuDuplicadoDevuelve409() throws Exception {
        when(service.crear(any(ProductoRequest.class))).thenThrow(new DatoDuplicadoException("SKU duplicado"));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void actualizarDevuelveProductoActualizado() throws Exception {
        when(service.actualizar(eq("p-1"), any(ProductoRequest.class))).thenReturn(producto("p-1"));

        mockMvc.perform(put("/api/productos/p-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-1"));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        doNothing().when(service).eliminar("p-1");

        mockMvc.perform(delete("/api/productos/p-1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar("p-1");
    }

    private String requestJson() {
        return """
                {"nombre":"Teclado","sku":"AB-123","precio":25.50,"descripcion":"USB"}
                """;
    }

    private Producto producto(String id) {
        Producto producto = new Producto("Teclado", "AB-123", new BigDecimal("25.50"), "USB");
        producto.setId(id);
        return producto;
    }
}
