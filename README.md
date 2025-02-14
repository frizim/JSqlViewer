# JSQLViewer [Experimental]
This is a simple graphical Java application for viewing and editing the data stored in any database and table of the configured database server.

## Configuration
JSQLViewer expects a file called `database.properties` in its working directory.
Properties read from this file are passed to HikariCP at startup, so any configuration option described [here](https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby) is valid.
A sample configuration for a MySQL/MariaDB server running on your local machine looks like this:
```
jdbcUrl=jdbc:mysql://localhost:3306/
username=root
password=secret
```

The configuration database user requires read access to the information_schema database in order to enumerate databases and tables.