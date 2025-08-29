CREATE TABLE loan_type
(
    id                   UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    name                 VARCHAR(100) UNIQUE NOT NULL,
    minimum_amount       NUMERIC(15, 2)      NOT NULL,
    maximum_amount       NUMERIC(15, 2)      NOT NULL,
    interest_rate        NUMERIC(5, 2)       NOT NULL,
    automatic_validation BOOLEAN             NOT NULL DEFAULT FALSE
);

CREATE TABLE loan_status
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE loan_application
(
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount                NUMERIC(15, 2) NOT NULL,
    term                  VARCHAR(50)    NOT NULL,
    identification_number VARCHAR(20)    NOT NULL,
    email                 VARCHAR(150)   NOT NULL,
    type_id               UUID           NOT NULL,
    status_id             UUID           NOT NULL
);

INSERT INTO loan_type (name, minimum_amount, maximum_amount, interest_rate, automatic_validation)
VALUES ('PERSONAL LOAN', 500000, 100000000, 18.50, TRUE),
       ('MORTGAGE LOAN', 30000000, 1000000000, 12.00, FALSE),
       ('AUTO LOAN', 10000000, 200000000, 14.00, TRUE),
       ('PERSONAL UNSECURED LOAN', 1000000, 80000000, 20.00, TRUE),
       ('EDUCATION LOAN', 500000, 50000000, 10.50, TRUE),
       ('MICROCREDIT', 300000, 50000000, 25.00, TRUE);

INSERT INTO loan_status (name, description)
VALUES ('UNDER REVIEW', 'Application received, under evaluation'),
       ('APPROVED', 'Loan application approved'),
       ('REJECTED', 'Loan application rejected'),
       ('CANCELLED', 'Application cancelled by the client or the system');

ALTER TABLE loan_application
    ADD CONSTRAINT fk_application_type
        FOREIGN KEY (type_id) REFERENCES loan_type (id);

ALTER TABLE loan_application
    ADD CONSTRAINT fk_application_status
        FOREIGN KEY (status_id) REFERENCES loan_status (id);


