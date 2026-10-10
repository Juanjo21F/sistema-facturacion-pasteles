-- Datos iniciales: categorías (el enunciado no pide CRUD de categorías, por eso se siembran aquí).
INSERT INTO categorias (nombre, descripcion)
SELECT 'Tortas', 'Tortas y pasteles grandes' WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = 'Tortas');
INSERT INTO categorias (nombre, descripcion)
SELECT 'Cupcakes', 'Pasteles individuales' WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = 'Cupcakes');
INSERT INTO categorias (nombre, descripcion)
SELECT 'Galletas', 'Galletas y bizcochos' WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = 'Galletas');
INSERT INTO categorias (nombre, descripcion)
SELECT 'Postres', 'Postres fríos y porciones' WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = 'Postres');
