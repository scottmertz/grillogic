# GRILLOGIC
### Recipe Costing & Operational Intelligence for Independent F&B Operators

---

## Overview

GRILLOGIC is a full-stack web application built to give independent restaurant and food truck operators clear, accurate visibility into their recipe costs, labor impact, and menu profitability.

Most independent operators are flying blind on their margins. Spreadsheets are hard to maintain during a busy service, and enterprise solutions are priced for chains. GRILLOGIC was built by a Head Chef with nearly 20 years of kitchen experience to solve the problem from the inside.

---

## Features

### Ingredient Management
- Create and manage ingredients with vendor, unit, and cost details
- Automatic cost-per-unit calculations
- Line-item cost rollups inside recipes

### Recipe Builder
- Add ingredients with quantities and units
- Automatic line-cost calculation per ingredient
- Batch cost and portion cost generation
- Integrity checks to maintain consistent data

### Cost Summary (per recipe)
- Total Cost
- Portion Cost
- Menu Price
- Food Cost %
- Labor %
- Prime Cost %
- Labor Cost
- Profit After Labor
- Suggested Menu Price (based on target food cost %)

### Prime Cost Modeling
Calculated using industry-standard formulas:

```
Prime Cost % = Food Cost % + Labor %
Labor Cost = Menu Price × (Labor % / 100)
Profit After Labor = Menu Price – (Portion Cost + Labor Cost)
```

### Color-Coded Metrics
Key metrics are color-coded for at-a-glance interpretation — operators know immediately whether a dish is healthy, marginal, or losing money.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| Security | Spring Security + JWT (JJWT) |
| ORM | Spring Data JPA / Hibernate |
| Templates | Thymeleaf |
| Database | PostgreSQL |
| Build Tool | Maven |
| IDE | IntelliJ IDEA Community |
| Utilities | Lombok |

---

## Architecture

### Core Entities
- `Ingredient` — stores cost, unit, vendor, and category data
- `Recipe` — top-level recipe with serving size, labor, and target food cost
- `RecipeIngredient` — join entity linking recipes to ingredients with quantities and units
- `User` — authenticated user with BCrypt-hashed password and JWT-based session

### Key Services
- **UnitConverter** — handles unit conversions across the ingredient management system
- **CostingService** — calculates per-line cost, total cost, cost per serving, food cost %, and yield-loss adjustments
- **JwtAuthFilter** — validates JWT tokens on every protected request
- **GlobalExceptionHandler** — returns clean HTTP status codes across the entire application

### Security
- BCrypt password hashing
- JWT generation and validation
- Spring Security access rules per route
- Role-based access control (in progress)

---

## Roadmap

The following features are planned or in active development:

- [ ] Frontend v1 (Thymeleaf) — Login, ingredient entry, recipe builder with live cost recalc
- [ ] Ownership Model — Link recipes and ingredients to individual client users
- [ ] Client Dashboard — Subscriber-facing view of recipes, costs, and profitability data
- [ ] Sub-Recipes as Ingredients — Nested recipe costing for complex dishes
- [ ] Cooking Method Yield ("Pit Boss Pay") — Yield-adjusted costing by cooking method
- [ ] In-App PDF Report Generation — Automated audit reports and recovery plans
- [ ] Printable Recipe Cards — Kitchen-ready recipe format (no pricing shown)
- [ ] Recipe Versioning — Track cost changes over time (vendor swaps, portion changes)
- [ ] Admin Panel + Stripe + Email — Multi-client business view, subscriptions, transactional email
- [ ] Multi-User Access Per Client Account — Owner + manager logins under one account
- [ ] Vendor Price Comparison — Track multiple vendor prices per ingredient, flag cost changes
- [ ] Real-Time Profit Dashboard — Combined food cost + live sales data view
- [ ] POS Integration — SkyTab (Shift4), Square, Toast, Clover, Lightspeed, TouchBistro
- [ ] Menu Engineering Matrix — Stars / Plowhorses / Puzzles / Dogs classification
- [ ] AI Features — Recipe suggestions, prep list generation, vendor negotiation insights
- [ ] OCR Invoice Capture — Photograph an invoice or receipt → auto-extract and enter into system

---

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL
- Maven

### Setup

1. Clone the repository
```bash
git clone https://github.com/scottmertz/grillogic.git
cd grillogic
```

2. Configure your database connection in `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/grillogic
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

3. Build and run
```bash
mvn spring-boot:run
```

---

## Design Principles

**Chef-Driven**
Every feature is built around real kitchen workflows. Clarity, speed, and accuracy are non-negotiable.

**Operator-Focused**
Color coding, summary metrics, and clean layout support quick decision-making during a busy service.

**Extensible**
The architecture supports future enhancements including multi-user operations, POS integrations, and AI-powered insights without requiring a rebuild.

---

## About the Developer

GRILLOGIC was built by Scott Mertz — a Head Chef with nearly 20 years of professional kitchen experience and a software developer completing an AAS in Information Systems Technology at Virginia Western Community College.

The platform was born out of a real operational problem: spreadsheets are impossible to maintain during a busy service, and independent operators have no affordable tool built for how kitchens actually work. GRILLOGIC is that tool.

- LinkedIn: [linkedin.com/in/getgrillogic](https://linkedin.com/in/getgrillogic)
- GitHub: [github.com/scottmertz](https://github.com/scottmertz)
- Email: smertz.dev@gmail.com

---

## License

This project is proprietary and not licensed for public distribution.
