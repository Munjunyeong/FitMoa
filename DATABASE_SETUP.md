# Database setup

Before starting the app after enabling password hashing, run
`database/migrations/002_account_password_hash.sql` once against the `fitmoa`
database. This widens `ACCOUNT.PASSWORD` to hold BCrypt hashes. Existing
plain-text passwords are upgraded to BCrypt the first time each user logs in.

`application.properties` retains the project's existing local MariaDB settings.
Keep this local password configuration out of any public repository.
