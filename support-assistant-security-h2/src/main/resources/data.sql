-- H2 only. Runs at every start, after schema.sql.
-- Two sample users for the classroom. The hashes are BCrypt hashes of meera123 and karan123.
INSERT INTO DESK_USERS (ID, USERNAME, PASSWORD_HASH, USER_ROLE)
VALUES (1, 'meera', '$2a$10$Bfg2JesPXqB6feDQOsdiPO2pUnpbd46gedZT8XF4YqM0CYvNgdnue', 'EMPLOYEE');

INSERT INTO DESK_USERS (ID, USERNAME, PASSWORD_HASH, USER_ROLE)
VALUES (2, 'karan', '$2a$10$T4q6jGv1.XdnsZA6Jw/hqOGQ3vICbWFyD0iNU8C/BEMhHXisegpna', 'AGENT');
