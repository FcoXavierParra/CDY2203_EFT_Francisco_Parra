# Documentación de API - Pet Adoption System

Documentación técnica de los servicios disponibles en la API de gestión de mascotas y pacientes.

## Información General

- **Base URL**: `http://localhost:8080` (desarrollo) | `https://api.example.com` (producción)
- **Versión API**: 1.0.0
- **Autenticación**: JWT Bearer Token
- **Content-Type**: `application/json`

---

## 📋 Tabla de Contenidos

1. [Autenticación](#autenticación)
2. [Mascotas (Pets)](#mascotas-pets)
3. [Pacientes (Patients)](#pacientes-patients)
4. [Códigos de Respuesta](#códigos-de-respuesta)
5. [Ejemplos de Uso](#ejemplos-de-uso)

---

## Autenticación

### POST /login
Autentica un usuario y retorna un token JWT para usar en otras operaciones protegidas.

**Acceso**: Público (sin autenticación)

**Request Body**:
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response** (200 OK):
```json
"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTUxNjIzOTAyMn0..."
```

**Response** (400 Bad Request):
```json
{
  "message": "Login failed: User not found"
}
```

**Headers Requeridos**:
- `Content-Type: application/json`

**Nota**: El token retornado debe usarse como `Authorization: Bearer <token>` en los headers de las peticiones protegidas.

---

## Mascotas (Pets)

### GET /pets
Obtiene la lista completa de todas las mascotas registradas.

**Acceso**: Público

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Max",
    "species": "Perro",
    "breed": "Golden Retriever",
    "age": 3,
    "gender": "Macho",
    "location": "Santiago",
    "photos": [
      "https://example.com/max1.jpg",
      "https://example.com/max2.jpg"
    ],
    "status": "available"
  },
  {
    "id": 2,
    "name": "Luna",
    "species": "Gato",
    "breed": "Persa",
    "age": 2,
    "gender": "Hembra",
    "location": "Valparaíso",
    "photos": ["https://example.com/luna.jpg"],
    "status": "available"
  }
]
```

---

### POST /pets
Registra una nueva mascota disponible para adopción.

**Acceso**: Protegido (requiere JWT)

**Headers Requeridos**:
- `Authorization: Bearer <token>`
- `Content-Type: application/json`

**Request Body** (requeridos campos: name, species, breed, age, gender):
```json
{
  "name": "Max",
  "species": "Perro",
  "breed": "Golden Retriever",
  "age": 3,
  "gender": "Macho",
  "location": "Santiago",
  "photos": [
    "https://example.com/max1.jpg",
    "https://example.com/max2.jpg"
  ]
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "name": "Max",
  "species": "Perro",
  "breed": "Golden Retriever",
  "age": 3,
  "gender": "Macho",
  "location": "Santiago",
  "photos": ["https://example.com/max1.jpg", "https://example.com/max2.jpg"],
  "status": "available"
}
```

**Response** (400 Bad Request):
```json
{
  "message": "Error al registrar mascota: null - entity needs to have a primary key"
}
```

---

### GET /pets/{id}
Obtiene la información detallada de una mascota específica.

**Acceso**: Público

**Path Parameters**:
- `id` (integer): ID de la mascota

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "Max",
  "species": "Perro",
  "breed": "Golden Retriever",
  "age": 3,
  "gender": "Macho",
  "location": "Santiago",
  "photos": ["https://example.com/max1.jpg"],
  "status": "available"
}
```

**Response** (404 Not Found): Mascota no encontrada

---

### PUT /pets/{id}
Actualiza la información de una mascota existente.

**Acceso**: Protegido (requiere JWT)

**Headers Requeridos**:
- `Authorization: Bearer <token>`
- `Content-Type: application/json`

**Path Parameters**:
- `id` (integer): ID de la mascota a actualizar

**Request Body** (todos los campos son opcionales):
```json
{
  "status": "adopted",
  "age": 4,
  "location": "Valparaíso"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "Max",
  "species": "Perro",
  "breed": "Golden Retriever",
  "age": 4,
  "gender": "Macho",
  "location": "Valparaíso",
  "photos": ["https://example.com/max1.jpg"],
  "status": "adopted"
}
```

**Response** (404 Not Found): Mascota no encontrada

---

### DELETE /pets/{id}
Elimina un registro de mascota del sistema.

**Acceso**: Protegido (requiere JWT)

**Headers Requeridos**:
- `Authorization: Bearer <token>`

**Path Parameters**:
- `id` (integer): ID de la mascota a eliminar

**Response** (200 OK):
```json
{
  "message": "Mascota eliminada exitosamente"
}
```

**Response** (404 Not Found): Mascota no encontrada

---

### GET /pets/available
Obtiene la lista de mascotas disponibles para adopción (status = "available").

**Acceso**: Público

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Max",
    "species": "Perro",
    "breed": "Golden Retriever",
    "age": 3,
    "gender": "Macho",
    "location": "Santiago",
    "photos": ["https://example.com/max1.jpg"],
    "status": "available"
  }
]
```

---

### GET /pets/search
Busca mascotas por uno o varios criterios. Todos los parámetros son opcionales pero se puede combinar varios para búsquedas más específicas.

**Acceso**: Público

**Query Parameters**:
- `species` (string, opcional): Filtrar por especie (ej: "Perro", "Gato")
- `gender` (string, opcional): Filtrar por sexo ("Macho" o "Hembra")
- `location` (string, opcional): Filtrar por ubicación (ej: "Santiago")
- `age` (integer, opcional): Filtrar por edad exacta (ej: 3)
- `status` (string, opcional): Filtrar por estado ("available" o "adopted", default: "available")

**Ejemplos de uso**:

1. **Buscar todos los perros disponibles**:
   ```
   GET /pets/search?species=Perro
   ```

2. **Buscar perros machos de 3 años**:
   ```
   GET /pets/search?species=Perro&gender=Macho&age=3
   ```

3. **Buscar mascotas en Santiago**:
   ```
   GET /pets/search?location=Santiago
   ```

4. **Buscar gatos hembras en Valparaíso disponibles**:
   ```
   GET /pets/search?species=Gato&gender=Hembra&location=Valparaíso&status=available
   ```

5. **Buscar todas las mascotas adoptadas**:
   ```
   GET /pets/search?status=adopted
   ```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Max",
    "species": "Perro",
    "breed": "Golden Retriever",
    "age": 3,
    "gender": "Macho",
    "location": "Santiago",
    "photos": ["https://example.com/max1.jpg"],
    "status": "available"
  }
]
```

---

## Pacientes (Patients)

### GET /patients
Obtiene la lista completa de pacientes registrados.

**Acceso**: Protegido (requiere JWT)

**Headers Requeridos**:
- `Authorization: Bearer <token>`

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Firulais",
    "species": "Perro",
    "breed": "Labrador",
    "age": 5,
    "owner": "Juan Pérez"
  }
]
```

---

### POST /patients
Registra un nuevo paciente en el sistema.

**Acceso**: Protegido (requiere JWT)

**Headers Requeridos**:
- `Authorization: Bearer <token>`
- `Content-Type: application/json`

**Request Body** (requeridos: name, species, breed, age, owner):
```json
{
  "name": "Firulais",
  "species": "Perro",
  "breed": "Labrador",
  "age": 5,
  "owner": "Juan Pérez"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "name": "Firulais",
  "species": "Perro",
  "breed": "Labrador",
  "age": 5,
  "owner": "Juan Pérez"
}
```

**Response** (400 Bad Request):
```json
{
  "message": "Error al registrar: Campo requerido"
}
```

---

### GET /patients/{id}
Obtiene la información de un paciente específico.

**Acceso**: Protegido (requiere JWT)

**Headers Requeridos**:
- `Authorization: Bearer <token>`

**Path Parameters**:
- `id` (integer): ID del paciente

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "Firulais",
  "species": "Perro",
  "breed": "Labrador",
  "age": 5,
  "owner": "Juan Pérez"
}
```

**Response** (404 Not Found): Paciente no encontrado

---

## Códigos de Respuesta

| Código | Descripción |
|--------|-------------|
| 200 | OK - Operación exitosa |
| 201 | Created - Recurso creado exitosamente |
| 400 | Bad Request - Datos inválidos o incompletos |
| 404 | Not Found - Recurso no encontrado |
| 500 | Internal Server Error - Error en el servidor |

---

## Ejemplos de Uso

### Flujo Completo: Login y Crear Mascota

**1. Autenticarse**:
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

Respuesta:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**2. Crear una nueva mascota (usando el token)**:
```bash
curl -X POST http://localhost:8080/pets \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Max",
    "species": "Perro",
    "breed": "Golden Retriever",
    "age": 3,
    "gender": "Macho",
    "location": "Santiago",
    "photos": ["https://example.com/max.jpg"]
  }'
```

**3. Buscar mascotas por criterios**:
```bash
curl -X GET "http://localhost:8080/pets/search?species=Perro&gender=Macho&location=Santiago" \
  -H "Accept: application/json"
```

**4. Obtener mascota específica**:
```bash
curl -X GET http://localhost:8080/pets/1 \
  -H "Accept: application/json"
```

**5. Actualizar estado de mascota (adoptada)**:
```bash
curl -X PUT http://localhost:8080/pets/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{"status": "adopted"}'
```

---

## Acceso a Documentación Interactiva

Una vez que el servidor está corriendo, puedes acceder a:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs JSON**: `http://localhost:8080/v3/api-docs`
- **API Docs YAML**: `http://localhost:8080/v3/api-docs.yaml`

---

## Notas Importantes

1. **Autenticación**: Los endpoints de creación, actualización y eliminación requieren un token JWT válido.
2. **Búsqueda pública**: Los usuarios pueden buscar mascotas sin autenticación.
3. **Status de mascotas**: Por default es "available". Puedes cambiar a "adopted" cuando sea adoptada.
4. **Fotos**: Se aceptan arrays de URLs. Puedes tener múltiples fotos por mascota.
5. **Ubicación**: Campo opcional pero recomendado para búsquedas geográficas.

---

Generado para: DUOC UC - CDY2203
Versión: 1.0.0
Fecha: 2026-03-26
