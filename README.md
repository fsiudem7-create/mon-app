# PlanBoss

PlanBoss est une application **Kotlin Multiplatform + Compose Multiplatform** (Android + Windows Desktop) orientée exécution de planning avec intégrations Notion et Google Calendar.

## 1) Architecture + décisions

### Choix du stack
- **Choix retenu:** KMP + Compose Multiplatform.
- **Justification:** partage maximal du code métier (domain/data/logic), UI cohérente Android/Windows, Kotlin natif pour intégration Android notifications/work manager, JVM Desktop pour Windows.

### Modules
```
PlanBoss/
├─ shared/                  # logique partagée multiplateforme
│  ├─ domain/               # modèles métier
│  ├─ data/                 # clients APIs + repositories
│  ├─ logic/                # sync/linking/discipline/notifications/focus
│  ├─ ui/                   # écrans Compose partagés
│  └─ settings/             # paramètres applicatifs
├─ androidApp/              # entrypoint Android
└─ desktopApp/              # entrypoint Desktop Windows
```

## 2) Diagramme textuel

```
[Android UI Compose] ----\
                         > [shared/ui + shared/logic] -> [shared/data]
[Desktop UI Compose] ----/               |                    |
                                         |                    +--> Google Calendar API
                                         +--> discipline/linking/focus/scheduler
                                                              +--> Notion API
```

## 3) Schéma DB locale (implémentation prévue SQLDelight/Room)

Tables:
- `tasks(id, notion_id, title, status, due_at, start_at, duration_minutes, priority, source, external_event_id, last_notified_at)`
- `events(id, calendar_id, title, start_at, end_at, location, description, is_completed_local)`
- `links(task_id, event_id, link_confidence, link_type)`
- `focus_sessions(id, related_task_id, related_event_id, start_at, end_at, duration_seconds, mode, notes)`
- `completed_events(event_id, completed_at)`
- `sync_state(provider, last_success_at, last_error, cursor, token_meta)`
- `notification_log(key, emitted_at, channel, action)`

## 4) Intégrations OAuth/API

### Google Calendar
- OAuth2 Google Sign-In (scopes minimaux `calendar.readonly`, extension write optionnelle).
- Fonctions: listing calendriers, sélection des calendriers à synchroniser, lecture des événements par fenêtre temporelle.
- Done local: table `completed_events` (pas de done natif GCal).

### Notion
- API officielle Notion.
- OAuth Notion (ou token integration fallback).
- Mapping robuste si champs manquants: fallback par défaut dans `NotionClient`.
- Fonctions: lecture Tasks DB + update statut.

### Notion Calendar
- À date: pas d’API publique stable garantissant toutes les opérations.
- **Plan B automatique activé:**
  1. sync via Google Calendar (source principale calendrier),
  2. option future ICS feed si URL disponible.

## 5) UX / Fonctionnalités
- Dark mode par défaut (palette noir/charbon + accent unique cyan).
- Tabs partagés: Aujourd’hui, Tâches, Calendrier, Focus.
- Focus: stopwatch + pomodoro.
- Discipline mode: scoring + message coaching + plan de rattrapage.
- Anti-spam notifications: dédup + limite `maxPerHour`.

## 6) Installation et exécution

### Prérequis
- JDK 17+
- Android Studio Iguana+ (SDK 34)
- (Windows) JDK + Gradle

### Android
1. Créer OAuth client Android dans Google Cloud.
2. Configurer SHA-1/SHA-256 + package `com.planboss`.
3. Ajouter la config OAuth dans stockage sécurisé (EncryptedSharedPreferences/Keystore).
4. Lancer:
   ```bash
   ./gradlew :androidApp:assembleDebug
   ```

### Windows Desktop
1. Créer OAuth client Desktop dans Google Cloud.
2. Configurer redirect URI loopback (localhost) pour OAuth PKCE.
3. Injecter `GOOGLE_CLIENT_ID`, `NOTION_CLIENT_ID` via variables d’environnement.
4. Lancer:
   ```bash
   ./gradlew :desktopApp:run
   ```

### Config Notion
1. Créer integration Notion + récupérer token/OAuth.
2. Partager la base Tasks avec l’intégration.
3. Vérifier mapping des propriétés (`Name`, `Status`, `Due`, etc.).

## 7) Checklist tests
- `LinkingEngine`: heuristique titre tâche <-> événement.
- `NotificationEngine`: anti-spam et déduplication.
- `DisciplineEngine`: scoring compliance.

## 8) Améliorations futures
- Persistances SQLDelight/Room complètes (offline queue + journal conflits).
- WorkManager Android + scheduler natif Windows pour notifications hors app.
- Sync incrémentale avec tokens/cursors API.
- Écran Settings complet (rappels, discipline mode, intervalle sync) connecté à stockage chiffré.
