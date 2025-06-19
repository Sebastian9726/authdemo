# Auth Demo - Spring Boot

**Prueba Técnica - Desarrollador Java Spring Boot**

API REST que permite autenticación de usuarios contra la API externa DummyJSON, registrando cada autenticación exitosa en una base de datos PostgreSQL.

## 🏗️ **Tecnologías utilizadas**

- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Data JPA**
- **PostgreSQL**
- **OpenFeign Client**
- **Lombok**
- **JUnit 5 & Mockito**

## 📋 **Requisitos previos**

1. **Java 21** o superior
2. **PostgreSQL** instalado y ejecutándose
3. **Maven 3.6+** (o usar Maven Wrapper incluido)

## 🗄️ **Configuración de base de datos**

### PostgreSQL Setup

1. **Crear base de datos:**
```sql
CREATE DATABASE dblocal;
CREATE USER admin WITH PASSWORD 'admin';
GRANT ALL PRIVILEGES ON DATABASE dblocal TO admin;
```

2. **Cadena de conexión configurada:**
```
postgresql://admin:admin@localhost:5432/dblocal
```

## 🚀 **Ejecución del proyecto**

### Con Maven Wrapper (recomendado):
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Con Maven instalado:
```bash
mvn spring-boot:run
```

El servidor estará disponible en: **http://localhost:8080**

## 📡 **Endpoints disponibles**

### 1. Autenticación de usuario
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "emilys",
    "password": "emilyspass"
}
```

**Ejemplo curl:**
```bash
curl --request POST \
  --url http://localhost:8080/api/auth/login \
  --header 'Content-Type: application/json' \
  --data '{
    "username": "emilys",
    "password": "emilyspass"
  }'
```

### 2. Obtener información del usuario autenticado
```http
GET /api/auth/me
Cookie: accesToken = {access_token}
```

**Ejemplo curl:**
```bash
curl --request GET \
  --url http://localhost:8080/api/auth/me \
  --header 'Cookie: accesToken = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
```

### 3. Obtener lista de usuarios disponibles
```http
GET /api/auth/users
```

**Ejemplo curl:**
```bash
curl --request GET \
  --url http://localhost:8080/api/auth/users
```

### 4. Consultar historial de login por usuario
```http
GET /api/auth/login-history/{username}
```

**Ejemplo curl:**
```bash
curl --request GET \
  --url http://localhost:8080/api/auth/login-history/emilys
```

### 5. Consultar todos los logs de login
```http
GET /api/auth/login-logs
```

**Ejemplo curl:**
```bash
curl --request GET \
  --url http://localhost:8080/api/auth/login-logs
```

## 👥 **Usuarios de prueba**

Puedes usar cualquiera de estos usuarios para probar la autenticación:

| Username    | Password      |
|-------------|---------------|
| emilys      | emilyspass    |
| michaelw    | michaelwpass  |
| sophiab     | sophiabpass   |
| jamesd      | jamesdpass    |
| emmaj       | emmajpass     |

Para obtener la lista completa de usuarios disponibles, usa el endpoint `/api/auth/users`.

## 🗃️ **Estructura de la base de datos**

### Tabla: `login_log`

| Campo         | Tipo          | Descripción                    |
|---------------|---------------|--------------------------------|
| id            | UUID          | Identificador único            |
| username      | VARCHAR       | Nombre de usuario              |
| login_time    | TIMESTAMP     | Fecha y hora del login         |
| access_token  | VARCHAR(1000) | Token de acceso de DummyJSON   |
| refresh_token | VARCHAR(1000) | Token de refresco de DummyJSON |

## 🔄 **Funcionamiento del sistema**

1. **Login:** El usuario envía credenciales a `/api/auth/login`
2. **Validación:** La aplicación valida contra `https://dummyjson.com/auth/login`
3. **Registro:** Si es exitoso, se guarda el registro en la tabla `login_log`
4. **Respuesta:** Se retorna la información del usuario y tokens
5. **Consulta:** Se puede usar el `access_token` para obtener información del usuario

## 📸 **Evidencias del funcionamiento**

### 1. Autenticación exitosa (POST /api/auth/login)

**Petición:**
```json
{
    "username": "sophiab",
    "password": "sophiabpass"
}
```

**Respuesta exitosa (200 OK):**
```json
{
    "id": 3,
    "username": "sophiab",
    "email": "sophia.brown@x.dummyjson.com",
    "firstName": "Sophia",
    "lastName": "Brown",
    "gender": "female",
    "image": "https://dummyjson.com/icon/sophiab/128",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

![Login exitoso - 200 OK]

![login](https://github.com/user-attachments/assets/bb6e1f85-c765-48d0-b98b-3a5de89f2eda)


### 2. Respuesta personalizada de error (GET /api/auth/me sin token)

**Petición sin token de autorización:**
```http
GET /api/auth/me
```

**Respuesta de error personalizada (401 Unauthorized):**
```json
{
    "error": "UNAUTHORIZED",
    "message": "Token de acceso requerido. Por favor, proporciona un token válido.",
    "code": "AUTH_001",
    "timestamp": 1750299731161
}
```

### 3. Persistencia en base de datos

Los registros de login se guardan exitosamente en la tabla `login_log` de PostgreSQL:

![Registros en base de datos]

![db](https://github.com/user-attachments/assets/04d4de9e-bfdf-432b-9e27-913977f809a7)

**Ejemplo de registro guardado:**
- **ID:** 9a722491-bfec-494b-85eb-17f6d414255c
- **Username:** emilys
- **Access Token:** eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
- **Refresh Token:** eyJpZCI6MSwid...
- **Login Time:** 2024-12-18 15:42:11

### 4. Mejoras implementadas

#### Manejo de errores personalizado
- Se creó la clase `ErrorResponse` para proporcionar respuestas de error estructuradas
- Incluye código de error único (`AUTH_001`, `AUTH_002`, etc.)
- Mensajes descriptivos en español
- Timestamp del momento del error

#### Características técnicas destacadas
- ✅ **Autenticación exitosa** contra DummyJSON API
- ✅ **Persistencia** de logs en PostgreSQL
- ✅ **Respuestas personalizadas** de error
- ✅ **Validación** de tokens de acceso
- ✅ **Logging detallado** para debugging
- ✅ **Arquitectura SOLID** y buenas prácticas

## 🧪 **Ejecutar pruebas**

```bash
# Ejecutar todas las pruebas
./mvnw test

# Ejecutar solo las pruebas del servicio
./mvnw test -Dtest=AuthServiceTest
```

## 📊 **Principios SOLID aplicados**

- **S - Single Responsibility:** Cada clase tiene una responsabilidad específica
- **O - Open/Closed:** Servicios extensibles sin modificar código existente
- **L - Liskov Substitution:** Interfaces bien definidas para Feign Client
- **I - Interface Segregation:** Interfaces específicas para cada funcionalidad
- **D - Dependency Inversion:** Inyección de dependencias con Spring

## 🏛️ **Arquitectura del proyecto**

```
src/main/java/
├── controller/     # Controladores REST
├── service/        # Lógica de negocio
├── repository/     # Acceso a datos JPA
├── entity/         # Entidades JPA
├── dto/           # Data Transfer Objects
├── client/        # Feign Clients
└── AuthdemoApplication.java

src/test/java/
└── service/       # Pruebas unitarias
```

## 🔍 **Logs y monitoreo**

La aplicación incluye logging detallado:
- Intentos de autenticación
- Respuestas de DummyJSON API
- Guardado de logs en base de datos
- Errores y excepciones

## 🛠️ **Desarrollo**

### Agregar nuevas funcionalidades:

1. **Nuevo endpoint:** Crear método en `AuthController`
2. **Nueva lógica:** Implementar en `AuthService`
3. **Nuevos datos:** Agregar entidad JPA si es necesario
4. **Testing:** Crear prueba unitaria correspondiente

### Variables de entorno (opcional):

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/dblocal
export SPRING_DATASOURCE_USERNAME=admin
export SPRING_DATASOURCE_PASSWORD=admin
```

## 📝 **Notas técnicas**

- Los tokens tienen formato JWT
- La aplicación maneja automáticamente la creación de tablas (DDL)
- Se incluye CORS habilitado para desarrollo frontend
- Timeouts configurados para Feign Client (5s conexión, 10s lectura)

## 🤝 **Autor**

**Prueba Técnica desarrollada por:** JUAN SEBASTIAN TRASLAVIÑA DAVILA  
**Email:** jtraslavina300@unab.edu.co  
**Fecha:** Junio 2025 
