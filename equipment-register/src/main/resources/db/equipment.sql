-- Plant Equipment Register: complete the TODOs, then run this whole script in SQL Developer (F5).

-- TODO: create table EQUIPMENT
--   ID          NUMBER, identity primary key
--   NAME        VARCHAR2(100), not null
--   DEPARTMENT  VARCHAR2(50), not null
--   STATUS      VARCHAR2(20), default 'WORKING', only 'WORKING' or 'UNDER_REPAIR'

-- TODO: insert 2-3 sample rows and COMMIT


CREATE OR REPLACE PACKAGE EQUIPMENT_PKG AS

    PROCEDURE ADD_EQUIPMENT(
        P_NAME       IN  VARCHAR2,
        P_DEPARTMENT IN  VARCHAR2,
        P_STATUS     IN  VARCHAR2,
        P_ID         OUT NUMBER);

    PROCEDURE GET_ALL_EQUIPMENT(
        P_RESULT OUT SYS_REFCURSOR);

    PROCEDURE GET_EQUIPMENT(
        P_ID     IN  NUMBER,
        P_RESULT OUT SYS_REFCURSOR);

    PROCEDURE UPDATE_EQUIPMENT(
        P_ID         IN NUMBER,
        P_NAME       IN VARCHAR2,
        P_DEPARTMENT IN VARCHAR2,
        P_STATUS     IN VARCHAR2);

    PROCEDURE DELETE_EQUIPMENT(
        P_ID IN NUMBER);

END EQUIPMENT_PKG;
/

CREATE OR REPLACE PACKAGE BODY EQUIPMENT_PKG AS

    PROCEDURE ADD_EQUIPMENT(
        P_NAME       IN  VARCHAR2,
        P_DEPARTMENT IN  VARCHAR2,
        P_STATUS     IN  VARCHAR2,
        P_ID         OUT NUMBER) IS
    BEGIN
        NULL; -- TODO: insert the row and return the new ID into P_ID (RETURNING ... INTO)
    END ADD_EQUIPMENT;

    PROCEDURE GET_ALL_EQUIPMENT(
        P_RESULT OUT SYS_REFCURSOR) IS
    BEGIN
        NULL; -- TODO: OPEN P_RESULT FOR a SELECT of all rows ordered by ID
    END GET_ALL_EQUIPMENT;

    PROCEDURE GET_EQUIPMENT(
        P_ID     IN  NUMBER,
        P_RESULT OUT SYS_REFCURSOR) IS
    BEGIN
        NULL; -- TODO: OPEN P_RESULT FOR a SELECT of the row with this ID
    END GET_EQUIPMENT;

    PROCEDURE UPDATE_EQUIPMENT(
        P_ID         IN NUMBER,
        P_NAME       IN VARCHAR2,
        P_DEPARTMENT IN VARCHAR2,
        P_STATUS     IN VARCHAR2) IS
    BEGIN
        NULL; -- TODO: update NAME, DEPARTMENT, STATUS for this ID
    END UPDATE_EQUIPMENT;

    PROCEDURE DELETE_EQUIPMENT(
        P_ID IN NUMBER) IS
    BEGIN
        NULL; -- TODO: delete the row with this ID
    END DELETE_EQUIPMENT;

END EQUIPMENT_PKG;
/
