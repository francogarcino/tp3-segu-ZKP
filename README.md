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
- Endpoints de prueba con grupo matemático pequeño para testing

## Requisitos previos

- Java 17 o superior
- Gradle (incluido wrapper)

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

### POST `/auth/register`

Registra un nuevo usuario con su verificador público.

**Request Body:**
```json
{
  "username": "string",
  "vHex": "string"
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `username` | string | Nombre de usuario a registrar |
| `vHex` | string | Verificador público `v = g^x mod p` en formato hexadecimal |

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
  "tHex": "string"
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `username` | string | Nombre de usuario |
| `tHex` | string | Commitment `t = g^r mod p` en formato hexadecimal |

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
  "sHex": "string",
  "tHex": "string"
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `username` | string | Nombre de usuario |
| `sHex` | string | Respuesta `s = r + c * x mod q` en formato hexadecimal |
| `tHex` | string | Commitment original (para referencia) |

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

## Endpoints de prueba

Los siguientes endpoints utilizan un grupo matemático pequeño (p=23) y un challenge fijo (c=5) para facilitar las pruebas y debugging.

### POST `/auth/test/start`

Igual que `/auth/start` pero con challenge fijo.

**Request Body:**
```json
{
  "username": "string",
  "tHex": "string"
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
  "sHex": "string",
  "tHex": "string"
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

### Registrar usuario
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "vHex": "a1b2c3d4"}'
```

### Iniciar autenticación
```bash
curl -X POST http://localhost:8080/auth/start \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "tHex": "e5f6a7b8"}'
```

### Completar autenticación
```bash
curl -X POST http://localhost:8080/auth/finish \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "sHex": "9c8d7e6f", "tHex": "e5f6a7b8"}'
```

## Parámetros criptográficos

### Grupo productivo (SmallGroup)
- `p`: Primo de aproximadamente 423 bits  
  `13407807929942597099574024998205846127479365820592393377723561443721764030073546976801874298166903427690031858186486050853753882811946569946433649006084171`
- `q`: Orden del subgrupo cíclico (en hexadecimal)  
  `FFFFFFFFFFFFFFFFFFFFFFFF99DEF836146BC9B1B4D22831`
- `g`: Generador del grupo = 5

### Grupo de prueba (MockedSmallGroup)
- `p`: 23 (primo pequeño para testing)
- `q`: 22 (orden del grupo multiplicativo mod 23)
- `g`: 5 (generador)

## Tecnologías utilizadas

- **Kotlin** - Lenguaje de programación
- **Spring Boot 3.2.2** - Framework web
- **Gradle** - Gestión de dependencias y build
