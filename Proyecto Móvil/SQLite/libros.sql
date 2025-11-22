BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "libros" (
	"id"	TEXT,
	"titulo"	TEXT,
	"autor"	TEXT,
	"fecha_publicacion"	TEXT,
	"ruta_imagen"	TEXT,
	"ruta_pdf"	TEXT,
	"descripcion"	TEXT,
	"fecha_modificacion"	TEXT,
	PRIMARY KEY("id")
);
INSERT INTO "libros" VALUES ('UUUUUUUUUUUUUUUU','Drácula','Bram Stoker','1897-01-01','https://cdn.kobo.com/book-images/88a05cf1-a3b6-461...','https://www.suneo.mx/literatura/subidas/Bram%20Stt...','Historia de vampiros. Adaptaciones cinematográficas.','2025-06-04 00:00:00');
INSERT INTO "libros" VALUES ('DDDDDDDDDDDDDDDD','La metamorfosis','Franz Kafka','1915-01-01','https://elchicodelasdonasblog.wordpress.com/wp-con...','https://www.cch.unam.mx/sites/default/files/La_met...','Novela corta, Absurdo, Literatura fantástica. Ciudadanía y existencia.','2025-06-04 00:00:00');
INSERT INTO "libros" VALUES ('3333333333333333','Orgullo y prejuicio','Jane Austen','1813-01-28','https://imagenes.elpais.com/resizer/v2/HXWSKKPIJJL...','https://web.seducoahuila.gob.mx/biblioweb/upload/o...','Novela romántica y comedia. Géneros: Ficción, Novela social.','2025-06-04 00:00:00');
INSERT INTO "libros" VALUES ('1984','1984','George Orwell','1949-06-08','https://marinacasado.com/wp-content/uploads/2016/0...','https://www.philosophia.cl/biblioteca/orwell/1984....','Novela política de ficción distópica. Géneros: Ciencia ficción.','2025-06-04 00:00:00');
INSERT INTO "libros" VALUES ('0x11111111111111111111111111111111','De la Tierra a la Luna','Julio Verne','1865-01-01','https://e00-lab-elmundo.uecdn.es/hombre-en-la-luna...','https://bibliotecadigital.ilce.edu.mx/Colecciones/...','Géneros: Ciencia ficción, Ficción de aventuras, Novela clásica.','2025-06-04 00:00:00');
COMMIT;
