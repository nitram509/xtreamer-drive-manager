
# Xtreamer Drive Manager

When you're using one of the famous [Xtreamer](http://www.xtreamer.net/) media players,
Driver Manager helps you organize the folder structure of your disk drives.

## Requirements

* Java 1.8+
* Maven 3.1+

The pom.xml configures Win32-64bit at the moment, you may adjust the dependency to fit your platform.

## TheMovieDB.org API-KEY

When starting Xtreamer Drive Manager you have to provide an API KEY from 'themoviedb.org'.
This can be done via environment variable:

````export API_KEY=ab123456789012345678901234567890cd````

or via Java system property:

````java -DAPI_KEY=ab123456789012345678901234567890cd de.bitkings.ManageDrives````

