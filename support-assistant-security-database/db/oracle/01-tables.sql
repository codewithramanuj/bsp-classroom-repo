-- Oracle only. Run once, as the user that your application connects as.
-- The names start with DESK_ so they do not clash with the SUPPORT_ tables of the other projects.

CREATE SEQUENCE DESK_USERS_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE DESK_USERS (
    ID             NUMBER        PRIMARY KEY,
    USERNAME       VARCHAR2(50)  NOT NULL,
    -- A BCrypt hash is always 60 characters. The password itself is never stored.
    PASSWORD_HASH  VARCHAR2(100) NOT NULL,
    -- No ROLE_ prefix here: Spring Security adds it.
    USER_ROLE      VARCHAR2(20)  NOT NULL,
    CONSTRAINT DESK_USERS_NAME_UK UNIQUE (USERNAME),
    CONSTRAINT DESK_USERS_ROLE_CK CHECK (USER_ROLE IN ('EMPLOYEE', 'AGENT'))
);
