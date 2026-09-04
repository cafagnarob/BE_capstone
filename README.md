# BE_capstone — Rider App (Backend)

API REST per Rider App, un'applicazione social e di ride-tracking per motociclisti: gestione profilo e garage moto,
organizzazione di eventi/raduni, tracciamento dei percorsi in moto con mappe, feed social (post, commenti, like, follow)
e notifiche.

Questo repository contiene solo il backend. Il frontend (React) vive in un repository separato:

Repository frontend: https://github.com/cafagnarob/rider-app-frontend
Frontend live (demo): https://rider-app-frontend-ten.vercel.app/

Se questo repository è privato, per consultarlo è necessario essere invitati come collaboratori: contattare l'autore (
cafagna.rob@gmail.com) oppure vedi la sezione "Accesso al repository" più sotto.

# Stack tecnologico

Java 21, Spring Boot 4.1 (Web, Data JPA, Security, Validation)
PostgreSQL come database relazionale
Spring Security + JWT (io.jsonwebtoken / jjwt) per l'autenticazione stateless
Cloudinary per l'upload e la gestione delle immagini
Mapbox Directions API + MapTiler per il calcolo dei percorsi e il geocoding
Spring Mail per l'invio di email (verifica account, reset password)
Lombok, Datafaker (seed dei dati di sviluppo)
Maven (build), Docker (containerizzazione)

# Moduli principali / dominio applicativo

Modulo Descrizione
Auth Registrazione, login, verifica email, reset password
Users & Profile Profilo utente, profilo pubblico, link social, ricerca utenti
Garage Brand moto, modelli moto, veicoli dell'utente
Events Creazione/ricerca eventi, partecipazioni, inviti, codici di accesso
Rides & Routes Tracciamento ride, calcolo/anteprima percorsi (Mapbox)
Social Post, commenti, like, follow, notifiche
Admin Gestione utenti, brand e modelli moto

# Requisiti

JDK 21
Maven (in alternativa è incluso il wrapper mvnw / mvnw.cmd, non serve Maven installato)
PostgreSQL 14+ in esecuzione localmente (oppure via Docker)
Facoltativo: Docker, se si preferisce eseguire l'app containerizzata

# Setup e avvio in locale

Clonare il repository:
bash
git clone https://github.com/cafagnarob/BE_capstone.git
cd BE_capstone
Creare un database PostgreSQL locale, ad esempio BE_capstone.
Creare nella root del progetto un file env.properties (già escluso da .gitignore, non va mai committato) con le
variabili elencate nella tabella sottostante.
Avviare l'applicazione:
bash
./mvnw spring-boot:run
L'API sarà disponibile su http://localhost:8080.

# Variabili d'ambiente

Le variabili vengono lette dal file env.properties nella root del progetto (vedi spring.config.import=optional:file:
./env.properties in application.properties). Nessun valore reale è incluso in questo README per motivi di sicurezza:
sostituire i placeholder con le proprie credenziali.

Variabile Descrizione Esempio / dove ottenerla
PORT Porta su cui gira l'app (facoltativa, default 8080)    8080
DB_URL URL JDBC del database PostgreSQL jdbc:postgresql://localhost:5432/BE_capstone
DB_USER Utente database postgres
DB_PASSWORD Password database la-tua-password
CLOUDINARY_CLOUD_NAME Cloud name Cloudinary dashboard Cloudinary
CLOUDINARY_API_KEY API key Cloudinary dashboard Cloudinary
CLOUDINARY_API_SECRET API secret Cloudinary dashboard Cloudinary
JWT_SECRET Chiave segreta per firmare i JWT stringa lunga e casuale, generata da te
JWT_EXPIRATION Scadenza token (proprietà richiesta all'avvio; la scadenza effettiva nel codice è attualmente fissata a 7
giorni)    604800000 (ms)
MAIL_HOST Host SMTP smtp.gmail.com
MAIL_USERNAME Account email mittente la tua email
MAIL_PASSWORD Password / App Password dell'account email vedi impostazioni account Google (App Password)
MAPBOX_ACCESS_TOKEN Token Mapbox Directions API account Mapbox
MAPTILER_ACCESS_KEY API key MapTiler (geocoding)    account MapTiler
FRONTEND_URL URL del frontend, usato nei link generati (es. email)    http://localhost:5173 (dev)

# Esecuzione con Docker

Il progetto include un Dockerfile multi-stage (build Maven + runtime JRE):

bash
docker build -t be-capstone .
docker run --env-file env.properties -p 8080:8080 be-capstone

# Autenticazione

L'API usa JWT Bearer token. Tutte le rotte sotto /auth/** sono pubbliche (registrazione, login, verifica email, reset
password); tutte le altre richiedono l'header:

Authorization: Bearer <token>

# Panoramica degli endpoint principali

Base path Descrizione
/auth Registrazione, login, verifica email, reset/forgot password
/users Profilo utente, ricerca utenti, profilo pubblico
/users/{username}/follow Follow/unfollow
/vehicles Veicoli dell'utente (garage)
/brands, /motorcycle-models Catalogo marche e modelli moto
/events Creazione, ricerca, partecipazione, inviti agli eventi
/events/{eventId}/participations Partecipazioni a un evento
/rides Ride tracciati dall'utente
/routes Calcolo/anteprima percorsi (integrazione Mapbox)
/posts Feed social: creazione e lettura post
/posts/{postId}/comments Commenti ai post
/posts/{postId}/likes Like ai post
/notifications Notifiche utente
/admin/users, /admin/brands, /admin/motorcycle-models Endpoint di amministrazione

# CORS

Le origini attualmente consentite (config/CorsConfig.java) sono:

http://localhost:5173 (Vite dev server)
http://localhost:3000
https://rider-app-frontend-ten.vercel.app/

Se il frontend viene pubblicato su un dominio diverso, aggiungere l'URL a questa lista.

# Deploy

Il backend è online su Render, buildato a partire dal Dockerfile incluso nel repository:

API live: https://be-capstone-kyub.onrender.com

Nota: se il servizio è ospitato su un piano gratuito, dopo un periodo di inattività la prima richiesta può richiedere
alcuni secondi in più (cold start).

# Accesso al repository

Per chi deve valutare il progetto avendo accesso solo al repository frontend:

Il frontend consuma l'API live sopra indicata: non è necessario avviare il backend in locale per provare l'applicazione.
Per consultare il codice sorgente del backend, il repository è: https://github.com/cafagnarob/BE_capstone — se privato,
richiedere l'accesso come collaboratore all'autore.

# Sicurezza

Il file env.properties contiene credenziali reali (database, JWT, Cloudinary, email, Mapbox) e non deve mai essere
versionato: è già escluso tramite .gitignore. Nessun valore reale è presente in questo README.

# Autore

Roberto Cafagna — Progetto capstone, corso Full Stack Developer.