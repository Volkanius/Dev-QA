package dev.com.producto;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public Producto crear(ProductoRequest request) {
        String sku = normalizarSku(request.sku());
        if (repository.existsBySku(sku)) {
            throw new DatoDuplicadoException("Ya existe un producto con el SKU " + sku);
        }

        try {
            return repository.save(new Producto(request.nombre().trim(), sku, request.precio(), request.descripcion()));
        } catch (DuplicateKeyException exception) {
            throw new DatoDuplicadoException("Ya existe un producto con el SKU " + sku);
        }
    }

    public List<Producto> listar() {
        return repository.findAll();
    }

    public Producto obtener(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto con id " + id));
    }

    public Producto actualizar(String id, ProductoRequest request) {
        Producto producto = obtener(id);
        String sku = normalizarSku(request.sku());
        if (!sku.equals(producto.getSku()) && repository.existsBySku(sku)) {
            throw new DatoDuplicadoException("Ya existe un producto con el SKU " + sku);
        }

        producto.setNombre(request.nombre().trim());
        producto.setSku(sku);
        producto.setPrecio(request.precio());
        producto.setDescripcion(request.descripcion());
        try {
            return repository.save(producto);
        } catch (DuplicateKeyException exception) {
            throw new DatoDuplicadoException("Ya existe un producto con el SKU " + sku);
        }
    }

    public void eliminar(String id) {
        Producto producto = obtener(id);
        repository.delete(producto);
    }

    private String normalizarSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }
}
