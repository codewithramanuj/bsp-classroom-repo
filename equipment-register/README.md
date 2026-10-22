# Equipment Register: Starter Project

This is the starter project for the [Plant Equipment Register case study](../practice-case-study.md). The project setup (Step 1 of the case study) is already done for you. Start from **Step 2**.

## What you implement

Every class and method signature is in place. You fill in the `TODO` bodies:

| File | TODO |
|---|---|
| `src/main/resources/db/equipment.sql` | `EQUIPMENT` table, sample rows, and each procedure body in `EQUIPMENT_PKG` |
| `repository/EquipmentRepository` | `SimpleJdbcCall` fields and setup, all five calls, and the row mapper |
| `service/EquipmentService` | All five methods |
| `api/EquipmentController` | All five endpoints |

The DTOs (`EquipmentRequest`, `EquipmentResponse`) are complete. Find every task with your IDE's TODO view, or run `grep -rn TODO src`.

## Run it

1. Check the database settings in `src/main/resources/application.yaml`.
2. Complete and run `src/main/resources/db/equipment.sql` in SQL Developer.
3. Start the app: run `EquipmentRegisterApplication` from the IDE, or use `mvn spring-boot:run`.
4. Open <http://localhost:8080/swagger-ui.html>.
