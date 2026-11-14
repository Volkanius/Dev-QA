# Microservicio CRUD de productos

API REST con Spring Boot 3, Java 17 y MongoDB. El SKU se normaliza a mayúsculas y es único.

## Ejecución local

Requiere JDK 17+ y Maven 3.8+. Inicia MongoDB con Docker Compose y luego la aplicación:

```powershell
docker compose up -d
mvn spring-boot:run
```

La URI predeterminada es `mongodb://localhost:27017/devqa`; se puede cambiar con la variable de entorno `MONGODB_URI`. La documentación interactiva Swagger UI está disponible en `http://localhost:8080/swagger-ui/index.html`.

| Método | Ruta | Resultado |
|---|---|---|
| `POST` | `/api/productos` | Crea un producto (`201`) |
| `GET` | `/api/productos` | Lista productos (`200`) |
| `GET` | `/api/productos/{id}` | Obtiene un producto (`200` o `404`) |
| `PUT` | `/api/productos/{id}` | Reemplaza un producto (`200`, `404` o `409`) |
| `DELETE` | `/api/productos/{id}` | Elimina un producto (`204` o `404`) |

El cuerpo de creación y actualización requiere `nombre`, `sku` (3-30 caracteres alfanuméricos o guiones) y `precio` (no negativo, hasta dos decimales). `descripcion` es opcional. Los errores devuelven JSON con estado, mensaje, ruta y detalles de validación.

## Pruebas y análisis

```powershell
mvn verify
```

Ejecuta pruebas unitarias y web, genera el reporte HTML de cobertura en `target/site/jacoco/index.html` y falla si la cobertura global de líneas o ramas es menor al 80%.

Para analizar con SonarQube, configura `SONAR_HOST_URL` y `SONAR_TOKEN` para una instancia activa y ejecuta:

```powershell
mvn verify sonar:sonar "-Dsonar.host.url=$env:SONAR_HOST_URL" "-Dsonar.token=$env:SONAR_TOKEN"
```

La tarea espera el resultado del Quality Gate del servidor (`sonar.qualitygate.wait=true`). Configura en el servidor sus condiciones para cobertura mínima del 80%, bugs y vulnerabilidades sin severidad crítica.

## Entregables de HU-01

- Reporte técnico: [docs/Reporte_Tecnico_HU-01.pdf](docs/Reporte_Tecnico_HU-01.pdf)
- Evidencias Swagger, Maven y JaCoCo: `docs/evidencias/`

La publicación del repositorio institucional y el análisis SonarQube requieren configurar el remoto Git y una instancia con credenciales autorizadas; el reporte marca ambos puntos como pendientes hasta contar con esos accesos.
