# ZKP Login Demo

## Descripción

Esta aplicación implementa un sistema de autenticación basado en **Zero-Knowledge Proof (ZKP)** o **Prueba de Conocimiento Cero**. Permite que un usuario demuestre que conoce una contraseña (secreto) sin revelarla al servidor.

El protocolo ZKP utilizado se basa en el esquema de Schnorr, donde:
- El **prover** (cliente) demuestra conocimiento de un secreto `x` sin revelarlo
- El **verifier** (servidor) verifica la prueba sin aprender nada sobre el secreto

### Características principales

- Autenticación segura sin transmisión de contraseñas
- Implementación del protocolo Schnorr ZKP
- API REST para registro y autenticación
- Documentación interactiva con Swagger UI
- Endpoints de prueba con grupo matemático pequeño para testing
- Cliente Python interactivo (REPL)

## Requisitos previos

- Java 17 o superior
- Gradle (incluido wrapper)
- Python 3 con `requests` (para el cliente Python)

## Pasos para ejecutar la aplicación

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd ZKPLoginDemo
```

### 2. Compilar el proyecto

```bash
./gradlew build
```

### 3. Ejecutar la aplicación

```bash
./gradlew bootRun
```

La aplicación se iniciará en `http://localhost:8080`

### 4. Acceder a la documentación Swagger

Una vez iniciada la aplicación, puedes acceder a la documentación interactiva de la API en:

```
http://localhost:8080/swagger-ui.html
```

## Cliente Python (REPL)

Se incluye un cliente interactivo en Python para probar la autenticación ZKP.

### Instalación de dependencias

```bash
pip install requests
```

### Ejecución

```bash
python src/resources/consumer.py
```

### Comandos disponibles

| Comando | Descripción |
|---------|-------------|
| `register` | Registrar un nuevo usuario |
| `login` | Autenticarse con un usuario existente |
| `login --f` | Intentar login con fallo forzado (para testing) |
| `privacy true/false` | Activar/desactivar modo privacidad (ocultar secretos) |
| `setTest true/false` | Cambiar entre endpoints de producción y test |
| `debug true/false` | Activar/desactivar modo debug (muestra valores criptográficos durante login) |
| `quit` | Salir del programa |

## Flujo de autenticación ZKP

### Registro de usuario

1. El cliente genera un secreto `x` (derivado de la contraseña)
2. El cliente calcula `v = g^x mod p` (verificador público)
3. El cliente envía `username` y `v` al servidor

### Autenticación (Protocolo Schnorr)

1. **Inicio de autenticación:**
   - El cliente genera un valor aleatorio `r`
   - El cliente calcula `t = g^r mod p` (commitment)
   - El cliente envía `username` y `t` al servidor
   - El servidor responde con un challenge aleatorio `c`

2. **Finalización de autenticación:**
   - El cliente calcula `s = r + c * x mod q` (respuesta)
   - El cliente envía `s` al servidor
   - El servidor verifica que `g^s == t * v^c mod p`

## Endpoints de la API

### Base URL
```
http://localhost:8080/auth
```

---

### GET `/auth/vars`

Obtiene los parámetros criptográficos del grupo utilizado.

**Response:**
```json
{
  "primeModulusP": "16404534621550038323",
  "subgroupOrderQ": "8202267310775019161",
  "generatorG": "4"
}
```

---

### POST `/auth/register`

Registra un nuevo usuario con su verificador público.

**Request Body:**
```json
{
  "username": "string",
  "verifierHex": "string"
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `username` | string | Nombre de usuario a registrar |
| `verifierHex` | string | Verificador público `v = g^x mod p` en formato hexadecimal |

**Response:**
```json
{
  "ok": true,
  "message": "User registered"
}
```

---

### POST `/auth/start`

Inicia el proceso de autenticación. El servidor devuelve un challenge aleatorio.

**Request Body:**
```json
{
  "username": "string",
  "commitmentHex": "string"
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `username` | string | Nombre de usuario |
| `commitmentHex` | string | Commitment `t = g^r mod p` en formato hexadecimal |

**Response:**
```json
{
  "challengeHex": "string"
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `challengeHex` | string | Challenge aleatorio `c` en formato hexadecimal |

---

### POST `/auth/finish`

Completa el proceso de autenticación enviando la respuesta al challenge.

**Request Body:**
```json
{
  "username": "string",
  "responseHex": "string",
  "commitmentHex": "string"
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `username` | string | Nombre de usuario |
| `responseHex` | string | Respuesta `s = r + c * x mod q` en formato hexadecimal |
| `commitmentHex` | string | Commitment original (para referencia) |

**Response:**
```json
{
  "ok": true,
  "message": "Authentication success"
}
```

o en caso de fallo:
```json
{
  "ok": false,
  "message": "Authentication FAILED"
}
```

---

## Endpoints de prueba (ocultos en Swagger)

Los siguientes endpoints utilizan un grupo matemático pequeño (p=23, q=11, g=2) y un challenge fijo (c=5) para facilitar las pruebas y debugging.

### GET `/auth/test/vars`

Obtiene los parámetros del grupo de prueba.

**Response:**
```json
{
  "primeModulusP": "23",
  "subgroupOrderQ": "11",
  "generatorG": "2"
}
```

---

### POST `/auth/test/start`

Igual que `/auth/start` pero con challenge fijo.

**Request Body:**
```json
{
  "username": "string",
  "commitmentHex": "string"
}
```

**Response:**
```json
{
  "challengeHex": "5"
}
```

---

### POST `/auth/test/finish`

Igual que `/auth/finish` pero usando el grupo pequeño para verificación.

**Request Body:**
```json
{
  "username": "string",
  "responseHex": "string",
  "commitmentHex": "string"
}
```

**Response:**
```json
{
  "ok": true,
  "message": "Authentication success"
}
```

---

## Ejemplo de uso con cURL

### Obtener parámetros criptográficos
```bash
curl http://localhost:8080/auth/vars
```

### Registrar usuario
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "verifierHex": "a1b2c3d4"}'
```

### Iniciar autenticación
```bash
curl -X POST http://localhost:8080/auth/start \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "commitmentHex": "e5f6a7b8"}'
```

### Completar autenticación
```bash
curl -X POST http://localhost:8080/auth/finish \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "responseHex": "9c8d7e6f", "commitmentHex": "e5f6a7b8"}'
```

## Parámetros criptográficos

### Grupo productivo (SmallGroup)
- `primeModulusP`: `16404534621550038323`
- `subgroupOrderQ`: `8202267310775019161`
- `generatorG`: `4`

### Grupo de prueba (MockedSmallGroup)
- `primeModulusP`: `23`
- `subgroupOrderQ`: `11`
- `generatorG`: `2`

## Tecnologías utilizadas

- **Kotlin** - Lenguaje de programación
- **Spring Boot 3.2.2** - Framework web
- **SpringDoc OpenAPI 2.5.0** - Documentación Swagger
- **Gradle** - Gestión de dependencias y build
- **Python** - Cliente REPL
