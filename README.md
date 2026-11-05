# FLUX CLIENT (`1.21.11`)

**FLUX CLIENT** is a lightweight, high-performance **Minecraft Java Edition `1.21.11`** Fabric client mod developed by **JIGNESHGAMING-k7j**.

Designed for both **Desktop Java 21** and **low-end Android Java launchers** (such as PojavLauncher / Zalith / MojoLauncher), FLUX CLIENT delivers a sleek custom **Main Menu**, an orange-and-black **Mod Menu**, **22 real working modules**, **6 instant HUD Presets**, a **drag-and-drop HUD Layout Editor**, client-side **Cosmetics**, and **genuine GPU/CPU frame-pacing optimizations** with zero fake FPS counters and zero `java.awt` desktop-only dependencies.

---

## Key Features

### 1. Custom FLUX CLIENT Main Menu & Unified Dark Menu Backdrop
- Desaturated dark voxel landscape backdrop (`ScreenMixin`, `EntryListWidgetMixin` & `FluxMainMenuScreen`) applied consistently across the **Main Menu**, **Multiplayer Screen**, **World Selection Screen**, and **Options / Settings Screens**, replacing vanilla's heavy 6-face rotating 3D panorama and multi-pass Gaussian blur to cut idle GPU fillrate on mobile GPUs.
- Top-right live player chip + 4 quick-action buttons (**Mod Menu**, **Quick Messages**, **Screenshots Folder**, **Minecraft Settings**) + crimson **Quit** button.
- Side-by-side glowing electric-blue `F` emblem + `FLUX CLIENT` hero branding with smooth blue-bloom hover transitions on all 6 buttons:
  - **Singleplayer**, **Multiplayer**, **Cosmetics**, **Mods**, **Store**, and **Quit Game**.
- Bottom-right two-tone **JOIN OUR DISCORD / CLICK HERE ->** card and clean `Flux Client 1.21.11` version footer (with zero floating launcher overlays).

### 2. Custom Orange & Black In-Game Mod Menu (`Right Shift`)
- **Category Filters**: `All`, `HUD`, `Render`, `Movement`, `Player`, `Utility`.
- **Live Search**: Instant filtering across module names, descriptions, and categories.
- **4-Column Scrollable Module Grid**:
  - Signature top orange accent strip (`#EE5A24`) when enabled.
  - Custom crisp geometric module icons.
  - Per-module **Gear** settings modal (boolean switches, draggable numeric sliders, mode selectors, color presets, and inline text inputs).
  - Emerald **Enabled** / slate **Disabled** toggle state.
- **HUD Presets Modal (`Presets`)**:
  - `01` **PvP Focus** — FPS, Server Info, Direction, Auto Sprint, Crosshair, Hurt Shake Off, Scoreboard & Full Bright.
  - `02` **Stream Clean** — Minimal edge-aligned FPS, Coordinates & Clock.
  - `03` **Survival Scout** — FPS, Coordinates, Direction, Biome, Clock, Full Bright & Zoom.
  - `04` **Performance** — FPS, Memory Usage, Server Info + Entity & Particle culling enabled.
  - `05` **Builder Utility** — Coordinates, Direction, Biome, Clock, Block Overlay, Time Changer & Zoom.
  - `06` **Custom Layout** — Snapshots and restores your custom dragged HUD positions & active modules.
- **Reset Confirmation Modal (`Reset`)**:
  - **DEFAULTS** (`Restore original setup`)
  - **REMOVE ALL** (`Disable every module`)
- **Drag-and-Drop HUD Editor (`H` key or Monitor icon)**:
  - Drag any active HUD module anywhere on screen with center-axis snapping and **mouse-wheel scaling** (`0.7x` – `1.8x`).

### 3. All 22 Working Modules

| Category | Module | Description |
| :--- | :--- | :--- |
| **HUD** | **FPS Counter** | Live frame rate readout with optional frame-time (`ms`) and min/max tracking. |
| **HUD** | **Coordinates** | Real-time `X / Y / Z` position, chunk coordinates, and Nether/Overworld dimension conversion. |
| **HUD** | **Direction** | Cardinal/intercardinal compass (`N`, `NE`, `E`, `SE`, `S`, `SW`, `W`, `NW`) + exact yaw/pitch. |
| **HUD** | **Clock** | Real-world 12h/24h system time or in-game world day & clock time. |
| **HUD** | **Memory Usage** | JVM heap usage (`%` and `Used / Max MB`) with allocation bar. |
| **HUD** | **Biome** | Formatted current biome name from the client world chunk registry. |
| **HUD** | **Resource Packs** | Active resource pack count and top pack summary. |
| **HUD** | **Server Info** | Current server address (`Singleplayer` / server IP) and live player list ping (`ms`). |
| **Render** | **Full Bright** | Blaze3D `1.21.11` Lightmap UBO brightness boost + optional Darkness pulse suppression. |
| **Render** | **Block Overlay** | Custom colored block selection wireframe & optional translucent face fill. |
| **Render** | **Hitboxes** | Custom entity bounding boxes with optional eye-height ring and look-vector ray. |
| **Render** | **Remove Hurt Shake** | Cancels or scales camera damage tilt (`0%` – `100%`) for steady aim. |
| **Render** | **Hide Boss Bar** | Cleanly suppresses top-screen boss bar overlays. |
| **Render** | **Time Changer** | Client-side sky time override (`Sunrise`, `Day`, `Noon`, `Sunset`, `Night`, `Midnight`, `Custom`). |
| **Render** | **Custom Crosshair** | Crisp `Cross`, `Circle`, `Dot`, or `Cross + Dot` reticle with dynamic attack-cooldown & entity target tinting. |
| **Render** | **Custom Scoreboard** | Clean sidebar scoreboard with toggleable red score numbers, custom opacity, and vertical offset. |
| **Movement** | **Auto Sprint** | Server-safe automatic forward sprinting respecting hunger, sneaking, blindness, and item usage. |
| **Player** | **Toggle Sneak** | Hands-free crouch toggle with optional menu auto-release. |
| **Utility** | **Zoom** | Smooth optical FOV zoom (`C` key) with scroll-wheel magnification and proportional mouse sensitivity scaling. |
| **Utility** | **Auto GG** | Detects match-end chat messages on popular servers with anti-spam cooldown and customizable message. |
| **Utility** | **Quick Messages** | 6 customizable chat/command macro slots (`G` key in-game). |
| **Utility** | **Notifications** | Smooth slide-in bottom-right status toasts for module & preset actions. |

### 4. Low-End & PojavLauncher Optimization Engine
Accessible via the **Gauge** tab in the Mod Menu header (`LowEndOptimizer`):
- **Distance-Based Entity Culling** (`EntityRenderManagerMixin`): Skips rendering distant ambient/passive mobs, armor stands, item frames, and dropped items/XP orbs beyond configurable thresholds while **never** culling players, bosses, glowing entities, or custom-named entities.
- **Distance-Based Block Entity Culling** (`BlockEntityRenderManagerMixin`): Skips building render state and drawing distant chests, signs, banners, and skulls while keeping long-range beacons and end gateways/portals visible.
- **Particle Burst & Distance Limiter** (`ParticleManagerMixin`): Caps per-tick particle spawns and distant particles so TNT, crystal, or weather bursts do not stall mobile Tile-Based Deferred Rendering (TBDR) GPUs.
- **Menu Gaussian Blur Bypass** (`ScreenMixin`): Bypasses expensive multi-pass FBO Gaussian blur shaders when opening menus on PojavLauncher / GL4ES / mobile GPUs.
- **Zero-Allocation HUD Pipeline**: Caches throttled readout strings (`FPS`, `Memory`, `Clock`, `Biome`, `Direction`, `Server Info`, `Resource Packs`) so HUD rendering allocates zero temporary lists or strings per frame on tight 3 GB RAM heaps.
- **Unfocused FPS Limiter** (`MinecraftClientMixin`): Caps frame rate when unfocused to reduce battery drain and thermal throttling.
- **One-Click Low-End Profile**: Applies heap-aware view/simulation distances (`5/4` chunks on `<= 1.2 GB` heaps, `6/5` chunks otherwise), fast clouds/off, `0` biome blend, `0` menu blur, and reduced particles safely.
- **Zero `java.awt` Dependencies**: Pure integer ARGB/HSV math ensures full compatibility with stripped mobile Java 21 runtimes.

---

## Default Keybinds

| Key | Action |
| :--- | :--- |
| **Right Shift** | Open FLUX Mod Menu |
| **H** | Open Drag-and-Drop HUD Layout Editor |
| **C** | Zoom (Hold or Toggle mode) |
| **G** | Open Quick Messages Popup (when Quick Messages module is enabled) |

*(All keybinds can also be rebound inside Minecraft's standard `Options -> Controls -> Key Binds -> FLUX CLIENT` menu.)*

---

## Prebuilt JAR Deliverable

The compiled, remapped Fabric `1.21.11` mod JAR is located in:

```text
release/flux-client-1.0.0+mc1.21.11.jar
```

---

## Installation Instructions (Desktop & PojavLauncher)

### Requirements
- **Minecraft Java Edition**: `1.21.11`
- **Mod Loader**: [Fabric Loader](https://fabricmc.net/) `0.17.3+` (tested with `0.18.6`)
- **Required Dependency**: [Fabric API](https://modrinth.com/mod/fabric-api) for `1.21.11` (`0.141.6+1.21.11` or newer)
- **Java Runtime**: Java `21`

### Desktop (Official Launcher / Prism Launcher / Modrinth App)
1. Install **Fabric Loader** for Minecraft **`1.21.11`**.
2. Place **Fabric API (`1.21.11`)** and `flux-client-1.0.0+mc1.21.11.jar` into your `.minecraft/mods/` folder.
3. Launch the `fabric-loader-1.21.11` profile.
4. Configuration is automatically saved to `.minecraft/config/fluxclient/config.json`.

### Android Java Launchers (Vivo 1916 — Android 9, 3 GB RAM / PojavLauncher)
1. Select or create a **Minecraft `1.21.11` Fabric** instance configured to use **JRE / Java 21**.
2. Copy **Fabric API (`1.21.11`)** and `flux-client-1.0.0+mc1.21.11.jar` into the instance's `.minecraft/mods/` directory.
3. On **3 GB RAM devices (e.g., Vivo 1916)**, allocate **`950 MB – 1150 MB` RAM** (avoid allocating over `1.3 GB` so Android 9 and the OpenGL/GL4ES/LTW driver keep native memory headroom and avoid LMK force-closes). On 4 GB+ devices, `1280 MB – 2048 MB` is ideal.
4. Launch the game, open **Mods -> Gauge Icon (Low-End Optimizer)**, and click **Apply Low-End Profile** for optimal mobile frame pacing.
5. *(Recommended compatible performance mods)*: Pairs cleanly alongside **Sodium**, **Lithium**, **FerriteCore**, and **ImmediatelyFast** for `1.21.11`.

---

## Building From Source

```bash
git clone https://github.com/JIGNESH-HOSTING/FLUX-CLIENT-.git
cd FLUX-CLIENT-
./gradlew --no-daemon clean build
```

The remapped production mod JAR is generated at:

```text
build/libs/flux-client-1.0.0+mc1.21.11.jar
```

---

## Credits
- **Client Name**: FLUX CLIENT (`1.21.11`)
- **Developer**: **JIGNESHGAMING-k7j**
- **License**: LGPL-3.0-only
