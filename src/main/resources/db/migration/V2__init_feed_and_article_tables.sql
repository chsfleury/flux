DROP TABLE IF EXISTS fluxdb.feed;
CREATE TABLE fluxdb.feed (
  url VARCHAR(1000) NOT NULL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  selector varchar(1000) NULL,
  prefix varchar(1000) NULL,
  suffix varchar(1000) NULL,
  next_scan timestamp NULL
);

CREATE INDEX next_scan_idx ON fluxdb.feed(next_scan);

DROP TABLE IF EXISTS fluxdb.article;
CREATE TABLE fluxdb.article (
  url VARCHAR(1000) NOT NULL PRIMARY KEY,
  flux_url VARCHAR(1000) NOT NULL,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  author VARCHAR(100),
  tags VARCHAR(255),
  CONSTRAINT fk_flux_url FOREIGN KEY (flux_url) REFERENCES fluxdb.feed(url)
);