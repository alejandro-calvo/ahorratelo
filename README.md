# Ahórratelo 💰

Ahórratelo es una aplicación Android desarrollada en **Kotlin** para registrar, organizar y visualizar gastos personales.

La aplicación permite establecer un presupuesto mensual, registrar gastos físicos y virtuales, clasificarlos por categorías, adjuntar imágenes de tickets, asociar ubicación a los gastos físicos y visualizar dichos gastos sobre **Google Maps**.

Los datos se almacenan localmente utilizando **Room**, siguiendo una arquitectura basada en `DAO`, `Repository` y `ViewModel`.

---

## Funcionalidades principales

### Gestión de gastos

La aplicación permite:

- Crear nuevos gastos.
- Editar gastos existentes.
- Eliminar gastos.
- Consultar todos los gastos registrados.
- Clasificar cada gasto como:
  - **Físico**
  - **Virtual**
- Asignar diferentes categorías.
- Añadir comentarios.
- Guardar la fecha del gasto.
- Asociar una imagen del ticket.
- Asociar coordenadas geográficas a los gastos físicos.

Cada gasto se almacena localmente en la base de datos de la aplicación.

---

## Presupuesto mensual

Desde la pantalla principal se puede establecer un presupuesto mensual.

La aplicación calcula automáticamente:

- Presupuesto establecido.
- Dinero total gastado.
- Dinero restante.

Además, muestra diferentes mensajes dependiendo de la situación:

- Si todavía no existen gastos.
- Si el gasto se mantiene dentro del presupuesto.
- Si se ha superado el presupuesto mensual.

El presupuesto se almacena mediante `SharedPreferences`, ya que se trata de un único valor sencillo que debe mantenerse entre ejecuciones de la aplicación.

---

## Gastos físicos y virtuales

Cada gasto puede clasificarse como:

### Físico

Representa compras realizadas en una ubicación concreta, como por ejemplo:

- Restaurantes.
- Supermercados.
- Transporte.
- Ocio.

Estos gastos pueden almacenar:

- Latitud.
- Longitud.
- Imagen del ticket.

Los gastos físicos que disponen de coordenadas pueden visualizarse posteriormente sobre Google Maps.

### Virtual

Representa pagos realizados digitalmente, como servicios o compras online.

Estos gastos se almacenan igual que los físicos, pero no necesitan información de geolocalización.

---

## Categorías

Los gastos pueden clasificarse en las siguientes categorías:

- Restaurante
- Supermercado
- Transporte
- Ocio
- Servicio
- Otros

Estas categorías se utilizan tanto para organizar los gastos como para representarlos posteriormente en el mapa y en el resumen.

---

# Mapa de gastos

La aplicación integra **Google Maps SDK for Android**.

Los gastos físicos que tienen almacenadas coordenadas aparecen representados mediante marcadores sobre el mapa.

Cada categoría utiliza un color diferente:

| Categoría | Color del marcador |
|---|---|
| Restaurante | Rojo |
| Supermercado | Verde |
| Transporte | Azul |
| Ocio | Violeta |
| Servicio | Naranja |
| Otros | Rosa |

Cada marcador muestra:

- Nombre del gasto.
- Categoría.
- Importe.

La aplicación también puede mostrar la ubicación actual del usuario mediante la capa de localización de Google Maps.

Al abrir el mapa:

1. Se comprueban los permisos de ubicación.
2. Si están concedidos, se obtiene la última ubicación conocida.
3. La cámara se desplaza hacia la posición del usuario.
4. Se cargan los gastos físicos almacenados.
5. Se representa cada gasto como un marcador.

Si no puede obtenerse la ubicación del usuario, la cámara puede desplazarse al primer gasto físico disponible.

---

# Geolocalización

La aplicación utiliza `FusedLocationProviderClient` para obtener la ubicación del dispositivo.

Al crear un gasto físico:

1. Se comprueba si la aplicación dispone de permisos de ubicación.
2. Si no los tiene, se solicitan al usuario.
3. Se obtiene la última ubicación conocida.
4. La latitud y longitud se introducen en el formulario del gasto.
5. Las coordenadas se almacenan junto al resto de información.

Se utilizan los permisos:

```text
ACCESS_FINE_LOCATION
ACCESS_COARSE_LOCATION
```

La localización se utiliza únicamente para asociar una posición a los gastos físicos y representarlos posteriormente en el mapa.

---

# Imágenes de tickets

Ahórratelo permite asociar una imagen a un gasto.

La selección de imágenes se realiza mediante `Photo Picker`.

Cuando el usuario selecciona una imagen:

1. La aplicación recibe su `URI`.
2. La imagen se copia al almacenamiento interno privado de la aplicación.
3. Se genera un archivo propio dentro de la carpeta de tickets.
4. Se almacena la ruta interna junto al gasto.

De esta forma, la aplicación no depende de permisos temporales sobre la imagen original y puede seguir accediendo al ticket posteriormente.

Las imágenes se almacenan dentro de una carpeta interna similar a:

```text
files/tickets/
```

Los nombres se generan automáticamente utilizando el tiempo actual:

```text
ticket_XXXXXXXXXXXX.jpg
```

---

# Resumen de gastos

La aplicación dispone de una pantalla de resumen que analiza todos los gastos almacenados.

Se muestran:

- Gasto total.
- Total de gastos físicos.
- Total de gastos virtuales.
- Gasto acumulado por categoría.

Por ejemplo:

```text
Total gastado: 450 €
Gastos físicos: 320 €
Gastos virtuales: 130 €

Restaurante: 120 €
Supermercado: 150 €
Transporte: 50 €
Servicio: 130 €
```

Los valores se recalculan automáticamente cuando cambia la información almacenada.

---

# Persistencia de datos

Los gastos se almacenan localmente utilizando **Room**, la capa de persistencia de Android basada en SQLite.

La entidad principal es:

```text
Expense
```

Cada gasto contiene:

- `expenseId`
- `name`
- `amount`
- `category`
- `type`
- `comment`
- `date`
- `latitude`
- `longitude`
- `ticketImageUri`

---

## DAO

El acceso a la base de datos se realiza mediante `ExpenseDao`.

Las principales operaciones disponibles son:

### Obtener todos los gastos

```kotlin
@Query("SELECT * FROM expense_table")
fun getAll(): LiveData<List<Expense>>
```

### Obtener un gasto por ID

```kotlin
@Query("SELECT * FROM expense_table WHERE expenseId = :id")
fun getById(id: Int): LiveData<Expense>
```

### Obtener gastos físicos con ubicación

```kotlin
@Query(
    "SELECT * FROM expense_table " +
    "WHERE type = 'Físico' " +
    "AND latitude IS NOT NULL " +
    "AND longitude IS NOT NULL"
)
fun getPhysicalExpenses(): LiveData<List<Expense>>
```

También se implementan operaciones para:

- Insertar gastos.
- Actualizar gastos.
- Eliminar gastos.

Las operaciones de escritura se ejecutan como funciones `suspend` para evitar bloquear la interfaz de usuario.

---

# Arquitectura

El proyecto separa la interfaz, la lógica y el acceso a datos utilizando varios componentes de Android Architecture Components.

La estructura principal es:

```text
Activity
   ↓
ViewModel
   ↓
Repository
   ↓
DAO
   ↓
Room / SQLite
```

## ViewModel

`ExpenseViewModel` sirve como intermediario entre las Activities y la capa de datos.

Las Activities no acceden directamente a Room.

El ViewModel expone mediante `LiveData`:

- Todos los gastos.
- Los gastos físicos.
- Gastos individuales.

Las operaciones de escritura utilizan `viewModelScope` y corrutinas.

---

## Repository

`ExpenseRepository` crea una capa intermedia entre el ViewModel y el DAO.

Centraliza operaciones como:

- Obtener todos los gastos.
- Obtener gastos físicos.
- Buscar un gasto por ID.
- Insertar.
- Actualizar.
- Eliminar.

---

## LiveData

Las diferentes pantallas observan objetos `LiveData`.

Cuando la información almacenada cambia, la interfaz se actualiza automáticamente.

Esto se utiliza, por ejemplo, en:

- Lista de gastos.
- Resumen.
- Mapa.
- Pantalla principal.

---

## Corrutinas

Las operaciones de escritura sobre Room se realizan utilizando **Kotlin Coroutines**.

Esto permite ejecutar operaciones sobre la base de datos fuera del hilo principal y evitar bloquear la interfaz.

---

# Pantallas principales

## Pantalla principal

Muestra:

- Presupuesto mensual.
- Total gastado.
- Dinero restante.
- Estado del presupuesto.

También incluye acceso rápido para añadir un nuevo gasto.

La navegación lateral permite acceder a:

- Lista de gastos.
- Resumen.
- Mapa.

---

## Lista de gastos

Los gastos se muestran mediante un `RecyclerView`.

Cada elemento representa un gasto almacenado.

Al seleccionar uno se abre la pantalla de edición correspondiente.

También existe un botón flotante para crear nuevos gastos.

---

## Añadir / editar gasto

La misma Activity se utiliza para crear y modificar gastos.

Si recibe un `expenseId`, se utiliza el modo edición.

En caso contrario, se utiliza para crear un nuevo gasto.

Desde esta pantalla se pueden configurar:

- Nombre.
- Importe.
- Tipo.
- Categoría.
- Comentario.
- Fecha.
- Coordenadas.
- Imagen del ticket.

También es posible eliminar un gasto existente.

---

## Resumen

Presenta estadísticas básicas de los gastos registrados:

- Gasto total.
- Gastos físicos.
- Gastos virtuales.
- Acumulado por categoría.

---

## Mapa

Representa sobre Google Maps todos los gastos físicos que tengan coordenadas almacenadas.

Los marcadores utilizan distintos colores dependiendo de la categoría.

---

# Tecnologías utilizadas

### Lenguaje

- Kotlin

### Android

- Android SDK
- AndroidX
- Material Components
- RecyclerView
- Data Binding
- SharedPreferences
- Photo Picker

### Arquitectura y datos

- Room
- SQLite
- DAO
- Repository Pattern
- ViewModel
- LiveData
- Kotlin Coroutines

### Mapas y localización

- Google Maps SDK for Android
- Google Play Services Location
- FusedLocationProviderClient

### Build

- Gradle
- Kotlin DSL

---

# Requisitos

El proyecto utiliza:

```text
minSdk: 24
targetSdk: 36
compileSdk: 36
Java: 11
```

Para ejecutarlo se recomienda utilizar una versión reciente de **Android Studio**.

---

# Instalación

## 1. Clonar el repositorio

```bash
git clone https://github.com/alejandro-calvo/ahorratelo.git
```

Abre la carpeta del proyecto desde Android Studio.

---

## 2. Sincronizar Gradle

Android Studio debería detectar automáticamente los archivos de Gradle.

Ejecuta:

```text
Sync Project with Gradle Files
```

y espera a que se descarguen las dependencias necesarias.

---

# Configuración de Google Maps

Para utilizar la pantalla de mapa es necesario disponer de una API Key válida de Google Maps.

La clave **no se incluye en el repositorio** por motivos de seguridad.

## 1. Crear una API Key

Desde Google Cloud Console:

1. Crea o selecciona un proyecto.
2. Habilita **Maps SDK for Android**.
3. Crea una API Key.

Se recomienda restringir la clave para que únicamente pueda utilizarse desde la aplicación Android correspondiente.

---

## 2. Configurar `local.properties`

En la raíz del proyecto existe o debe crearse un archivo:

```text
local.properties
```

Añade dentro del archivo:

```properties
GOOGLE_MAPS_API_KEY=TU_API_KEY
```

Por ejemplo:

```properties
GOOGLE_MAPS_API_KEY=AIzaXXXXXXXXXXXXXXX
```

No sustituyas el resto del contenido que Android Studio pueda haber generado dentro de `local.properties`, como la ubicación del Android SDK.

Simplemente añade la variable:

```properties
GOOGLE_MAPS_API_KEY=TU_API_KEY
```

El proyecto utiliza `Secrets Gradle Plugin` para obtener esta variable y proporcionársela al `AndroidManifest.xml`.

En el Manifest se utiliza:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${GOOGLE_MAPS_API_KEY}" />
```

---

## 3. Seguridad de la clave

`local.properties` está excluido mediante `.gitignore`, por lo que la API Key personal no debe subirse al repositorio.

Cada persona que clone Ahórratelo debe utilizar **su propia API Key de Google Maps**.

---

# Ejecución

Una vez configurado el proyecto:

1. Abre Ahórratelo en Android Studio.
2. Espera a que termine la sincronización de Gradle.
3. Configura la API Key de Google Maps.
4. Selecciona un emulador Android o conecta un dispositivo físico.
5. Ejecuta la aplicación mediante **Run**.

Al utilizar las funciones de localización, Android solicitará permiso para acceder a la ubicación del dispositivo.

---

# Estructura del proyecto

La parte principal del código se encuentra en:

```text
app/src/main/java/com/example/ahorratelo/
```

Los principales archivos son:

```text
MainActivity.kt
AddEditExpenseActivity.kt
ExpenseListActivity.kt
MapActivity.kt
SummaryActivity.kt

Expense.kt
ExpenseDao.kt
AppDatabase.kt
ExpenseRepository.kt
ExpenseViewModel.kt
ExpenseViewModelFactory.kt

ExpenseAdapter.kt
```

Los recursos visuales se encuentran en:

```text
app/src/main/res/
```

incluyendo:

```text
layout/
drawable/
menu/
mipmap/
values/
xml/
```

---

# Flujo general

El funcionamiento general de la aplicación puede resumirse así:

```text
Usuario
   ↓
Activity
   ↓
ViewModel
   ↓
Repository
   ↓
DAO
   ↓
Room / SQLite
```

Para gastos físicos:

```text
Gasto físico
   ↓
Coordenadas
   ↓
Room
   ↓
LiveData
   ↓
MapActivity
   ↓
Google Maps
   ↓
Marcador
```

Para imágenes de tickets:

```text
Photo Picker
   ↓
URI de imagen
   ↓
Copia al almacenamiento interno
   ↓
Ruta guardada en Expense
   ↓
Room
```

---

# Privacidad y funcionamiento offline

Los gastos se almacenan localmente en el dispositivo mediante Room.

La aplicación no requiere un backend propio ni una cuenta de usuario externa para gestionar los gastos.

La conexión a Internet es necesaria para las funcionalidades relacionadas con Google Maps.

---

# Contexto del proyecto

Ahórratelo fue desarrollado como proyecto académico dentro del **Grado en Ingeniería Telemática de la Universidad Rey Juan Carlos**.

El objetivo del proyecto fue desarrollar una aplicación Android completa utilizando Kotlin y diferentes componentes del ecosistema Android, incluyendo:

- Persistencia local.
- Arquitectura basada en ViewModel y Repository.
- Interfaces reactivas mediante LiveData.
- Corrutinas.
- Gestión de permisos.
- Geolocalización.
- Integración con Google Maps.
- Almacenamiento de imágenes.
- Navegación entre diferentes Activities.
- Gestión y análisis de información de gastos.
