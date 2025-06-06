CREATE TABLE favorites
(
    student_id INT          NOT NULL,
    fav_key    VARCHAR(100) NOT NULL,
    fav_val    VARCHAR(100) NOT NULL,
    PRIMARY KEY (student_id, fav_key),
    FOREIGN KEY (student_id) REFERENCES students (id)
);