# 🎲 Parchís Clásico — Arquitectura Orientada a Componentes & MVC

<div align="center">

![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Apache%20Maven-3.9%2B-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Java%20Swing-007396?style=for-the-badge&logo=java&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC%20%7C%20Facade%20%7C%20Observer-4E73DF?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Completed%20%26%20Tested-brightgreen?style=for-the-badge)

<p align="center">
  <b>Una recreación digital fiel, interactiva y arquitectónicamente robusta del tradicional juego de mesa Parchís.</b>
  <br />
  Diseñado para 2 a 4 jugadores con animaciones fluidas celda a celda, renderizado vectorial dinámico y un motor de reglas exhaustivo.
</p>

</div>

---

## 📖 Tabla de Contenidos

1. [Descripción General](#-descripción-general)
2. [Características Principales](#-características-principales)
3. [Arquitectura y Patrones de Diseño](#-arquitectura-y-patrones-de-diseño)
4. [Estructura del Proyecto](#-estructura-del-proyecto)
5. [Reglamento Oficial Implementado](#-reglamento-oficial-implementado)
6. [Requisitos e Instalación](#-requisitos-e-instalación)
7. [Cómo Jugar](#-cómo-jugar)
8. [Equipo de Desarrollo](#-equipo-de-desarrollo)

---

## 🌟 Descripción General

Este proyecto implementa el tradicional juego de mesa **Parchís** (derivado del clásico Pachisi de la India). Cada jugador comanda un ejército de **4 fichas** con el objetivo estratégico de sacarlas de su base, recorrer un circuito cerrado de 52 casillas en el tablero, sortear bloqueos rivales, capturar piezas enemigas y ascender por su pasillo de color hasta alcanzar la meta central.

El sistema fue desarrollado con un fuerte énfasis en la **separación de responsabilidades**, **desacoplamiento modular** y **diseño estructural detallado**, empleando el patrón **MVC**, el patrón **Fachada** para aislar la lógica de negocio y el patrón **Observador** para sincronizar la interfaz en tiempo real.

---

## ✨ Características Principales

### 🎲 Motor de Reglas Exhaustivo
* **Salida de Base Inteligente:** Requiere un 5 en el dado para posicionar una ficha en la casilla de salida correspondiente.
* **Control de Capacidad en Casillas:** Control estricto de un máximo de 2 fichas por casilla en todo el circuito.
* **Sistema de Barreras (Bloqueos):** Dos fichas en una misma casilla (segura o regular) generan un puente que impide tanto aterrizar en ella como saltar por encima a cualquier oponente.
* **Mecánica de Capturas ("Comer Ficha"):** Aterrizar sobre una casilla regular ocupada por un rival envía su ficha de vuelta a su base.
* **Zonas Seguras Inmunes:** 8 casillas de seguro donde conviven fichas rivales sin riesgo de captura.
* **Bonificaciones y Seises:** Sacar un 6 permite repetir turno de inmediato, con penalización y cambio de turno al encadenar 3 seises seguidos.
* **Carril de Llegada y Meta:** Requiere tirada exacta para coronar en la casilla 56, bloqueando saltos entre fichas propias en el pasillo de meta.

### 🎨 Experiencia Visual y Gráfica (Swing / Graphics2D)
* **Tablero Vectorial Dinámico:** Renderizado nítido con antialiasing (`RenderingHints.KEY_ANTIALIASING`) e interpolación bilineal adaptada al tamaño del panel.
* **Animación Parabólica en Tiempo Real:** Las fichas no se teletransportan; avanzan casilla por casilla con una animación de salto de 60 FPS mediante `javax.swing.Timer`.
* **Dado Interactivo con Animación:** Simulación de tirada con frames aleatorios dinámicos antes de revelar el valor resultante.
* **Indicadores Visuales y Feedback:** Fichas elegibles iluminadas, puntero de ratón interactivo (`HAND_CURSOR`), turnos destacados con colores distintivos y mensajes de estado contextuales.

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
│  ┌───────────────────────────────────────────────────────┐  │
│  │                    IFachadaReglas                     │  │
│  │  (Fachada que expone lanzarDado(), moverFicha(), etc) │  │
│  └───────────────────────────▲───────────────────────────┘  │
│                              │ implementa                   │
│  ┌───────────────────────────┴───────────────────────────┐  │
│  │                   FachadaReglasMock                   │  │
│  │   (Motor de reglas, barreras, capturas y victoria)    │  │
│  └───────────────────────────────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────┘
                               │ intercambia
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                        CAPA DE DTOs                         │
│  ┌────────────────────┐ ┌──────────────┐ ┌───────────────┐  │
│  │  EstadoPartidaDTO  │ │   FichaDTO   │ │  JugadorDTO   │  │
│  └────────────────────┘ └──────────────┘ └───────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Patrones Aplicados:
1. **MVC (Modelo - Vista - Controlador):** `VistaPartida` delega las acciones de los botones y clics a `ControladorPartida`, quien actualiza el `ModeloPartida`.
2. **Fachada (Facade Pattern):** La interfaz `IFachadaReglas` oculta la complejidad del cálculo matemático de 52 casillas, rutas, barreras intermedias y turnos.
3. **Observador (Observer Pattern):** La interfaz `IObservadorPartida` permite a los componentes visuales reaccionar automáticamente cada vez que el estado de la partida cambia.
4. **Data Transfer Object (DTO):** Objetos planos serializables y desacoplados (`EstadoPartidaDTO`, `FichaDTO`, `JugadorDTO`) que aíslan el modelo de datos de la interfaz visual.

---

## 📁 Estructura del Proyecto

```bash
Parchis/
├── pom.xml                               # Configuración de dependencias y compilador Maven
├── nbactions.xml                         # Acciones de integración de Apache NetBeans
├── README.md                             # Documentación oficial del repositorio
└── src/
    └── main/
        ├── java/
        │   ├── app/
        │   │   └── Main.java             # Clase de arranque (inicializa MVC y abre ventana)
        │   ├── dto/
        │   │   ├── EstadoPartidaDTO.java # Snapshot completo del estado actual de la partida
        │   │   ├── FichaDTO.java         # Datos de posición, estado de base, meta y elegibilidad
        │   │   └── JugadorDTO.java       # Información de jugador, color asignado y marcador
        │   ├── negocios/
        │   │   ├── IFachadaReglas.java   # Contrato de la fachada de reglas del juego
        │   │   └── FachadaReglasMock.java# Lógica del motor: turnos, dados, barreras y colisiones
        │   └── presentacion/
        │       ├── ControlDado.java      # Componente visual interactivo del dado con animación
        │       ├── ControladorPartida.java# Controlador que procesa eventos de usuario
        │       ├── IObservadorPartida.java# Interfaz del patrón Observador
        │       ├── ModeloPartida.java    # Modelo observable de presentación
        │       ├── PanelJugador.java     # Tarjeta visual con avatar, color y fichas en meta
        │       ├── PanelTablero.java     # Lienzo de Graphics2D con dibujo y animación de fichas
        │       ├── Recursos.java         # Cargador y caché de imágenes en memoria
        │       ├── VistaPartida.java     # Ventana principal del juego (JFrame Swing)
        │       └── VistaPartida.form     # Diseño visual de NetBeans GUI Builder
        └── resources/
            └── img/                      # Assets gráficos oficiales (tablero, dados, fichas, avatares)
```

---

## 📜 Reglamento Oficial Implementado

| Regla | Comportamiento en el Motor |
|---|---|
| **Salida de Ficha** | Se requiere obtener un **5** para liberar una ficha de la base a la casilla de salida de su color. |
| **Capacidad Máxima** | Una casilla solo puede contener un máximo de **2 fichas** simultáneamente. |
| **Barreras / Puentes** | Si una casilla tiene 2 fichas, **ninguna ficha (rival o propia) puede saltarla ni caer en ella**. |
| **Apertura de Barrera** | El dueño de las fichas en una barrera puede mover una ficha hacia adelante, liberando el paso. |
| **Repetir Turno (6)** | Sacar un 6 otorga un turno consecutivo adicional tras mover ficha. |
| **Penalización de Seises** | Sacar **tres seises consecutivos** cancela el turno inmediatamente y cede el turno al rival. |
| **Casillas Seguras** | Casillas especiales (`0, 8, 13, 21, 26, 34, 39, 47`) identificadas con círculo gris donde las fichas no pueden ser comidas. |
| **Captura de Fichas** | Caer en una casilla regular con una ficha contraria la captura y la envía de vuelta a su base (`posicion = -1`). |
| **Pasillo de Meta** | Del paso 51 al 55 las fichas entran a su rampa privada; no se permite saltar fichas del mismo jugador en este tramo. |
| **Llegada a Meta** | La meta se ubica en la posición **56**. Requiere un número exacto para coronar la ficha. |
| **Condición de Victoria** | El primer jugador en colocar sus **4 fichas en la meta** gana automáticamente la partida. |

---

## 🚀 Requisitos e Instalación

### Requisitos Previos
* **Java Development Kit (JDK):** Versión 21 o superior.
* **Apache Maven:** 3.8.0 o superior (o usar el Maven integrado en NetBeans).
* **IDE Recomendado:** Apache NetBeans 19+, IntelliJ IDEA o VS Code con extensión Java.

### Clonar el Repositorio
```bash
git clone https://github.com/wareon711/Parchis.git
cd Parchis
```

### Compilar y Ejecutar desde Terminal
```bash
# Compilar el proyecto con Maven
mvn clean compile

# Ejecutar el juego
mvn exec:java
```

### Ejecutar desde Apache NetBeans
1. Abre NetBeans y selecciona **File > Open Project...**
2. Selecciona la carpeta `Parchis` (reconocida automáticamente por el ícono de Maven `pom.xml`).
3. Presiona **`F6`** (o clic derecho sobre el proyecto > **Run**).

---

## 🎮 Cómo Jugar

1. **Lanzar el Dado:** En tu turno, haz clic sobre el botón o el componente visual del dado para tirar.
2. **Elegir Ficha:** Las fichas que tengan un movimiento legal permitido según las reglas del juego parpadearán o estarán disponibles para selección.
3. **Mover:** Haz clic sobre la ficha deseada en el tablero para desplazarla con animación fluida hasta su destino.
4. **Pase de Turno Automático:** Si tras lanzar el dado no existe ningún movimiento reglamentario posible, el sistema informará en pantalla y pasará el turno de forma automática.
5. **Ganar:** ¡El primer jugador en introducir sus cuatro piezas al centro del tablero se corona campeón!

---

## 👥 Equipo de Desarrollo

Proyecto desarrollado de manera colaborativa utilizando Git con ramas de características independientes (`feature branches`) y flujo de Pull Requests:

| Integrante | ID / Matrícula | Usuario GitHub | Rol en el Proyecto | Rama Asignada |
|---|:---:|:---:|---|:---:|
| **Gibran Alonso Ibarra Palomares** | `267883` | [@wareon711](https://github.com/wareon711) | Interfaz gráfica Swing (`VistaPartida`), panel del tablero (`PanelTablero`), dados interactivos, recursos y documentación. | `feature/interfaz-y-recursos` |
| **Héctor Eduardo Rojo Cota** | `271229` | [@Eduardx77](https://github.com/Eduardx77) | Estructura base, arranque `Main`, modelos DTOs, interfaz del observador y configuración Maven. | `feature/app-y-dto` |
| **Mía Anangel Nieblas Hollman** | `267653` | [@angeltqm](https://github.com/angeltqm) | Motor de reglas de negocio, validación de jugadas (`FachadaReglasMock`) y controlador MVC. | `feature/negocios-y-control` |

---

<div align="center">
  <sub>Desarrollado con dedicación para el curso de <b>Arquitectura y Diseño Estructural de Software</b>.</sub>
</div>