# Royalty Manager

Simple exercise consisting of an API for keeping track and calculating royalties to rights owners.
Built using Scala, Cats and Http4s

## Requirements

In order to build and test the project, you need to have SBT 1.0 or greater installed

## How to build it?

to compile, run `sbt compile`
to run the tests run `sbt test`
to start the service run `sbt run`

## How does it work?

Im using http4s to create the api, and implementing an in-memory solution instead of using a database, for easing the example. In a proper implementation you would have to provide an implementation of `$ENTITY$RepositoryAlgebra`, which is the interface.

## Settings

You can change settings like host or port, located in the file `src/main/resources/application.conf`

