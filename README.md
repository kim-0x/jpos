# JPOS

JPOS is a point of sale system for small grocery stores. It helps store owners manage products, inventory, pricing, sales activity, and reporting from a single Java application.

## Overview

The application is designed to support day-to-day store operations with role-based access for different types of users:

- **Admin** and **Store Manager** users can create and search the product catalog.
- **Cashier** users can check the latest product price and start a sales transaction.
- The system supports user authentication and authorization, so users must log in before accessing features.
- Inventory management tracks stock in and stock out and supports inventory reporting.
- Reporting includes sales insights such as total revenue, cost of goods sold, and profit, as well as inventory insights such as on-hand stock and total remaining stock value to date.

## Current modules

JPOS currently includes these modules:

- **User module** for login, user management, and role-based access
- **Inventory module** for product catalog and stock tracking

The **sales module** is planned for the next release.

The project also includes unit tests for the currently implemented modules.

## Prerequisites

Install the following before running the project:

| Requirement | Version |
| --- | --- |
| Java Development Kit (JDK) | 17+ |
| Apache Maven | 3.9+ |

Confirm the tools are available:

```sh
java -version
mvn -version
```

## Project structure

```text
src/main/java   Application source code
src/test/java   JUnit test source code
pom.xml         Maven build configuration
```

## Getting started

Repository: `https://github.com/kim-0x/jpos.git`

```sh
git clone https://github.com/kim-0x/jpos.git
cd jpos
```

1. Clone the repository.
2. Open the project in your preferred Java IDE, or use the terminal.
3. Make sure Java 17 and Maven are installed.

## Run the application

From the project root:

```sh
mvn compile
java -cp target/classes Main
```

For the current mock setup, the default admin login is:

```sh
username: admin
password: admin
```

## Test setup

This project uses **Maven** and **JUnit 4** for unit testing. Test files are located under `src/test/java`.

## Run JUnit tests

Run all tests:

```sh
mvn test
```

Run a single test class:

```sh
mvn -Dtest=UserFacadeTest test
```

Current test coverage includes unit tests for:

- User module
- Inventory services
- Inventory repositories
