-- =============================================================================
-- Datos de prueba para la EFT - poblar la app con mascotas, pacientes y citas
-- Ejecutar UNA VEZ después de levantar el stack y de que JPA haya creado las
-- tablas (puedes verificar con: docker exec db-mysql-cdy2203-1-1 mysql -u root
-- -ppassword -e "USE mydatabase; SHOW TABLES;").
-- =============================================================================

USE mydatabase;

-- Pets disponibles para adopcion
INSERT INTO pet (id, name, species, breed, age, gender, location, status) VALUES
  (1, 'Lola',     'Perro', 'Mestizo',          3,  'Hembra', 'Santiago Centro',     'Disponible'),
  (2, 'Toby',     'Perro', 'Labrador',         5,  'Macho',  'Providencia',         'Disponible'),
  (3, 'Mishi',    'Gato',  'Siames',           2,  'Hembra', 'Nunoa',               'Disponible'),
  (4, 'Rocky',    'Perro', 'Pastor Aleman',    7,  'Macho',  'La Florida',          'Adoptado'),
  (5, 'Luna',     'Gato',  'Persa',            1,  'Hembra', 'Las Condes',          'Disponible'),
  (6, 'Max',      'Perro', 'Beagle',           4,  'Macho',  'Maipu',               'Disponible'),
  (7, 'Coco',     'Gato',  'Comun europeo',    6,  'Hembra', 'San Bernardo',        'En tratamiento'),
  (8, 'Bruno',    'Perro', 'Bulldog frances',  2,  'Macho',  'Vitacura',            'Disponible');

-- Fotos de las mascotas
INSERT INTO pet_photos (pet_id, photo_url) VALUES
  (1, 'https://placedog.net/400/300?id=1'),
  (1, 'https://placedog.net/400/300?id=11'),
  (2, 'https://placedog.net/400/300?id=2'),
  (3, 'https://placekitten.com/400/300?image=3'),
  (4, 'https://placedog.net/400/300?id=4'),
  (5, 'https://placekitten.com/400/300?image=5'),
  (6, 'https://placedog.net/400/300?id=6'),
  (7, 'https://placekitten.com/400/300?image=7'),
  (8, 'https://placedog.net/400/300?id=8');

-- Avanzar la secuencia para que el siguiente INSERT desde la app no choque
UPDATE pet_seq SET next_val = 100;

-- Pacientes (mascotas que ya tienen ficha clinica)
INSERT INTO patient (id, name, species, breed, age, owner, owner_email, owner_phone) VALUES
  (1, 'Firulais', 'Perro', 'Quiltro',     8,  'Maria Gonzalez', 'maria.g@example.cl', '+56912345678'),
  (2, 'Pelusa',   'Gato',  'Angora',      4,  'Pedro Soto',     'pedro.s@example.cl', '+56987654321'),
  (3, 'Roko',     'Perro', 'Golden',      6,  'Ana Diaz',       'ana.d@example.cl',   '+56956781234');

UPDATE patient_seq SET next_val = 100;

-- Citas veterinarias
INSERT INTO appointment (id, patient_id, date, time, reason, veterinarian) VALUES
  (1, 1, '2026-05-12', '10:00:00', 'Vacunacion anual',           'Dr. Garcia'),
  (2, 2, '2026-05-13', '11:30:00', 'Control rutinario',          'Dra. Martinez'),
  (3, 3, '2026-05-14', '15:00:00', 'Limpieza dental',            'Dr. Garcia'),
  (4, 1, '2026-05-20', '09:00:00', 'Revision post-vacunacion',   'Dra. Martinez');

UPDATE appointment_seq SET next_val = 100;