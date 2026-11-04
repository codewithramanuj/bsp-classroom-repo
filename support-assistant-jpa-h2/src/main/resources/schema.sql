-- H2 only. Runs at every start, on an empty in-memory database.
-- The names are the same as the Oracle tables of the PL/SQL projects.

CREATE SEQUENCE SUPPORT_TICKETS_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE SUPPORT_TICKETS (
    ID           BIGINT         PRIMARY KEY,
    TITLE        VARCHAR(100)   NOT NULL,
    DESCRIPTION  VARCHAR(500),
    STATUS       VARCHAR(20)    NOT NULL,
    PRIORITY     VARCHAR(10)    NOT NULL,
    CREATED_AT   TIMESTAMP      NOT NULL,
    DUE_BY       TIMESTAMP      NOT NULL
);
