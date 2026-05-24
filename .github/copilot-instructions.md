# Wynntils Copilot Coding Agent Instructions

## Project Overview

**Wynntils** is a Minecraft mod for the Wynncraft MMORPG server that enhances gameplay with customizable options and additions. This is a complete rewrite (originally codenamed Artemis) of the legacy Wynntils mod.

- **Technology Stack**: Java 21, Minecraft 1.21.4, Architectury (multi-loader architecture)
- **Supported Loaders**: Fabric and NeoForge
- **Build System**: Gradle 9.0.0 with multi-module project structure
- **License**: GNU Lesser General Public License v3.0
- **Repository Size**: Large (~100+ packages, substantial codebase)

## Critical Build Requirements

### Java Version
**ALWAYS use Java 21 (JDK 21).** The project requires Java 21 as specified in `gradle.properties` (`java_version=21`). Using Java 17 or other versions will cause build failures.

### Environment Setup
1. **Initial Setup**: Import the project as a Gradle project. Run `./gradlew --refresh-dependencies` if your IDE doesn't automatically do it.
2. **Java Home**: Ensure `JAVA_HOME` points to JDK 21 installation
3. **Gradle Wrapper**: Always use `./gradlew` (project includes its own Gradle 9.0.0 wrapper)

## Build Commands (Execute in This Order)

### Code Formatting (CRITICAL - Run Before Every Commit)
```bash
# Check formatting (required before PR merge)
./gradlew spotlessCheck

# Auto-fix formatting issues
./gradlew spotlessApply
```

**ALWAYS run `spotlessApply` before committing code.** Spotless uses the Palantir Java format engine and enforces strict code style rules. GitHub Actions will auto-commit formatting fixes, but running locally saves CI time.

### Building
```bash
# Build all modules (includes common, fabric, and neoforge)
./gradlew buildDependents -x spotlessCheck -x test

# Output artifacts are in:
# - fabric/build/libs/wynntils-VERSION-fabric+MC-1.21.4.jar
# - neoforge/build/libs/wynntils-VERSION-neoforge+MC-1.21.4.jar
```

**Build Time**: Expect 2-5 minutes for a clean build.

### Testing
```bash
# Run all tests
./gradlew test -x spotlessCheck --stacktrace --info

# Test reports are in: fabric/build/reports/tests/test/
```

**Test Location**: Tests are primarily in `fabric/src/test/java/` directory.

### Complete CI Workflow (Matches GitHub Actions)
```bash
# 1. Format code
./gradlew spotlessApply

# 2. Remove empty files created by spotless
find $(git ls-files -m) -size 0 -delete

# 3. Clean up unused i18n translations
bash utils/remove_unused_i18n.sh

# 4. Build
./gradlew buildDependents -x spotlessCheck -x test

# 5. Test
./gradlew test -x spotlessCheck --stacktrace --info
```

## Project Architecture

### Module Structure (Multi-Module Gradle Project)
```
Wynntils/
├── common/          # Shared code for both loaders (primary module)
├── fabric/          # Fabric-specific code and tests
├── neoforge/        # NeoForge-specific code
├── build.gradle     # Root build configuration
└── settings.gradle  # Multi-module project settings
```

### Source Code Organization
The `common/src/main/java/com/wynntils/` directory contains:

**Core Framework** (`core/` package):
- `WynntilsMod.java` - Main mod entry point and initialization
- `components/` - Managers, Models, Services, Handlers (component registry pattern)
- `events/` - Event system and event definitions
- `mod/` - Mod loading and lifecycle management

**Feature Modules** (`features/` package):
- `chat/`, `combat/`, `commands/`, `debug/`, `embellishments/`
- `inventory/`, `map/`, `overlays/`, `players/`, `tooltips/`
- `trademarket/`, `ui/`, `utilities/`, `wynntils/`

**Services** (`services/` package):
- `athena/` - Wynntils account service (authentication, cloud configs)
- `chat/`, `cosmetics/`, `discord/`, `hades/`, `itemfilter/`
- `lootrunpaths/`, `map/`, `mapdata/`, `resourcepack/`, `statistics/`

**Models** (`models/` package):
- Domain-specific models: `abilities/`, `character/`, `items/`, `territories/`, etc.

**Mixins** (`mc/mixin/` package):
- Minecraft bytecode injection points for mod functionality

**UI Screens** (`screens/` package):
- Custom UI implementations for various features

**Commands** (`commands/` package):
- In-game commands (e.g., `LootrunCommand.java`)

### Configuration Files

- **Build Config**: `build.gradle` (root), `common/build.gradle`, `fabric/build.gradle`, `neoforge/build.gradle`
- **Gradle Properties**: `gradle.properties` (version declarations, Gradle JVM args)
- **Settings**: `settings.gradle` (includes common, fabric, neoforge modules)
- **Spotless Config**: Defined in root `build.gradle` (Java, JSON, Groovy Gradle formatting)
- **Greclipse**: `greclipse.properties` (Groovy formatting configuration)
- **Access Widener**: `common/src/main/resources/wynntils.accessWidener`
- **Mod Metadata**: `fabric/src/main/resources/fabric.mod.json`, `neoforge/src/main/resources/META-INF/mods.toml`
- **Qodana**: `qodana.yaml` (code quality analysis)

## Code Style Rules (From CONTRIBUTING.md)

1. **Braces**: Always use braces for control statements (loops, conditions). Exception: Single-line if statements with only control flow (return, continue, break) and no else clause.

2. **Fields**: Keep final and non-final fields in separate blocks. One field declaration per line.

3. **Imports**: NO wildcard imports. Spotless will detect and reject them.

4. **Annotations**: Do NOT use IntelliJ annotations (org.jetbrains.annotations.*). Spotless will reject them.

5. **Method Organization**: Generally public (including @SubscribeEvent), then protected, then private methods. Adapt to surrounding code style.

6. **Comments**: Match existing comment style. Add comments only when necessary to explain complex logic.

## Critical Development Considerations

### Spotless Formatting
- Spotless runs automatically on PR creation via GitHub Actions
- **Custom Rules**:
  - Refuses wildcard imports (cannot be auto-fixed)
  - Refuses IntelliJ annotations (cannot be auto-fixed)
  - No empty line after opening curly brace (auto-fixed)
  - License header with current year (auto-fixed)
  - Import ordering, unused import removal (auto-fixed)
- Run `spotlessApply` locally before committing to avoid CI churn

### Git Hooks (Optional)
Pre-commit hook available: `git config core.hooksPath utils/git-hooks`
This runs spotlessApply automatically on commit.

### Conventional Commits
**PR titles MUST follow [Conventional Commits](https://www.conventionalcommits.org/) format** (e.g., `feat:`, `fix:`, `docs:`, `refactor:`). This is required for release logs and auto-build.

### Testing
- Test files located in `fabric/src/test/java/`
- Uses JUnit 5 (Jupiter)
- Example test pattern: `TestModels.java`, `TestCustomColor.java`, `TestUtils.java`
- Setup uses `WynntilsMod.setupTestEnv()` in `@BeforeAll` methods
- Tests run in fabric module context

## GitHub Actions CI Pipeline

The `.github/workflows/build.yml` runs on PRs and performs:
1. **Checkout** code (with full history, `fetch-depth: 0`)
2. **Setup JDK 21** (Temurin distribution)
3. **Cache Gradle** data (`.gradle` directory)
4. **Format with Spotless** (`./gradlew spotlessApply`)
5. **Delete empty files** created by formatting
6. **Run i18n cleanup** (`bash utils/remove_unused_i18n.sh`)
7. **Auto-commit** formatting changes (as WynntilsBot)
8. **Build** (`./gradlew buildDependents -x spotlessCheck -x test`)
9. **Test** (`./gradlew test -x spotlessCheck --stacktrace --info`)
10. **Upload test reports** (always, even on failure)

## Key Dependencies and Frameworks

### Mod Loaders
- **Fabric**: Loader version 0.16.10, API version 0.114.2+1.21.4
- **NeoForge**: Version 21.4.121, EventBus 8.0.2

### Architectury
- Multi-loader abstraction framework
- Plugin: 3.4-SNAPSHOT
- Loom: 1.11-SNAPSHOT

### Development Tools
- **Parchment**: Mappings for better parameter names (1.21.4:2025.03.23)
- **MixinExtras**: Version 0.5.0 (bytecode manipulation utilities)
- **DevAuth**: Version 1.2.1 (Microsoft account auth in dev environment)
- **HotswapAgent**: 1.4.2-SNAPSHOT (for live code editing, optional)
- **Spotless**: 7.2.1 (code formatting)
- **Shadow**: 9.1.0 (JAR shading for dependencies)

### Custom Libraries
- **Hades**: Version 0.6.0 (protocol library)
- **Antiope**: Version 0.2.2 (utility library)

### Testing
- **JUnit**: Version 5.13.4 (Jupiter API)
- **Fabric Loader JUnit**: For Fabric-specific testing

## Common Pitfalls and Workarounds

### Build Failures
1. **"Plugin architectury-plugin not found"**: Usually indicates missing Maven repository or network issue. Ensure internet connection is stable. The plugin uses SNAPSHOT versions from https://maven.architectury.dev/.

2. **"Unsupported Java version"**: Verify `JAVA_HOME` points to JDK 21. Check with `java -version` (should show "21.x.x").

3. **Spotless Check Failures**: Run `./gradlew spotlessApply` before committing. If errors persist about wildcard imports or IntelliJ annotations, manually fix them (spotlessApply cannot auto-fix these).

### File Locations
- **Main entry point**: `common/src/main/java/com/wynntils/core/WynntilsMod.java`
- **Mod initialization**: Search for `init()` method in WynntilsMod
- **Component registry**: `Managers`, `Handlers`, `Models`, `Services` classes in `core/components/`
- **Language files**: `common/src/main/resources/assets/wynntils/lang/en_us.json`

### Testing Setup
Tests require special initialization via `WynntilsMod.setupTestEnv()` which:
- Initializes Minecraft shared constants
- Bootstraps the game environment
- Loads I18n translations from file system
- Initializes Wynntils managers, configs, features, and services

## Making Code Changes

### Workflow
1. **Create a feature branch** from `main` or `development`
2. **Make minimal changes** - only modify what's necessary
3. **Run Spotless**: `./gradlew spotlessApply`
4. **Build**: `./gradlew buildDependents -x spotlessCheck -x test`
5. **Test**: `./gradlew test -x spotlessCheck`
6. **Commit with conventional commit message** (e.g., `fix: resolve item tooltip rendering`)
7. **Push and create PR** with conventional commit title

### Finding Code
- **Feature logic**: Look in `common/src/main/java/com/wynntils/features/`
- **Game integration**: Check `common/src/main/java/com/wynntils/mc/mixin/`
- **Data models**: Examine `common/src/main/java/com/wynntils/models/`
- **Services**: Review `common/src/main/java/com/wynntils/services/`
- **Commands**: Investigate `common/src/main/java/com/wynntils/commands/`

### Adding Dependencies
1. Check `gradle.properties` for dependency version variables
2. Add dependency in appropriate module's `build.gradle`
3. For common module dependencies, also add to fabric and neoforge build files
4. Run `./gradlew --refresh-dependencies` after adding

## Trust These Instructions

These instructions are comprehensive and validated against the actual repository structure, documentation, and CI configuration. Only search for additional information if:
- These instructions are incomplete for your specific task
- These instructions contain errors or outdated information
- You need implementation details not covered here

For most code changes, builds, tests, and formatting tasks, these instructions contain everything you need to work efficiently.
