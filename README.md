# 🎲 Parchís Clásico — Arquitectura de Software

<div align="center">

![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Apache%20Maven-3.9%2B-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Java%20Swing-007396?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/Database-MySQL%208.4%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC%20%7C%20Broker%20%7C%20Facade%20%7C%20Observer-4E73DF?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Completed%20%26%20Tested-brightgreen?style=for-the-badge)

<p align="center">
  <b>Una recreación digital fiel, interactiva, distribuida y arquitectónicamente robusta del tradicional juego de mesa Parchís.</b>
  <br />
  Diseñado para 2 a 4 jugadores con comunicación de red LAN por sockets TCP, servidor Message Broker concurrente multihilo, autodescubrimiento UDP, persistencia relacional MySQL, inteligencia artificial de Bots autónomos y renderizado vectorial de alto rendimiento.
  <br />
  <i>Proyecto desarrollado para la materia de <b>Arquitectura de Software</b>.</i>
</p>

</div>

---

## 👥 Equipo de Desarrollo

Proyecto desarrollado de manera colaborativa utilizando Git con ramas de características independientes (`feature branches`) y flujo de Pull Requests para la materia de **Arquitectura de Software**:

| Integrante | ID / Matrícula | Usuario GitHub | Rol en el Proyecto | Rama Asignada |
|---|:---:|:---:|---|:---:|
| **Gibran Alonso Ibarra Palomares** | `267883` | [@wareon711](https://github.com/wareon711) | **Motor Matemático del Parchís, IA de Bots y Partida:**<br>Motor de reglas de juego (`FachadaReglasMock`), heurística de toma de decisiones de Bots (`BotJugador`), renderizado vectorial y transformaciones trigonométricas del tablero (`PanelTablero`), controladores de partida en tiempo real (`VistaPartida`, `ControladorPartida`, `ModeloPartida`). | `feature/motor-reglas-y-tablero` |
| **Héctor Eduardo Rojo Cota** | `271229` | [@Eduardx77](https://github.com/Eduardx77) | **Servidor Message Broker, Sockets LAN y Persistencia MySQL:**<br>Servidor Message Broker concurrente multihilo (`ParchisMessageBroker`), protocolo de sockets TCP y descubrimiento LAN UDP (`MatchmakingLocal`, `ComunicacionBroker`), capa de persistencia relacional JDBC (`ConexionBD`, `PerfilDAOMySQL`), cifrado y configuración de arranque `Main`. | `feature/app-y-dto` |
| **Mía Anangel Nieblas Hollman** | `267653` | [@angeltqm](https://github.com/angeltqm) | **Capa de Negocios, Validaciones, Gestión de Salas y DTOs:**<br>Lógica de negocio (`PerfilBO`), subsistema de validaciones con excepciones personalizadas (`NegocioException`, `SalaException`), gestión de salas de espera (`ControlSala`, `VistaSalaEspera`, `FachadaSala`) y contratos DTO serializables. | `feature/negocios-y-control` |
| **Adilene Sillas Gaxiola** | `269274` | [@Adilenes07](https://github.com/Adilenes07) | **Capa de Presentación Visual, Recursos y Documentación:**<br>Pantallas de interfaz gráfica NetBeans Swing (`VistaInicioSesion`, `VistaRegistro`, `VistaModoGuest`, `VistaInicio`, `DialogPerfil`, `VistaSeleccionarEntrada`, `VistaResultados`), componentes visuales reutilizables (`BotonPill`, `PanelFondo`), diseño de assets gráficos y documentación. | `feature/vistas-y-autenticacion` |

---

## 📖 Tabla de Contenidos

1. [Equipo de Desarrollo](#-equipo-de-desarrollo)
2. [Descripción General](#-descripción-general)
3. [Características Principales](#-características-principales)
4. [Arquitectura y Patrones de Diseño](#-arquitectura-y-patrones-de-diseño)
5. [Estructura del Proyecto](#-estructura-del-proyecto)
6. [Reglamento Oficial Implementado](#-reglamento-oficial-implementado)
7. [Requisitos e Instalación](#-requisitos-e-instalación)
8. [Cómo Jugar](#-cómo-jugar)

---

## 🌟 Descripción General

Este proyecto implementa el tradicional juego de mesa **Parchís** (derivado del clásico Pachisi de la India). Cada jugador comanda un ejército de **4 fichas** con el objetivo estratégico de sacarlas de su base, recorrer un circuito cerrado de 52 casillas en el tablero, sortear bloqueos rivales, capturar piezas enemigas y ascender por su pasillo de color hasta alcanzar la meta central.

El sistema fue desarrollado con un fuerte énfasis en la **separación de responsabilidades**, **desacoplamiento modular**, **concurrencia de red** y **diseño estructural detallado**, empleando el patrón **MVC**, el patrón **Message Broker** para eventos distribuidos, el patrón **Fachada** para aislar la lógica de negocio y el patrón **Observador** para sincronizar la interfaz en tiempo real.

---

## ✨ Características Principales

### 🎲 Motor de Reglas Exhaustivo
* **Salida de Base Inteligente:** Requiere un 5 en el dado para posicionar una ficha en la casilla de salida correspondiente.
* **Control de Capacidad en Casillas:** Control estricto de un máximo de 2 fichas por casilla en todo el circuito.
* **Sistema de Barreras (Bloqueos):** Dos fichas en una misma casilla (segura o regular) generan un puente que impide tanto aterrizar en ella como saltar por encima a cualquier oponente.
* **Mecánica de Capturas ("Comer Ficha"):** Aterrizar sobre una casilla regular ocupada por un rival envía su ficha de vuelta a su base y otorga 20 casillas de bonificación.
* **Zonas Seguras Inmunes:** 8 casillas de seguro donde conviven fichas rivales sin riesgo de captura.
* **Bonificaciones y Seises:** Sacar un 6 permite repetir turno de inmediato, con penalización y cambio de turno al encadenar 3 seises seguidos.
* **Carril de Llegada y Meta:** Requiere tirada exacta para coronar en la casilla 56, premiando con 10 casillas de bonificación.
* **Control de Inactividad (AFK):** Sistema de tolerancia con advertencias automáticas y penalización si un jugador agota su tiempo de turno.

### 🤖 Inteligencia Artificial (Bots Autónomos)
* **Heurística de Evaluación:** Los bots evalúan amenazas y oportunidades en cada tirada: priorizan coronar fichas en meta, abrir barreras, capturar fichas enemigas y avanzar de forma segura.
* **Relleno Automático de Salas:** En partidas de 4 jugadores, si se unen 2 jugadores reales la sala se completa con 2 bots; si se unen 3 jugadores reales, se completa con 1 bot para garantizar partidas dinámicas e inmediatas.
* **Ejecución Asíncrona:** Simulación de tiempo de deliberación humano en hilos secundarios para mantener la fluidez visual sin congelar la interfaz.

### 🌐 Conectividad LAN y Servidor Message Broker
* **Arquitectura Orientada a Mensajes:** Servidor `ParchisMessageBroker` multihilo que desacopla la comunicación entre los clientes mediante sockets TCP thread-safe.
* **Descubrimiento LAN Automático:** Mediante datagramas UDP broadcast (`:27016`), los clientes en la misma red local detectan servidores activos sin necesidad de ingresar direcciones IP manualmente.

### 💾 Persistencia Relacional con MySQL
* **Autenticación Segura:** Registro e inicio de sesión de usuarios con contraseñas cifradas y almacenamiento de perfiles.
* **Modo Invitado (Guest):** Permite jugar partidas locales inmediatas sin requerir registro previo en base de datos.
* **Pool JDBC y Transacciones:** Gestión eficiente de conexiones mediante `ConexionBD` y patrón DAO (`IPerfilDAO`, `PerfilDAOMySQL`).

### 🎨 Experiencia Visual y Gráfica (Swing / Graphics2D)
* **Tablero Vectorial Dinámico:** Renderizado nítido con antialiasing (`RenderingHints.KEY_ANTIALIASING`), matrices de rotación trigonométricas en radianes y cálculo de superposición de fichas.
* **Animación Parabólica en Tiempo Real:** Las fichas avanzan casilla por casilla con una animación de salto fluida a 60 FPS mediante `javax.swing.Timer`.
* **Chat Rápido y Expresiones:** Menú emergente de reacciones rápidas y control de audio integrado en la interfaz de juego.

---

## 🏛 Arquitectura y Patrones de Diseño

El sistema está organizado bajo una arquitectura en capas limpiamente desacoplada:

```
┌─────────────────────────────────────────────────────────────┐
│                       app.Main                              │
│             (Punto de entrada de la aplicación)             │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                     │
│  ┌───────────────────────┐       ┌───────────────────────┐  │
│  │     VistaPartida      │◄──────┤   ControladorPartida  │  │
│  │ (PanelTablero/Dados)  │       │  (Maneja eventos GUI) │  │
│  └───────────┬───────────┘       └───────────┬───────────┘  │
│              │                               │              │
│              ▼                               ▼              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                    ModeloPartida                      │  │
│  │       (Observable - Notifica cambios de estado)       │  │
│  └───────────────────────────┬───────────────────────────┘  │
└──────────────────────────────┼──────────────────────────────┘
                               │ usa
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      CAPA DE NEGOCIOS                       │
│  ┌───────────────────────┐       ┌───────────────────────┐  │
│  │    IFachadaReglas     │       │     IFachadaSala      │  │
│  │  (Motor del Parchís)  │       │  (Gestión de Espera)  │  │
│  └───────────▲───────────┘       └───────────▲───────────┘  │
│              │ implementa                    │ implementa   │
│  ┌───────────┴───────────┐       ┌───────────┴───────────┐  │
│  │   FachadaReglasMock   │       │      FachadaSala      │  │
│  │ (Reglas, IA Bots, AFK)│       │(Validaciones/Partida) │  │
│  └───────────────────────┘       └───────────────────────┘  │
└──────────────┬───────────────────────────────┬──────────────┘
               │                               │
               ▼                               ▼
┌─────────────────────────────┐ ┌─────────────────────────────┐
│      CAPA DE RED Y BROKER   │ │     CAPA DE PERSISTENCIA    │
│  ┌───────────────────────┐  │ │  ┌───────────────────────┐  │
│  │  ParchisMessageBroker │  │ │  │      ConexionBD       │  │
│  │ (Servidor Multihilo)  │  │ │  │   (Pool JDBC MySQL)   │  │
│  ├───────────────────────┤  │ │  ├───────────────────────┤  │
│  │   MatchmakingLocal    │  │ │  │    PerfilDAOMySQL     │  │
│  │ (Sockets LAN TCP/UDP) │  │ │  │  (Persistencia DAO)   │  │
│  └───────────────────────┘  │ │  └───────────────────────┘  │
└─────────────────────────────┘ └─────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                        CAPA DE DTOs                         │
│  ┌────────────────────┐ ┌──────────────┐ ┌───────────────┐  │
│  │  EstadoPartidaDTO  │ │   FichaDTO   │ │  JugadorDTO   │  │
│  ├────────────────────┤ ├──────────────┤ ├───────────────┤  │
│  │     SalaDTO        │ │  MensajeDTO  │ │   PerfilDTO   │  │
│  └────────────────────┘ └──────────────┘ └───────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Patrones de Diseño Aplicados:
1. **MVC (Modelo - Vista - Controlador):** `VistaPartida` delega las acciones de los botones y clics a `ControladorPartida`, quien actualiza el `ModeloPartida`.
2. **Message Broker (Publicador / Suscriptor):** Servidor centralizado `ParchisMessageBroker` que enruta eventos de red entre jugadores de manera desacoplada.
3. **Fachada (Facade Pattern):** Las interfaces `IFachadaReglas` e `IFachadaSala` ocultan la complejidad de la lógica interna al resto de capas.
4. **Observador (Observer Pattern):** La interfaz `IObservadorPartida` permite a los componentes visuales reaccionar reactivamente ante cualquier cambio de estado.
5. **Data Access Object (DAO):** `IPerfilDAO` encapsula el acceso a la base de datos relacional MySQL, desacoplando la lógica de negocio de las consultas SQL.
6. **Data Transfer Object (DTO):** Modelos de datos inmutables y serializables que aíslan los datos intercambiados entre la red, el negocio y la vista.

---

## 📁 Estructura del Proyecto

```bash
Parchis/
├── pom.xml                               # Configuración Maven (dependencias, plugins, Java 21)
├── nbactions.xml                         # Acciones de integración de Apache NetBeans
├── Parchis.jar                           # Ejecutable empaquetado Fat JAR listo para jugar
├── README.md                             # Documentación oficial del repositorio
└── src/
    └── main/
        ├── java/
        │   ├── app/
        │   │   └── Main.java             # Punto de arranque de la aplicación
        │   ├── datos/
        │   │   ├── ConexionBD.java       # Manejo de conexión JDBC con MySQL
        │   │   ├── IPerfilDAO.java       # Contrato de persistencia de perfiles
        │   │   ├── PerfilDAOMySQL.java   # Implementación DAO con consultas preparadas
        │   │   ├── PerfilDAOMemoria.java # Implementación DAO en memoria (fallback)
        │   │   ├── PerfilEntidad.java    # Entidad de base de datos
        │   │   └── PersistenciaException.java # Excepciones de acceso a datos
        │   ├── dto/
        │   │   ├── EstadoPartidaDTO.java # Snapshot completo del estado de la partida
        │   │   ├── FichaDTO.java         # Posición, color, casilla y estado de ficha
        │   │   ├── JugadorDTO.java       # Información de jugador, color y victorias
        │   │   ├── SalaDTO.java          # Datos de sala de espera y participantes
        │   │   ├── MensajeDTO.java       # Estructura de mensajes de red
        │   │   ├── PerfilDTO.java        # Datos de perfil de usuario
        │   │   └── TipoMensaje.java      # Enumeración de tipos de mensajes
        │   ├── negocios/
        │   │   ├── IFachadaReglas.java   # Contrato del motor de reglas del juego
        │   │   ├── FachadaReglasMock.java# Motor matemático de reglas, barreras y capturas
        │   │   ├── BotJugador.java       # Inteligencia artificial heurística de Bots
        │   │   ├── ParchisMessageBroker.java # Servidor Message Broker concurrente multihilo
        │   │   ├── MatchmakingLocal.java # Descubrimiento y conexión LAN TCP/UDP
        │   │   ├── ComunicacionBroker.java # Cliente de comunicación TCP con el Broker
        │   │   ├── ReceptorMensajes.java # Hilo receptor continuo de eventos de red
        │   │   ├── IFachadaSala.java     # Contrato de gestión de salas
        │   │   ├── FachadaSala.java      # Implementación de lógica de salas
        │   │   ├── PerfilBO.java         # Lógica de negocio de perfiles de usuario
        │   │   └── Validacion*.java      # Reglas de validación desacopladas
        │   └── presentacion/
        │       ├── VistaPartida.java     # Pantalla principal del juego (tablero, dados, chat)
        │       ├── PanelTablero.java     # Motor de renderizado vectorial y trigonometría
        │       ├── ControlDado.java      # Componente animado interactivo del dado
        │       ├── ControladorPartida.java# Controlador de eventos de partida
        │       ├── ModeloPartida.java    # Modelo observable de presentación
        │       ├── VistaInicio.java      # Menú principal de selección de modo
        │       ├── VistaInicioSesion.java# Pantalla de autenticación de usuario
        │       ├── VistaRegistro.java    # Pantalla de registro de nuevas cuentas
        │       ├── VistaModoGuest.java   # Entrada directa como invitado
        │       ├── VistaSalaEspera.java  # Sala de espera previa al inicio de partida
        │       ├── VistaResultados.java  # Pantalla de victoria / fin de partida
        │       ├── BotonPill.java        # Botón estilizado con gradientes y sombras
        │       └── SonidoUtil.java       # Manejador de efectos de audio
        └── resources/
            ├── conexion.properties       # Credenciales de conexión MySQL
            └── img/                      # Assets gráficos oficiales (tablero, avatares, dados)
```

---

## 📜 Reglamento Oficial Implementado

| Regla | Comportamiento en el Motor |
|---|---|
| **Salida de Ficha** | Se requiere obtener un **5** para liberar una ficha de la base a la casilla de salida de su color. |
| **Capacidad Máxima** | Una casilla solo puede contener un máximo de **2 fichas** simultáneamente. |
| **Barreras / Puentes** | Si una casilla tiene 2 fichas, **ninguna ficha (rival o propia) puede saltarla ni caer en ella**. |
| **Apertura de Barrera** | Si un jugador tiene una barrera y saca un 6, está obligado a abrirla moviendo una de sus fichas. |
| **Repetir Turno (6)** | Sacar un 6 otorga un turno consecutivo adicional tras mover ficha. |
| **Penalización de Seises** | Sacar **tres seises consecutivos** castiga al jugador regresando la última ficha movida a la base y cediendo el turno. |
| **Casillas Seguras** | Casillas especiales (`0, 8, 13, 21, 26, 34, 39, 47`) identificadas con círculo gris donde las fichas no pueden ser comidas. |
| **Captura de Fichas** | Caer en una casilla regular ocupada con una ficha contraria la captura, la envía de vuelta a su base y otorga **20 casillas de bonificación**. |
| **Pasillo de Meta** | Del paso 51 al 55 las fichas entran a su rampa privada protegida; no se permite saltar fichas en este tramo. |
| **Llegada a Meta** | La meta se ubica en la posición **56**. Requiere un número exacto para coronar y otorga **10 casillas de bonificación**. |
| **Condición de Victoria** | El primer jugador en colocar sus **4 fichas en la meta** gana automáticamente la partida. |

---

## 🚀 Requisitos e Instalación

### Requisitos Previos
* **Java Development Kit (JDK):** Versión 21 o superior.
* **Apache Maven:** 3.8.0 o superior (o usar el Maven integrado en NetBeans).
* **Base de Datos (Opcional para modo online):** MySQL Server 8.0 o superior (el archivo `src/main/resources/conexion.properties` contiene la configuración de conexión).
* **IDE Recomendado:** Apache NetBeans 19+, IntelliJ IDEA o VS Code con extensión Java.

### Clonar el Repositorio
```bash
git clone https://github.com/wareon711/Parchis.git
cd Parchis
```

### Ejecutar Directamente el JAR (Recomendado)
El proyecto incluye el empaquetado Fat JAR con todas las dependencias listas:
```bash
java -jar Parchis.jar
```
*(O simplemente haz doble clic sobre `Parchis.jar` en el explorador de archivos de Windows).*

### Compilar y Ejecutar con Maven
```bash
# Compilar y empaquetar el proyecto
mvn clean package

# Ejecutar mediante el plugin exec
mvn exec:java
```

### Ejecutar desde Apache NetBeans
1. Abre NetBeans y selecciona **File > Open Project...**
2. Selecciona la carpeta `Parchis` (reconocida automáticamente por el ícono de Maven `pom.xml`).
3. Presiona **`F6`** (o clic derecho sobre el proyecto > **Run**).

---

## 🎮 Cómo Jugar

1. **Ingreso:** Inicia sesión con tu cuenta, regístrate o accede directamente mediante el **Modo Invitado**.
2. **Selección de Modo:** Elige una partida de **2 Jugadores** (1 vs 1) o de **4 Jugadores** (todos contra todos).
3. **Sala de Espera y Matchmaking:** Al entrar a una sala, el sistema busca automáticamente compañeros en la red local. Si en una sala de 4 jugadores entran 2 o 3 jugadores reales, los puestos restantes se rellenan automáticamente con **Bots inteligentes**.
4. **Lanzar el Dado:** En tu turno, haz clic sobre el dado animado para tirar.
5. **Elegir Ficha:** Las fichas con movimientos legales parpadearán y se resaltarán; haz clic sobre la que desees desplazar.
6. **Chat Rápido:** Comunícate con los demás jugadores utilizando las reacciones rápidas del botón **Chat** en la esquina superior.
7. **Victoria:** ¡El primer jugador en introducir sus cuatro piezas al centro del tablero se corona campeón!

---

<div align="center">
  <sub>Proyecto desarrollado para la materia de <b>Arquitectura de Software</b>.</sub>
</div>
