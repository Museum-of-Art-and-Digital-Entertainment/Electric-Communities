-- oracle table loader sql script
-- create the tables for the user db tester.

set termout on
prompt Building tables
-- set termout off
-- set feedback off

ALTER SESSION SET NLS_LANGUAGE = AMERICAN;
ALTER SESSION SET NLS_TERRITORY = AMERICA;

DROP TABLE user_id_tbl;

DROP SEQUENCE user_id_seq;
CREATE SEQUENCE user_id_seq INCREMENT BY 1 START WITH 1;

CREATE TABLE user_id_tbl
	( user_id NUMBER(7) CONSTRAINT user_id_key PRIMARY KEY,
	  email_addr char (40),
	  first_name char (15),
	  middle_iniital char (2),
	  last_name char (15),
	  acct_status NUMBER (4),
	  street_addr1 char (80),
	  street_addr2 char (80),
	  city char (80),
	  state char (80),
	  zip_code char (10),
	  country char (32),
	  paid_up_till DATE,
	  avatar1_id NUMBER (4),
	  avatar2_id NUMBER (4));

INSERT INTO user_id_tbl VALUES
        (user_id_seq.nextval,
	'jeff@communities.com',
	'Jeff',
	'L.',
	'Crilly',
	'1',
	'473 Gary Ct.',
	'',
	'Palo Alto',
	'CA',
	'94306',
	'U.S.A.',
	'13-JUN-97',
	1234,
	3454);


COMMIT;

EXIT;
