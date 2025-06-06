CREATE TABLE students
(
    id           INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(100) NOT NULL,
    age          INT          NOT NULL,
    grade        INT          NOT NULL,
    updated_date DATE DEFAULT CURRENT_DATE
);