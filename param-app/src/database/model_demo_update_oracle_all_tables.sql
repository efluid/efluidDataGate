INSERT INTO "TTABLEOTHERTEST2"("VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES ('SomethingAAA1', 'testAAAA', 'test1AAAA', 14.5, '02-01-2018 23:42:32');
INSERT INTO "TTABLEOTHERTEST2"("VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES ('SomethingAAA2', 'testAAAAE', 'test1AAAA', 14.5, '02-01-2018 23:42:32');
INSERT INTO "TTABLEOTHERTEST2"("VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES ('SomethingAAA3', 'testAAAAF', 'test1AAAA', 14.5, '02-01-2018 23:42:32');
INSERT INTO "TTABLEOTHERTEST2"("VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES ('SomethingAAA3', 'testAAAAJ', 'test1AAAA', 14.5, '02-01-2018 23:42:32');
                                                                                                     
INSERT INTO "TCATEGORYMATERIEL"("NOM", "DETAIL") VALUES ('Machin', 'Type de machin');
INSERT INTO "TCATEGORYMATERIEL"("NOM", "DETAIL") VALUES ('Truc', 'Gros truc');

INSERT INTO "TTYPEMATERIEL"("TYPE", "SERIE", "CATID") VALUES ('Matériel ABCD1', 'L0098111', 3);
INSERT INTO "TTYPEMATERIEL"("TYPE", "SERIE", "CATID") VALUES ('Matériel ABCD2', 'SCH09222', 3);

INSERT INTO "TTABLEOTHER"("VALUE", "COUNT", "WHEN", "TYPEID") VALUES ('Exe_111', 45, '02-01-2018 23:42:32', 1);
INSERT INTO "TTABLEOTHER"("VALUE", "COUNT", "WHEN", "TYPEID") VALUES ('Exe_222', 33, '02-01-2018 23:42:32', 1);

INSERT INTO "TMODELE"("CODE_SERIE", "CREATE_DATE", "FABRICANT", "DESCRIPTION", "BINARY_VALUE", "TYPEID", "ACTIF") VALUES ('ZZZZ1', '02-01-2018 23:42:32', 'DUPON', 'Un modèle de test - UPD', '0123456789ABCDEF0123456789ABCDEF1', 1, 1);
INSERT INTO "TMODELE"("CODE_SERIE", "CREATE_DATE", "FABRICANT", "DESCRIPTION", "BINARY_VALUE", "TYPEID", "ACTIF") VALUES ('ZZZZ1', '02-01-2018 23:42:32', 'DUPON', 'Un modèle de test - UPD', '0123456789ABCDEF0123456789ABCDEF1', 1, 1);

INSERT INTO "ADVANCED_COLS" VALUES ('UU00011', 'Advanced UU 1', 3, 2, 'This is a detail value', 1, '0123456789ABCDEF0123456789ABCDEF1', '02-01-2018 11:42:34', '02-05-2018 23:42:34');
INSERT INTO "ADVANCED_COLS" VALUES ('UU00012', 'Advanced UU 2', 2, 2, 'This is a detail value', 1, '0123456789ABCDEF4123456789ABCDEF1', '02-01-2018 12:42:34', '02-05-2018 23:43:34');

INSERT INTO "TREFERENCE" VALUES ('REF771', 'Reference Data UUU 1', 'This is a reference value with some long detail which continue over and over and over and over. Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet ...');
INSERT INTO "TREFERENCE" VALUES ('REF772', 'Reference Data UUU 2', 'This is a reference value with some long detail which continue over and over and over and over. Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet ...');
INSERT INTO "TREFERENCE" VALUES ('REF773', 'Reference Data UUU 3', 'This is a reference value with some long detail which continue over and over and over and over. Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet ...');

INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_UUU_1' , 'Value UUU 1', 'Value Other test UUU', 'REF771');
INSERT INTO "TCONSUMER" ("CODE", "VALUE", "VALUE_OTHER", "REFERENCE_KEY") VALUES ('CONSO_UUU_2' , 'Value UUU 2', 'Value Other test UUU', 'REF772');