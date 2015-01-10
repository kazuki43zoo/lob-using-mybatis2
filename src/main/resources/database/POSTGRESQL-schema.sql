DROP TABLE IF EXISTS large_file;

CREATE TABLE large_file (
  id CHAR(36),
  file_name TEXT,
  content OID,
  CONSTRAINT pk_large_file PRIMARY KEY (id)
);

COMMIT;