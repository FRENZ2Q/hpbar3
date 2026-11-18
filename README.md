# HP Bar (Fabric, Minecraft 1.21.4)

Клиентский мод: полоска здоровья над игроками, животными и мобами.
Работает над невидимыми сущностями (в т.ч. игроками с зельем невидимости).

Настройки: клавиша **H** в игре (или файл `config/hpbar.json`).
В конфиге есть `hiddenTypes` — список ID типов, которые надо скрыть, например `["minecraft:bat"]`.

## Сборка
Нужны JDK 21 и Gradle 8.12+.

    gradle wrapper --gradle-version 8.12
    ./gradlew build          (Windows: gradlew.bat build)

Готовый мод: `build/libs/hpbar-1.0.0.jar` (НЕ файл с -sources).
Кинуть в папку `mods` вместе с Fabric API.

Без установки Gradle: залей проект на GitHub — вкладка Actions соберёт jar сама.

