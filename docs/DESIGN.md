# SplitIt — Sistema de Diseño

> **Concepto:** "Cuentas claras" — rediseño visual completo de las 7 pantallas de SplitIt con sistema de diseño propio (color, tipografía, formas, iconografía, motion).
> **Alcance:** define el lenguaje visual y la especificación por pantalla. La lógica de dominio (`BalanceCalculator`, optimizadores, repositorios, SQLDelight) queda fuera; los `ViewModel`/`UiState` existentes se reutilizan, solo se rediseña la capa de presentación y navegación.

---

## 1. Concepto de diseño: **"Cuentas claras"**

SplitIt no es un banco: es la app que usas *con* tus amigos, en la mesa de un restaurante, en un viaje, en un piso compartido. El diseño debe comunicar calidez y cercanía, no frialdad ni plantilla genérica.

El concepto **"Cuentas claras"** se apoya en cuatro ideas con sentido semántico directo con el dominio (gestionar gastos entre varios participantes):

| Pilar | Significado | Cómo se materializa |
|---|---|---|
| **Cálido, no bancario** | Hablar de dinero entre amigos no debería sentirse como una app de finanzas | Fondos crema/terrosos (nunca grises fríos), coral como color de marca, formas muy redondeadas |
| **El grupo es el protagonista** | Los gastos no son de "la app", son de *personas* | Avatares con iniciales como motivo recurrente: *avatar stack* en cada grupo, pagador visible en cada gasto, flujos persona→persona en liquidación |
| **El dinero se ve, no solo se lee** | Un balance debe entenderse en 1 segundo | Importes con tipografía hero y cifras tabulares, barras de balance divergentes como firma visual de la app, color semántico (coral sale / teal entra) |
| **Saldar se celebra** | Quedar en paz es el momento feliz del ciclo | Estado "todos al día" como pantalla de celebración, teal como color de equilibrio, copy cercano |

**Par de acentos cromáticos y su semántica de dominio:**

- **Coral** (`primary`) → acción, creación, dinero que *sale* (deudas). Es cálido, social, apetecible; evita el rojo-error agresivo y el azul-banca.
- **Teal** (`secondary`) → equilibrio, dinero que *entra* (créditos), estado saldado. Coral y teal son complementarios: el flujo de dinero entre participantes se narra solo con color.
- **Ámbar** (`tertiary`) → atención pendiente: liquidación desactualizada (*stale*), avisos no bloqueantes.

> **Regla de oro del sistema:** el color nunca es el único canal. Deuda/crédito siempre acompañan el color con icono (`arrow_upward`/`arrow_downward`) y signo (+/−).

---

## 2. Principios de diseño

1. **Una acción primaria por pantalla.** Cada pantalla tiene un único CTA dominante (FAB o botón primario) y el resto vive en menús overflow o acciones secundarias.
2. **Las personas primero.** Todo participante se representa siempre igual: burbuja circular con inicial + color estable. El usuario aprende "quién es quién" por color+inicial a lo largo de toda la app.
3. **Jerarquía del dinero.** El importe es el dato más importante de cualquier fila de gasto/balance/transfer: tipografía grande, peso semibold, cifras tabulares (`tnum`) para que las columnas de números no "bailen".
4. **Estados de verdad.** Cada pantalla define explícitamente sus estados: carga (skeleton, no spinner genérico), vacío (ilustrado, con CTA), error (con reintento) y contenido.
5. **Multiplataforma nativo.** Todo el sistema vive en `commonMain` con Compose Multiplatform: recursos en `composeResources`, sin dependencias Android-only, tipografía del sistema (Roboto/SF Pro) para sentirse nativo en cada plataforma.

---

## 3. Sistema de color

### 3.1 Paleta de marca — Light

Fondo cálido rosado-crema; nada de grises neutros fríos. Es el sello distintivo inmediato.

| Rol M3 | Hex | Uso |
|---|---|---|
| `primary` | `#C43C2E` | Botones primarios, FAB, elementos activos, deuda |
| `onPrimary` | `#FFFFFF` | Texto/iconos sobre primary (contraste ≥ 4.5:1) |
| `primaryContainer` | `#FFDAD3` | Fondos suaves de énfasis coral (chips activos, surfaceDebt) |
| `onPrimaryContainer` | `#410100` | Texto sobre primaryContainer |
| `secondary` | `#00696B` | Teal: crédito, acentos de equilibrio, iconos de "saldado" |
| `onSecondary` | `#FFFFFF` | |
| `secondaryContainer` | `#9CF1F0` | Fondos suaves teal (surfaceCredit, chips de "al día") |
| `onSecondaryContainer` | `#002020` | |
| `tertiary` | `#7C5700` | Ámbar oscuro: texto de avisos |
| `tertiaryContainer` | `#FFDEA6` | Banner de liquidación *stale*, avisos |
| `onTertiaryContainer` | `#271900` | |
| `error` | `#BA1A1A` | Solo errores reales (validación, fallos) — **no** deudas |
| `errorContainer` | `#FFDAD6` | |
| `background` | `#FFF8F6` | Fondo base cálido |
| `onBackground` | `#221917` | |
| `surface` | `#FFF8F6` | Superficies base |
| `onSurface` | `#221917` | |
| `surfaceVariant` | `#F4DDD7` | Cards de baja jerarquía, contenedores de formulario |
| `onSurfaceVariant` | `#524342` | Texto secundario |
| `outline` | `#857371` | Bordes, divisores suaves |
| `inverseSurface` / `inverseOnSurface` / `inversePrimary` | `#382E2C` / `#FFEDE9` / `#FFB4A3` | Snackbars y superficies invertidas |

### 3.2 Paleta de marca — Dark

Negro cálido terroso (`#1A1110`), coherente con el blanco crema del modo claro. El coral pasa a tono pastel luminoso para mantener contraste.

| Rol M3 | Hex |
|---|---|
| `primary` | `#FFB4A3` |
| `onPrimary` | `#5F1609` |
| `primaryContainer` | `#992515` |
| `onPrimaryContainer` | `#FFDAD3` |
| `secondary` | `#4CDADB` |
| `onSecondary` | `#003738` |
| `secondaryContainer` | `#004F51` |
| `onSecondaryContainer` | `#9CF1F0` |
| `tertiary` | `#F2BE4D` |
| `onTertiary` | `#3F2E00` |
| `tertiaryContainer` | `#5B4300` |
| `onTertiaryContainer` | `#FFDEA6` |
| `error` | `#FFB4AB` |
| `errorContainer` | `#93000A` |
| `background` | `#1A1110` |
| `onBackground` | `#F1DFDB` |
| `surface` | `#1A1110` |
| `onSurface` | `#F1DFDB` |
| `surfaceVariant` | `#534341` |
| `onSurfaceVariant` | `#D8C2BD` |
| `outline` | `#A08C88` |
| `inverseSurface` / `inverseOnSurface` / `inversePrimary` | `#F1DFDB` / `#382E2C` / `#C43C2E` |

### 3.3 Colores semánticos de dominio

Se exponen vía `CompositionLocal` propio (`SplitItSemanticColors`) para no sobrecargar el `ColorScheme` de M3:

| Token | Light | Dark | Semántica |
|---|---|---|---|
| `credit` | `#00696B` | `#4CDADB` | Dinero que el participante **recibe** (reutiliza teal) |
| `debt` | `#C43C2E` | `#FFB4A3` | Dinero que el participante **debe** (reutiliza coral) |
| `settled` | `#524342` | `#D8C2BD` | Balance en cero ("al día") — neutro, sin alarma |
| `staleWarning` | `#7C5700` | `#F2BE4D` | Liquidación desactualizada |
| `surfaceCredit` | `#9CF1F0`→(alfa 40%) | `#004F51` | Fondo suave de fila/tarjeta de crédito |
| `surfaceDebt` | `#FFDAD3` | `#992515`→(alfa 60%) | Fondo suave de fila/tarjeta de deuda |

### 3.4 Paleta de avatares (participantes)

**8 colores** armonizados con la marca (coral+teal presentes, sin duplicar el teal de sistema). Cada color tiene variante dark más luminosa. Las iniciales se dibujan en blanco (light) u oscuro (dark) según contraste.

| # | Nombre | Light | Dark |
|---|---|---|---|
| 1 | Coral | `#E0533D` | `#FF9E8C` |
| 2 | Teal | `#0F7B7E` | `#6FD8DB` |
| 3 | Índigo | `#5B5FC7` | `#A9ADFF` |
| 4 | Ámbar | `#B26A00` | `#FFC66E` |
| 5 | Frambuesa | `#B8375E` | `#FF8FAD` |
| 6 | Verde | `#3E7C3A` | `#8FD18A` |
| 7 | Océano | `#2E6DAE` | `#8FBDFF` |
| 8 | Violeta | `#8A4FB8` | `#D3A4FF` |

> El mapeo `participantColor()` mantiene compatibilidad con los hex ya persistidos en base de datos: resuelve los hex legacy sin error y esos colores simplemente dejan de ofrecerse en el selector.

### 3.5 Accesibilidad cromática

- Todos los pares texto/fondo objetivo **WCAG AA** (≥ 4.5:1 texto normal, ≥ 3:1 texto grande ≥ 18sp bold y componentes gráficos). Verificación con previews de contraste.
- Deuda/crédito/settled **nunca solo por color**: siempre con icono y/o signo.
- Touch targets ≥ 48 dp en toda superficie interactiva.

---

## 4. Tipografía

**Estrategia: fuentes del sistema** (`FontFamily.Default` → Roboto en Android, SF Pro en iOS). Cero peso binario, look nativo, y la personalidad se consigue con escala, pesos y *números tabulares*, no con una fuente custom.

### 4.1 Escala (basada en M3, ajustada)

| Estilo | Tamaño / línea | Peso | Uso |
|---|---|---|---|
| `displaySmall` | 36 / 44 | SemiBold | **Importe hero**: monto en formulario de gasto, total del grupo |
| `headlineMedium` | 28 / 36 | SemiBold | Título de pantalla en app bar grande |
| `titleLarge` | 22 / 28 | SemiBold | Títulos de sección hero (detalle de grupo) |
| `titleMedium` | 16 / 24 | SemiBold | Título de filas (gasto, grupo, participante) |
| `bodyLarge` | 16 / 24 | Regular | Texto principal, formularios |
| `bodyMedium` | 14 / 20 | Regular | Texto secundario, metadatos ("pagado por Ana · entre 4") |
| `bodySmall` | 12 / 16 | Regular | Timestamps, hints |
| `labelLarge` | 14 / 20 | SemiBold | Botones, chips |

### 4.2 Estilos semánticos de dinero

El dinero es un ciudadano de primera clase. Se define un `CompositionLocal` (`SplitItMoneyStyles`) con:

- `moneyHero` → `displaySmall` + `fontFeatureSettings = "tnum"` + SemiBold.
- `moneyRow` → `titleMedium` + `tnum` (importes en listas; alineación vertical perfecta entre filas).
- `moneyCaption` → `bodyMedium` + `tnum` (importes secundarios en cards).

> **Por qué `tnum`:** las cifras tabulares tienen ancho fijo; en listas de gastos y en la tabla de balances los importes forman columna legible sin "temblor". Es un detalle invisible que separa una app cuidada de una plantilla.

---

## 5. Formas, espaciado y elevación

### 5.1 Shapes ("social y expresivo" = redondez generosa)

| Token M3 | Valor | Uso |
|---|---|---|
| `extraSmall` | 8 dp | Chips pequeños, badges |
| `small` | 12 dp | TextFields, menús |
| `medium` | 20 dp | **Todas las cards** |
| `large` | 28 dp | Bottom sheets (esquinas superiores), diálogos |
| Botones / FAB | Pill completo (`CircleShape` M3 default) | CTAs |
| Avatares | Círculo | Participantes |

### 5.2 Espaciado (grid de 4 dp, tokens nombrados)

`SplitItSpacing` vía CompositionLocal: `xxs=2`, `xs=4`, `sm=8`, `md=12`, `lg=16`, `xl=20`, `xxl=24`, `xxxl=32`, `huge=48`.

Reglas de aplicación:

- Padding de pantalla: `lg` (16) lateral, `xl` (20) superior.
- Separación entre cards en lista: `md` (12).
- Padding interno de card: `lg` (16).
- Separación entre secciones de pantalla: `xxxl` (32).
- **Sin paddings ad-hoc** (valores mezclados sin sistema).

### 5.3 Elevación

Sistema plano cálido: las cards usan `surface` con borde `outlineVariant` sutil o `surfaceVariant` plana; elevación tonal M3 solo para FAB (nivel 3), bottom sheets y diálogos. Nada de sombras duras.

---

## 6. Iconografía

El sistema adopta **Material Symbols Rounded** (coherente con las formas redondeadas), exportados como **vector drawables XML propios en `composeResources/drawable/`**.

> **Decisión técnica:** set propio de vectores antes que la dependencia `material-icons-extended`: control total del estilo rounded, peso mínimo, cero incertidumbre de compatibilidad de versiones en Compose Multiplatform.

**Set de iconos:**

| Icono | Uso |
|---|---|
| `arrow_back` | Navegación atrás (TopAppBar) |
| `add` | FAB nuevo gasto/grupo/participante |
| `close` | Cerrar formulario/sheet |
| `check` | Confirmar, estado "al día" |
| `search` / `search_off` | Campo de búsqueda / sin resultados |
| `edit` / `delete` | Acciones de fila (en menú overflow) |
| `more_vert` | Menú overflow en cards |
| `group` | Grupos/participantes |
| `person_add` | Añadir participante |
| `receipt_long` | Gastos |
| `payments` | Importe/moneda |
| `swap_horiz` | Transfers (A → B) |
| `account_balance_wallet` | Liquidación |
| `arrow_upward` / `arrow_downward` | Deuda (sale) / crédito (entra) |
| `warning_amber` | Liquidación stale |
| `celebration` | Estado "todos al día" |
| `settings` | Ajustes |
| `contrast` | Selector de tema |
| `tune` | Modo de reparto (partes iguales / por partes) |

Todos los `Icon()` llevan `contentDescription` localizado (o `null` si son decorativos junto a texto).

---

## 7. Diseño por pantalla

Convenciones comunes a todas las pantallas:

- `SplitItScaffold` con fondo `background` cálido y `SplitItTopBar` (icono `arrow_back` real, título `headlineMedium`).
- Estados: **Loading** = skeletons (rectángulos redondeados con shimmer sutil sobre `surfaceVariant`), nunca spinner centrado solo; **Error** = `EmptyState` con icono e `ErrorState` reutilizable; **Vacío** = ilustración + copy + CTA (ver §10).
- Una sola acción primaria visible (FAB o botón inferior).

### 7.1 Lista de Grupos

**Objetivo:** responder "¿en qué grupos estoy y cómo van?" en 3 segundos.

- **App bar grande** (`LargeTopAppBar`) con título "Tus grupos" y acción de ajustes (icono `settings`). El título colapsa a barra normal al hacer scroll.
- **FAB** `add` + "Nuevo grupo" (FAB extendido, colapsa a icono al hacer scroll).
- **Buscador**: campo con `search` leading, clear con `close`, esquinas pill.
- **GroupCard** (firma visual de la app):
  - Izquierda: **avatar stack** — hasta 3 burbujas superpuestas (28 dp, solapadas 8 dp, borde `surface`) con iniciales de participantes; si hay más, burbuja "+N".
  - Centro: título del grupo (`titleMedium`), y debajo metadatos: "4 personas · 12 gastos" (`bodyMedium`, `onSurfaceVariant`).
  - Derecha: **chip de estado**: teal "Al día" (icono `check`) o ámbar "Pendiente" (icono `warning_amber`) si la liquidación está stale o hay gastos sin liquidar.
  - Acciones de editar/borrar pasan a **menú overflow** (`more_vert`) — desaparecen los botones de texto "Edit/Delete".
  - El borrado mantiene diálogo de confirmación (unificado como `ConfirmDeleteDialog`).
- **Vacío**: ilustración de burbujas de avatar flotando hacia un bote + "Aún no tienes grupos" + CTA "Crea tu primer grupo".
- **Sin resultados de búsqueda**: icono `search_off` + copy + botón limpiar.

### 7.2 Detalle de Grupo

**Objetivo:** hub del grupo: estado de las cuentas de un vistazo y acceso a las tres acciones.

- **Cabecera**: avatar stack grande (40 dp) de todos los participantes + título `headlineMedium` + descripción (`bodyLarge`, si existe).
- **Panel de resumen** (card destacada `surfaceVariant`, radio 20):
  - Fila de 3 métricas: **Gastado en total** (`moneyHero` pequeño + tnum), **Gastos** (count), **Personas** (count). La suma de importes se calcula en el `GroupDetailsViewModel` (agregación de `Money`, sin cambio de dominio).
  - Debajo: **estado de la liquidación**: chip teal "Liquidación al día" o banner ámbar con `warning_amber` + "Hay cambios sin liquidar".
- **Acciones** (jerarquía clara, no 3 botones iguales):
  - **Primaria**: botón grande "Ver liquidación" (`account_balance_wallet`), con badge ámbar si está stale.
  - **Secundarias**: dos `OutlinedCard` en fila: "Participantes" (`group`) y "Gastos" (`receipt_long`) con sus contadores.
  - **FAB**: `add` "Añadir gasto" → navega a Gastos con el formulario ya abierto (parámetro de navegación `openExpenseForm` en la ruta de Gastos).
- Navegación de vuelta: `arrow_back` en top bar + gesto nativo.

### 7.3 Formulario de Grupo (crear/editar)

- Top bar: `close` a la izquierda, título contextual "Nuevo grupo" / "Editar grupo", y acción de guardar como **botón de texto "Guardar"** (deshabilitado hasta que el nombre es válido).
- **Campos**: `OutlinedTextField` con radio 12, label flotante, icono leading (`group` para nombre), contador/ayuda y **error inline** con `error` + icono, en el momento de perder foco o al intentar guardar.
- Descripción: campo multilínea con hint de propósito ("¿Viaje? ¿Piso? ¿Cena?").
- Guardando: el botón muestra progreso inline (no spinner a pantalla completa).

### 7.4 Participantes

**Objetivo:** quién forma el grupo, de un vistazo visual.

- **Añadir**: FAB `person_add` "Añadir participante" → **`ModalBottomSheet`** (radio 28):
  - Campo nombre (autofocus, teclado arriba).
  - **Selector de color**: grid de 8 círculos (44 dp) con `check` blanco en el seleccionado; previsualización en vivo del avatar con la inicial a medida que se escribe.
  - Botón primario "Añadir" (deshabilitado sin nombre).
- **Filas**: `AvatarBubble` 48 dp (inicial blanca, `titleMedium`) + nombre + overflow (`edit`/`delete` con confirmación).
- **Edición**: mismo bottom sheet pre-rellenado.
- **Vacío**: ilustración de avatares vacíos + "Nadie por aquí todavía" + CTA. Copy que refuerza que se necesitan ≥ 2 personas para repartir.

### 7.5 Gastos

**Objetivo:** el registro del grupo: qué se pagó, quién lo pagó, entre quiénes.

- **Lista agrupada por fecha**: headers de día pegajosos ("Hoy", "Ayer", "12 ago") usando `dateMillis`. La agrupación se hace en el ViewModel (con test) y los headers usan `stickyHeader` de LazyColumn.
- **ExpenseCard**:
  - Izquierda: `AvatarBubble` 40 dp del **pagador** (semántica: "quién puso el dinero").
  - Centro: título del gasto + metadatos "Pagó Ana · entre 4" (`bodyMedium`). Los gastos de tipo **pago de transferencia** muestran metadata distinta: "Pago a Luis", sin opción de editar; solo borrar.
  - Derecha: **importe** en `moneyRow` + moneda (`onSurfaceVariant`). Nota (si hay) como `bodySmall` truncada a 1 línea.
  - Overflow con edit/delete + confirmación.
- **Buscador** idéntico al de Grupos.
- **FAB** `add` "Añadir gasto" → **formulario a pantalla completa** (no bottom sheet: tiene muchos campos):
  - **Importe hero**: `displaySmall` + tnum centrado, con símbolo/código de moneda, autofocus.
  - Título y nota.
  - **"Pagado por"**: fila horizontal de `AvatarBubble` seleccionables (selección única, anillo `primary` en el elegido).
  - **"Dividir entre"**: chips multi-seleccionables con avatar mini 24 dp + nombre; acceso rápido "Todos".
  - **Modo de reparto**: segmented control "Partes iguales" / "Por monto" (icono `tune`).
    - **Partes iguales** (default): calcula y almacena montos iguales explícitos; camino de 1 tap.
    - **Por monto**: cada participante seleccionado muestra un campo de monto (`OutlinedTextField`) con prefijo de moneda. Indicador en tiempo real del monto restante o excedente. Validación al guardar: la suma debe igualar el total del gasto.
    - Validación: ≥ 1 participante seleccionado; error específico si los montos no coinciden con el total ("Falta distribuir: 500 UYU" / "Excede el total por: 200 UYU").
  - Guardar como botón primario inferior a todo lo ancho.
- **Vacíos**: sin gastos → ilustración recibo + "Apunta el primer gasto"; búsqueda sin resultados → estado dedicado.

### 7.6 Liquidación (pantalla estrella)

**Objetivo:** entender quién debe a quién y saldar. Máxima densidad semántica de la app.

- **Sección Balances — gráfico de barras divergentes** (firma visual, implementación `Canvas` propia, sin librerías):
  - Eje central invisible; cada fila: avatar + nombre, y barra horizontal que crece a la **derecha en teal** (recibe, con `arrow_downward` + "+12,50") o a la **izquierda en coral** (debe, con `arrow_upward` + "−30,00").
  - Ancho de barra proporcional al mayor |balance|; animación de entrada (barras que crecen, 400 ms, `FastOutSlowIn`).
  - "Al día": sin barra, icono `check` + texto neutro.
- **Sección Transfers**: lista de **TransferCard**: `AvatarBubble` A → flecha `swap_horiz`/chevron sobre línea → `AvatarBubble` B, con importe `moneyRow` centrado bajo la flecha. Copy accesible: "Ana paga a Luis 12,50 €". Cada fila incluye un botón secundario **"Marcar como pagado"** (`check`) que registra el pago como un gasto de tipo `TRANSFER_PAYMENT` y regenera la liquidación.
- **Estado stale**: banner `tertiaryContainer` con `warning_amber` + "Los gastos cambiaron desde la última liquidación" + botón "Actualizar".
- **Generar/Regenerar**: botón primario inferior; con progreso inline.
- **Todos al día**: estado de **celebración** — ilustración `celebration`, copy positivo ("Cuentas claras, amistades largas 🎉" — el claim del concepto), teal dominante.

### 7.7 Ajustes

- Lista de preferencias con `SectionHeader`:
  - **General**: fila "Moneda por defecto" con valor actual (ej. "EUR") → abre diálogo selector (lista de códigos comunes + campo libre con validación ISO de 3 letras).
  - **Apariencia**: **segmented buttons** M3 (Sistema `contrast` / Claro `light_mode` / Oscuro `dark_mode`).
  - **Acerca de**: fila con la versión de la app leída del build.
- Guardado inmediato con feedback en snackbar ("Ajustes guardados") en vez de texto estático.

---

## 8. Sistema de componentes (inventario)

Todos en `ui/components/`, cada uno con `@Preview` en light/dark:

| Componente | Descripción |
|---|---|
| `SplitItScaffold` / `SplitItTopBar` | Estructura común, back con icono, soporte large/collapsing |
| `AvatarBubble` | Círculo + inicial(es) + color semántico; tamaños 24/28/40/48 |
| `AvatarStack` | N burbujas solapadas con "+N" |
| `MoneyText` | Importe formateado con tnum, variante hero/row/caption, color semántico opcional |
| `GroupCard` / `ExpenseCard` / `ParticipantRow` / `TransferCard` | Filas de dominio (reemplazan los `Card` ad-hoc) |
| `BalanceBarChart` | Barras divergentes con `Canvas` |
| `StatusChip` | "Al día" / "Pendiente" / "Stale" con color semántico + icono |
| `EmptyState` | Ilustración + título + cuerpo + CTA (genérico) |
| `ConfirmDeleteDialog` | Unifica los diálogos de confirmación de borrado |
| `SearchField` | Rediseño con leading icon y clear con icono |
| `ColorSelector` | Grid de 8 colores con selección |
| `AmountParticipantCard` | Tarjeta de participante con campo de monto para reparto por monto |
| `FormTextField` | TextField con icono, error y contador estandarizados |
| `PrimaryButton` / `SecondaryButton` | Wrappers con estado de carga inline |
| `Skeleton` / `SkeletonList` | Placeholders de carga con shimmer |

**Ilustraciones de empty states:** composiciones vectoriales propias hechas con composables (círculos, formas de la propia paleta: burbujas de avatar, recibo, bote). Sin assets rasterizados, peso cero, escala perfecta y coherente con la marca.

---

## 9. Motion design

| Elemento | Especificación |
|---|---|
| **Navegación** | Transiciones M3: slide horizontal compartido al avanzar/retroceder en jerarquía (300 ms, `FastOutSlowInEasing`), fade para destinos de mismo nivel |
| **Listas** | `Modifier.animateItem()` en LazyColumn: fade+scale sutil al insertar/eliminar gastos o participantes |
| **BalanceBarChart** | Barras que crecen desde el eje al entrar en pantalla (400 ms, stagger de 40 ms por fila) |
| **Press states** | Ripple estándar M3 + scale 0.98 en cards clicables |
| **FAB** | Expandido ↔ icono con animación al hacer scroll |
| **Sheets/diálogos** | Animación M3 por defecto |
| **Reduced motion** | Respetar accesibilidad: si el sistema pide reducir animaciones, se desactivan stagger y crecimiento de barras (fade simple) |

---

## 10. Estados vacíos, carga y error

| Pantalla | Vacío (ilustración + copy + CTA) |
|---|---|
| Grupos | Burbujas flotando a un bote · "Aún no tienes grupos" · "Crea tu primer grupo" |
| Detalle (sin datos) | No aplica (siempre tiene cabecera) |
| Participantes | Avatares fantasma · "Nadie por aquí todavía" · "Añade al primero" |
| Gastos | Recibo · "Apunta el primer gasto" · "Añadir gasto" |
| Liquidación (sin liquidar) | Balanza en equilibrio · "Genera la liquidación para ver quién debe a quién" · CTA |
| Liquidación (saldada) | Celebración · "Cuentas claras, amistades largas" |
| Búsquedas | `search_off` · "Sin resultados para '…'" · Limpiar |
| Errores | Icono `error` + mensaje + "Reintentar" |
| Carga | `SkeletonList` con forma de la fila real (avatar + 2 líneas + importe) |

Todo el copy nuevo se crea en `values/strings.xml` + `values-es/strings.xml` desde el primer momento.

---

## 11. Naming: **Grupos**

- **Copy visible** (strings EN/ES): "Groups"/"Grupos" en títulos, CTAs, empty states, diálogos ("Delete group?", "New group"…).
- **Código**: vocabulario de grupo en toda la app — tipos de dominio (`ExpenseGroup`, `GroupId`, `GroupDetails`), tabla SQLDelight `groups`, rutas (`Groups`, `GroupDetails`, `GroupForm`) y composables (`GroupCard`, `GroupListScreen`…).
- Los strings y el código usan vocabulario "Grupo" de forma consistente, sin restos de "Session/Sesión".
