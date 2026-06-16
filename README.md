# JPOS

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/kim-0x/jpos)

JPOS is a point of sale system for small grocery stores. It helps store owners manage products, inventory, pricing, sales activity, and reporting from a single Java application.

The latest implementation supports multiple repository backends (`CSV`, `BIN`, and `JDBC/SQLite`), with `JDBC/SQLite` configured as the default for the main app.

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
- **Sale module** for processing sale transactions and displaying transaction summaries
- **Report module** for exporting and viewing sales and inventory reports from `data/report`

The project includes comprehensive unit tests for all implemented modules.

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
data/           Application data root
data/bin/       Binary repository files (`*.dat`)
data/csv/       CSV seed/reference data
data/db/        SQLite database files (default: `jpos.db`)
data/report/    Exported report output and generated report assets
pom.xml         Maven build configuration
```

## Getting started

Repository: `https://github.com/kim-0x/jpos.git`

```sh
git clone https://github.com/kim-0x/jpos.git
cd jpos
```

1. Clone the repository.
2. Open the project in VS Code.
3. Make sure Java 17 and Maven are installed.

Recommended extension:

- Extension Pack for Java (Microsoft)

## Repository implementations

JPOS supports three repository implementations:

- `JDBC` (SQLite via `sqlite-jdbc`) - default implementation for the main app
- `BIN` (binary `*.dat` files under `data/bin`)
- `CSV` (CSV files under `data/csv`)

Default database location for JDBC mode:

```text
data/db/jpos.db
```

## Run the main application (default: JDBC/SQLite)

Recommended for contributors: run from VS Code using the launch configuration.

1. Open **Run and Debug** in VS Code.
2. Select **Main**.
3. Click **Start Debugging** (or press `F5`).

Optional terminal run (dependency-aware):

```sh
mvn compile
mvn "-Dexec.mainClass=Main" org.codehaus.mojo:exec-maven-plugin:3.3.0:java
```

Do not use `java -cp target/classes Main` for this project, because it excludes Maven dependencies such as `sqlite-jdbc`.

Default admin login credentials:

```sh
username: admin
password: admin
```

Note: Use lowercase credentials (`admin` / `admin`) for local runs.

## Migration app: BIN to SQLite

Use the migration app to copy data from binary repositories (`data/bin/*.dat`) into SQLite (`data/db/jpos.db`).

Recommended: run from VS Code **Run and Debug** with configuration **BinToSqliteMigration**.

Incremental migration (safe to rerun; uses `INSERT OR IGNORE`):

```sh
mvn compile
mvn "-Dexec.mainClass=com.jpos.migration.BinToSqliteMigration" org.codehaus.mojo:exec-maven-plugin:3.3.0:java
```

Reset SQLite tables, then migrate:

```sh
mvn "-Dexec.mainClass=com.jpos.migration.BinToSqliteMigration" "-Dexec.args=--reset" org.codehaus.mojo:exec-maven-plugin:3.3.0:java
```

## Concurrent cashier simulation app

The simulation app stress-tests sale transaction processing with multiple cashier threads.

Recommended: run from VS Code **Run and Debug** with one of:

- `ConcurrentCashierSimulation (JDBC)`
- `ConcurrentCashierSimulation (Bin)`
- `ConcurrentCashierSimulation (Csv)`

Examples:

```sh
mvn compile
mvn "-Dexec.mainClass=com.jpos.simulation.ConcurrentCashierSimulation" org.codehaus.mojo:exec-maven-plugin:3.3.0:java
mvn "-Dexec.mainClass=com.jpos.simulation.ConcurrentCashierSimulation" "-Dexec.args=--datasource=jdbc --cashiers=10 --transactions=100 --admin-username=admin --admin-password=admin" org.codehaus.mojo:exec-maven-plugin:3.3.0:java
mvn "-Dexec.mainClass=com.jpos.simulation.ConcurrentCashierSimulation" "-Dexec.args=--datasource=bin --cashiers=10 --transactions=100 --admin-username=admin --admin-password=admin" org.codehaus.mojo:exec-maven-plugin:3.3.0:java
mvn "-Dexec.mainClass=com.jpos.simulation.ConcurrentCashierSimulation" "-Dexec.args=--datasource=csv --cashiers=10 --transactions=100 --admin-username=admin --admin-password=admin" org.codehaus.mojo:exec-maven-plugin:3.3.0:java
```

Available options:

```text
--datasource=csv|bin|jdbc   default: jdbc
--cashiers=N                default: 5
--transactions=N            default: 3
--margin=0.25               default: 0.25
--admin-username=admin      default: admin
--admin-password=admin      default: admin
--stock=500                 default: 500
--maxRetries=10             default: 10 (JDBC mode)
--retryBackoffMs=50         default: 50 (JDBC mode)
```

Behavior notes:

- In `JDBC` mode, SQLite WAL mode is enabled to improve concurrent read/write behavior.
- In `CSV` and `BIN` mode, writes are serialized because file-based repositories are not thread-safe.

## Generate seed data for BIN repositories

You can generate high-volume historical sale transactions (for example, 500-1000 transactions/day over 3 or 6 months) directly into `.dat` files:

```sh
mvn compile
java -cp target/classes com.jpos.seed.SeedDataGenerator --months=3 --minTxPerDay=500 --maxTxPerDay=1000 --reset
```

For a larger period (6 months):

```sh
java -cp target/classes com.jpos.seed.SeedDataGenerator --months=6 --minTxPerDay=500 --maxTxPerDay=1000 --reset
```

Export generated BIN records to CSV (Excel-compatible):

```sh
java -cp target/classes com.jpos.seed.SeedDataGenerator \
  --months=3 --minTxPerDay=500 --maxTxPerDay=1000 --reset \
  --exportCsvDir=data/export
```

Notes:

- The generator creates products (if needed), sets prices, and writes transactions into BIN (`.dat`) repositories.
- The generator reuses the predefined product catalog (`data/csv/product.csv`) when BIN product data is empty instead of generating random products.
- Product prices are refreshed once per month during generation.
- Sale item generation uses quantity `1` per barcode scan event.
- It performs automatic low-stock restock and bulk monthly restock (at the start of each month) to sustain large transaction volumes.
- It logs start/end time and processing duration for each day to help track daily transaction generation time.
- If daily generation exceeds the safeguard timeout (default `120` seconds), it asks whether to continue. Override with `--dayTimeoutSeconds=<seconds>`.
- Use `--append` to add more generated data without clearing existing BIN sales/inventory data.
- Use `--exportCsvDir=<path>` to export BIN records (`product`, `inventory`, `pricebook`, `saletransaction`, `saleitem`) as CSV files for verification in spreadsheet tools.

When using BIN repositories, data is stored in:

```text
data/bin/user.dat
data/bin/product.dat
data/bin/inventory.dat
data/bin/pricebook.dat
data/bin/saleitem.dat
data/bin/saletransaction.dat
```

When using JDBC repositories (default main app), runtime data is stored in:

```text
data/db/jpos.db
```

## VS Code launch configurations

This repository includes predefined Java launch configurations in `.vscode/launch.json` for the new apps:

- `Main`
- `BinToSqliteMigration`
- `BinToSqliteMigration (reset)`
- `ConcurrentCashierSimulation (JDBC)`
- `ConcurrentCashierSimulation (Bin)`
- `ConcurrentCashierSimulation (Csv)`

To run any of them in VS Code:

1. Open **Run and Debug**.
2. Select one of the configurations above.
3. Click **Start Debugging**.

Reports are exported to and viewed from:

```text
data/report/
```

To serve and view the generated report in your browser:

```sh
cd data/report
java -m jdk.httpserver
```

Then open:

```text
http://localhost:8000/
```

## Test setup

This project uses **Maven** and **JUnit 4** for unit testing. Test files are located under `src/test/java`.

## Run JUnit tests

Run all tests:

```sh
mvn test
```

## Generate JaCoCo coverage report

Run the Maven verify phase to execute tests and produce the HTML coverage report:

```sh
mvn verify
```

After the build completes, open the report in your browser:

```text
target/site/jacoco/index.html
```

Run a single test class:

```sh
mvn -Dtest=UserFacadeTest test
```

Current test coverage includes unit tests for:

- User module
- Inventory services and repositories
- Sale module (facade, models, and repositories)

## Contributing

To contribute to JPOS, follow these guidelines:

### Code Organization

The project follows a modular architecture with the following structure:

```text
src/main/java/
├── Main.java                # Entry point; bootstraps all components (views, facades, services, repositories)
├── view/                    # User interface layer; handles input/output and coordinates facades
│   ├── AppMenu.java         # Main menu interface
│   ├── UserFeature.java     # User login and management interface
│   ├── ProductFeature.java  # Product catalog interface
│   ├── InventoryFeature.java # Inventory management interface
│   ├── SaleFeature.java     # Sale transaction interface
│   ├── ReportFeature.java   # Report export and viewing interface
│   └── implementation/      # View implementations
└── com/jpos/
    ├── user/                # User authentication and role management
    │   ├── model/
    │   ├── repository/
    │   ├── service/
    │   └── facade/
    ├── inventory/           # Product catalog and stock tracking
    │   ├── model/
    │   ├── repository/
    │   └── service/
    ├── report/              # Report generation, export, and viewing
    │   ├── model/
    │   ├── service/
    │   └── facade/
    └── sale/                # Sale transactions and pricing
        ├── model/
        ├── repository/
        ├── service/
        └── facade/
```

#### Architecture Pattern

- **Business Logic Modules** (`user/`, `inventory/`, `sale/`, `report/`): Each follows the pattern `model` → `repository` and/or `service` → `facade`, ensuring separation of concerns.
- **View Layer** (`view/`): Handles all user interaction (input/output) and coordinates between multiple facade objects.
- **Main Class**: Bootstraps the entire application by instantiating all repositories, services, facades, and views, then starts the application session.

#### Component Responsibilities

- **Models**: Data structures representing business entities
- **Repositories**: Persist and retrieve runtime data from binary files and seed/reference data from CSV where needed
- **Services**: Business logic and operations on models
- **Facades**: Simplified interfaces for views to interact with multiple services
- **Views**: Handle user interaction, display information, and coordinate facade calls
- **Main**: Application entry point and component initialization

### Development Workflow

1. **Create a feature branch** from `main` for your changes
2. **Implement your feature** following the architecture pattern:
   - Start with the **model** (data structure)
   - Create the **repository** (data persistence)
   - Implement the **service** (business logic)
   - Add the **facade** (simplified interface for views)
   - Create the **view** (user interaction)
3. **Write unit tests** in `src/test/java` for all modules
4. **Run tests locally** to ensure all tests pass:
   ```sh
   mvn test
   ```
5. **Check code coverage** to maintain high test coverage:
   ```sh
   mvn verify
   ```
6. **Update the Main class** if bootstrapping changes are needed for new components
7. **Submit a pull request** with a clear description of your changes

### Data Management

The application uses:

- `data/db/` for JDBC/SQLite runtime data (default)
- `data/bin/` for BIN runtime data
- `data/csv/` for seed/reference data
- `data/report/` for exported report output

Ensure:

- SQLite schema and file paths remain consistent with repository code
- Binary and CSV files follow the existing naming convention
- Seed data matches the corresponding model classes
- Generated reports remain under `data/report/` so they can be viewed after export

### Best Practices

- Follow Java naming conventions (camelCase for methods/variables, PascalCase for classes)
- Write unit tests for all new functionality
- Keep test coverage above 70%
- Use meaningful commit messages
- Document any new public APIs or significant changes
