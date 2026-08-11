-- V5_seed_layouts_and_buses.sql
-- Seed two example layouts and a bus

INSERT INTO seat_layouts (id, name, description, format, content, meta) VALUES
  ('11111111-1111-1111-1111-111111111111', '2+2 Standard', 'Standard 2+2 seater layout', 'json', '{"seats": [
    {"seatCode":"A1","row":1,"col":1,"price":120},
    {"seatCode":"A2","row":1,"col":2,"price":120},
    {"seatCode":"B1","row":2,"col":1,"price":120},
    {"seatCode":"B2","row":2,"col":2,"price":120}
  ]}', '{"rows":2,"cols":2}'),
  ('22222222-2222-2222-2222-222222222222', '2+1 Sleeper', '2+1 sleeper compact', 'json', '{"seats": [
    {"seatCode":"A1","row":1,"col":1,"price":200},
    {"seatCode":"A2","row":1,"col":2,"price":200},
    {"seatCode":"B1","row":2,"col":1,"price":200}
  ]}', '{"rows":2,"cols":2}');

INSERT INTO buses (id, operator_id, model, registration_number, seat_layout_id, total_seats) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', null, 'Volvo 9400', 'KA-01-AA-0001', '11111111-1111-1111-1111-111111111111', 4),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', null, 'Mini Sleeper', 'KA-01-BB-0002', '22222222-2222-2222-2222-222222222222', 3);
