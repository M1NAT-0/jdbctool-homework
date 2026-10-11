CREATE TABLE IF NOT EXISTS college (
    id   VARCHAR(32) PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS student (
    id          VARCHAR(32) PRIMARY KEY,
    name        VARCHAR(64) NOT NULL,
    major       VARCHAR(64),
    age         INT,
    enroll_date DATE,
    graduated   TINYINT(1),
    tuition     DECIMAL(10, 2)
);
