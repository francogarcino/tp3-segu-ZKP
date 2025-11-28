# ZKP Login Demo

## Descripción de la Aplicación

Esta aplicación es una demostración de autenticación mediante **Zero-Knowledge Proof (ZKP)** utilizando el protocolo de Schnorr. El sistema permite a los usuarios registrarse y autenticarse sin revelar su secreto (contraseña) al servidor.

### Componentes

- **Backend (Kotlin/Spring Boot)**: Servidor que expone endpoints REST para el registro y autenticación de usuarios mediante ZKP.
- **Cliente (Python)**: Script REPL interactivo que permite a los usuarios interactuar con el servidor para registrarse y autenticarse.

### ¿Cómo funciona ZKP?

1. **Registro**: El usuario elige un secreto `x` y calcula un verificador `v = g^x mod p`, que se envía al servidor. El servidor almacena solo el verificador, nunca el secreto.

2. **Autenticación**: 
   - El cliente genera un número aleatorio `r` y envía el commitment `t = g^r mod p` al servidor.
   - El servidor responde con un desafío aleatorio `c`.
   - El cliente calcula la respuesta `s = r + c*x mod q` y la envía al servidor.
   - El servidor verifica que `g^s mod p == t * v^c mod p`. Si es correcto, la autenticación es exitosa.

---

## Pasos para Utilizar la Aplicación

### Requisitos Previos

- **Java 17** o superior
- **Python 3** con el paquete `requests` instalado
- **Gradle** (incluido como wrapper en el proyecto)

### 1. Iniciar el Servidor (Backend)

```bash
# Desde el directorio raíz del proyecto
./gradlew bootRun
```

El servidor se iniciará en `http://localhost:8080`.

### 2. Ejecutar el Cliente Python

```bash
# Instalar dependencias de Python (si es necesario)
pip install requests

# Ejecutar el cliente REPL
python src/resources/consumer.py
```

### 3. Comandos del Cliente REPL

| Comando | Descripción |
|---------|-------------|
| `register` | Registrar un nuevo usuario con un secreto |
| `login` | Iniciar sesión con ZKP |
| `login --f` | Intentar login con fallo forzado (para pruebas) |
| `privacy true/false` | Activar/desactivar modo privacidad (ocultar secreto al escribir) |
| `debug true/false` | Activar/desactivar modo debug (mostrar información detallada) |
| `setTest true/false` | Usar endpoints de prueba con valores mockeados |
| `quit` | Salir del cliente |

### Ejemplo de Uso

```bash
> register
=== REGISTER ===
Username: usuario1
Secret x: 12345
REGISTER RESPONSE: {'ok': True, 'message': 'User registered'}

> login
=== LOGIN ===
Username: usuario1
Secret x: 12345
FINISH RESPONSE: {'ok': True, 'message': 'Authentication success'}
```

---

## Endpoints de la API

### Base URL: `http://localhost:8080/auth`

### 1. Obtener Parámetros del Grupo

```
GET /auth/vars
```

**Descripción**: Retorna los parámetros criptográficos del grupo utilizado para ZKP.

**Respuesta**:
```json
{
  "primeModulusP": "16404534621550038323",
  "subgroupOrderQ": "8202267310775019161",
  "generatorG": "4"
}
```

### 2. Registrar Usuario

```
POST /auth/register
```

**Descripción**: Registra un nuevo usuario con su verificador.

**Body**:
```json
{
  "username": "string",
  "verifierHex": "string"  // v = g^x mod p en hexadecimal
}
```

**Respuesta**:
```json
{
  "ok": true,
  "message": "User registered"
}
```

### 3. Iniciar Autenticación

```
POST /auth/start
```

**Descripción**: Inicia el proceso de autenticación ZKP. El cliente envía su commitment y recibe un desafío.

**Body**:
```json
{
  "username": "string",
  "commitmentHex": "string"  // t = g^r mod p en hexadecimal
}
```

**Respuesta**:
```json
{
  "challengeHex": "string"  // Desafío c en hexadecimal
}
```

### 4. Finalizar Autenticación

```
POST /auth/finish
```

**Descripción**: Completa el proceso de autenticación ZKP. El cliente envía la respuesta al desafío.

**Body**:
```json
{
  "username": "string",
  "responseHex": "string",    // s = r + c*x mod q en hexadecimal
  "commitmentHex": "string"   // t (el mismo commitment enviado en /start)
}
```

**Respuesta**:
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

## Endpoints de Prueba (Test)

Los siguientes endpoints utilizan valores mockeados para facilitar pruebas manuales:

- `GET /auth/test/vars` - Parámetros mockeados (p=23, q=11, g=2)
- `POST /auth/test/start` - Inicio de autenticación con desafío fijo (c=5)
- `POST /auth/test/finish` - Finalización de autenticación con valores mockeados

---

## Documentación Swagger

La documentación interactiva de la API está disponible en:

```
http://localhost:8080/swagger-ui.html
```
