INSERT INTO user (id, created, last_modified, version, email, email_validated, email_verification_code, mobile_number, mobile_verification_code, password, password_reset_code, username)
VALUES
(0, '2019-04-04 00:00:00', '2019-04-04 00:00:00', 1, 'amrit_malla@ymail.com', 0, 'afc8', '9843243174', 'g5788', 'password', NULL, 'amritmalla'),
(1, '2019-04-04 00:00:00', '2019-04-04 00:00:00', 1, 'amrit_malla@ymail.com', 1, 'afc8', '9843243174', 'g5788', 'password', NULL, 'amrit'),
(2, '2019-04-04 00:00:00', '2019-04-04 00:00:00', 0, 'amritmalla09@gmail.com', 1, 'afc8', '9843243174', 'g5788', 'password', NULL, 'mallaamrit');


INSERT INTO vendor (id, created, last_modified, version, addressline1, addressLine2, city, first_name, last_name, middle_name, phone_number, user_id) VALUES
(0, '2019-04-11 07:23:18', '2019-04-09 13:35:29', 1, 'tokha road', 'kharibot', 'kathmandu', 'amrit', 'malla', NULL, '9846346396', 1),
(1, '2019-04-10 08:24:26', '2019-04-11 05:12:14', 1, 'tokha road', 'kharibot', 'kathmandu', 'amrit', 'malla', NULL, '9846346396', 1),
(2, '2019-04-11 05:12:15', '2019-04-09 08:23:47', 1, 'tokha road', 'kharibot', 'Narayangarh', 'shial', 'maskey', NULL, '9846346396', 2);


insert into futsal(created, last_modified, version,city,country,futsal_name,latitude,longitude,rating,street_address,vendor_id)
values('2019-04-11 07:23:18', '2019-04-09 13:35:29', 1,'kathmandu','nepal','hamro futsal', 81.78,45.0007, 4.5, 'samakhusi, tokha road', 1);