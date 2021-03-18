CREATE TABLE "TAPPLICATIONINFO"
(
    "ID"                   character varying(25),
    "MODEAPPLICATION"      smallint,
    "ACTEURCREATION"       character varying(50),
    "DATEMODIFICATION"     TIMESTAMP(3),
    "ACTEURMODIFICATION"   character varying(50),
    "DATECREATION"         TIMESTAMP(3),
    "MAJPARAMETRAGEMETIER" smallint,
    "VERSION"              character varying(80),
    "PROJET"               character varying(80),
    "SITE"                 character varying(80),
    "AFFICHERMESSAGE"      smallint,
    "MESSAGEINFO"          character varying(250)
) ;


CREATE TABLE "T_2MANY1"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY1_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY2"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY2_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY3"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY3_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY4"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY4_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY5"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY5_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY6"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY6_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY7"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY7_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY8"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY8_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY9"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY9_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";


CREATE TABLE "T_2MANY10"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY10_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY11"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY11_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY12"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY12_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY13"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY13_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY14"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY14_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY15"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY15_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY16"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY16_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY17"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY17_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY18"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY18_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY19"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY19_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";


CREATE TABLE "T_2MANY20"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY20_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY21"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY21_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY22"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY22_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY23"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY23_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY24"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY24_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY25"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY25_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY26"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY26_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY27"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY27_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY28"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY28_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY29"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY29_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";


CREATE TABLE "T_2MANY30"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY30_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY31"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY31_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY32"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY32_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY33"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY33_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY34"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY34_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY35"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY35_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY36"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY36_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY37"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY37_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY38"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY38_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY39"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY39_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";


CREATE TABLE "T_2MANY40"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY40_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY41"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY41_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY42"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY42_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY43"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY43_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY44"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY44_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY45"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY45_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY46"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY46_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY47"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY47_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY48"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY48_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY49"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY49_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";


CREATE TABLE "T_2MANY50"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY50_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY51"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY51_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY52"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY52_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY53"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY53_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY54"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY54_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY55"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY55_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY56"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY56_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY57"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY57_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY58"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY58_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY59"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY59_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";


CREATE TABLE "T_2MANY60"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY60_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY61"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY61_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY62"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY62_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY63"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY63_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY64"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY64_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY65"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY65_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY66"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY66_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY67"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY67_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY68"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY68_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY69"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY69_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";


CREATE TABLE "T_2MANY70"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY70_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY71"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY71_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY72"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY72_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY73"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY73_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY74"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY74_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY75"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY75_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY76"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY76_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY77"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY77_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY78"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY78_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY79"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY79_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";


CREATE TABLE "T_2MANY80"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY80_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY81"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY81_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY82"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY82_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY83"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY83_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY84"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY84_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY85"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY85_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY86"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY86_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY87"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY87_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY88"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY88_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY89"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY89_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";


CREATE TABLE "T_2MANY90"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY90_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY91"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY91_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY92"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY92_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY93"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY93_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY94"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY94_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY95"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY95_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY96"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY96_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY97"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY97_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY98"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY98_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY99"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY99_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

CREATE TABLE "T_2MANY100"
(
    "ID"     SERIAL                 NOT NULL,
    "NOM"    character varying(256) NOT NULL,
    "DETAIL" character varying(256) NOT NULL,
    CONSTRAINT "T_2MANY100_PK" PRIMARY KEY ("ID")
) TABLESPACE "USERS";

INSERT INTO "T_2MANY1"("NOM", "DETAIL")
VALUES ('Test1', 'Some details 1');
INSERT INTO "T_2MANY2"("NOM", "DETAIL")
VALUES ('Test2', 'Some details 2');
INSERT INTO "T_2MANY3"("NOM", "DETAIL")
VALUES ('Test3', 'Some details 3');
INSERT INTO "T_2MANY4"("NOM", "DETAIL")
VALUES ('Test4', 'Some details 4');
INSERT INTO "T_2MANY5"("NOM", "DETAIL")
VALUES ('Test5', 'Some details 5');
INSERT INTO "T_2MANY6"("NOM", "DETAIL")
VALUES ('Test6', 'Some details 6');
INSERT INTO "T_2MANY7"("NOM", "DETAIL")
VALUES ('Test7', 'Some details 7');
INSERT INTO "T_2MANY8"("NOM", "DETAIL")
VALUES ('Test8', 'Some details 8');
INSERT INTO "T_2MANY9"("NOM", "DETAIL")
VALUES ('Test9', 'Some details 9');
INSERT INTO "T_2MANY10"("NOM", "DETAIL")
VALUES ('Test10', 'Some details 10');
INSERT INTO "T_2MANY11"("NOM", "DETAIL")
VALUES ('Test11', 'Some details 11');
INSERT INTO "T_2MANY12"("NOM", "DETAIL")
VALUES ('Test12', 'Some details 12');
INSERT INTO "T_2MANY13"("NOM", "DETAIL")
VALUES ('Test13', 'Some details 13');
INSERT INTO "T_2MANY14"("NOM", "DETAIL")
VALUES ('Test14', 'Some details 14');
INSERT INTO "T_2MANY15"("NOM", "DETAIL")
VALUES ('Test15', 'Some details 15');
INSERT INTO "T_2MANY16"("NOM", "DETAIL")
VALUES ('Test16', 'Some details 16');
INSERT INTO "T_2MANY17"("NOM", "DETAIL")
VALUES ('Test17', 'Some details 17');
INSERT INTO "T_2MANY18"("NOM", "DETAIL")
VALUES ('Test18', 'Some details 18');
INSERT INTO "T_2MANY19"("NOM", "DETAIL")
VALUES ('Test19', 'Some details 19');
INSERT INTO "T_2MANY20"("NOM", "DETAIL")
VALUES ('Test20', 'Some details 20');
INSERT INTO "T_2MANY21"("NOM", "DETAIL")
VALUES ('Test21', 'Some details 21');
INSERT INTO "T_2MANY22"("NOM", "DETAIL")
VALUES ('Test22', 'Some details 22');
INSERT INTO "T_2MANY23"("NOM", "DETAIL")
VALUES ('Test23', 'Some details 23');
INSERT INTO "T_2MANY24"("NOM", "DETAIL")
VALUES ('Test24', 'Some details 24');
INSERT INTO "T_2MANY25"("NOM", "DETAIL")
VALUES ('Test25', 'Some details 25');
INSERT INTO "T_2MANY26"("NOM", "DETAIL")
VALUES ('Test26', 'Some details 26');
INSERT INTO "T_2MANY27"("NOM", "DETAIL")
VALUES ('Test27', 'Some details 27');
INSERT INTO "T_2MANY28"("NOM", "DETAIL")
VALUES ('Test28', 'Some details 28');
INSERT INTO "T_2MANY29"("NOM", "DETAIL")
VALUES ('Test29', 'Some details 29');
INSERT INTO "T_2MANY30"("NOM", "DETAIL")
VALUES ('Test30', 'Some details 30');
INSERT INTO "T_2MANY31"("NOM", "DETAIL")
VALUES ('Test31', 'Some details 31');
INSERT INTO "T_2MANY32"("NOM", "DETAIL")
VALUES ('Test32', 'Some details 32');
INSERT INTO "T_2MANY33"("NOM", "DETAIL")
VALUES ('Test33', 'Some details 33');
INSERT INTO "T_2MANY34"("NOM", "DETAIL")
VALUES ('Test34', 'Some details 34');
INSERT INTO "T_2MANY35"("NOM", "DETAIL")
VALUES ('Test35', 'Some details 35');
INSERT INTO "T_2MANY36"("NOM", "DETAIL")
VALUES ('Test36', 'Some details 36');
INSERT INTO "T_2MANY37"("NOM", "DETAIL")
VALUES ('Test37', 'Some details 37');
INSERT INTO "T_2MANY38"("NOM", "DETAIL")
VALUES ('Test38', 'Some details 38');
INSERT INTO "T_2MANY39"("NOM", "DETAIL")
VALUES ('Test39', 'Some details 39');
INSERT INTO "T_2MANY40"("NOM", "DETAIL")
VALUES ('Test40', 'Some details 40');
INSERT INTO "T_2MANY41"("NOM", "DETAIL")
VALUES ('Test41', 'Some details 41');
INSERT INTO "T_2MANY42"("NOM", "DETAIL")
VALUES ('Test42', 'Some details 42');
INSERT INTO "T_2MANY43"("NOM", "DETAIL")
VALUES ('Test43', 'Some details 43');
INSERT INTO "T_2MANY44"("NOM", "DETAIL")
VALUES ('Test44', 'Some details 44');
INSERT INTO "T_2MANY45"("NOM", "DETAIL")
VALUES ('Test45', 'Some details 45');
INSERT INTO "T_2MANY46"("NOM", "DETAIL")
VALUES ('Test46', 'Some details 46');
INSERT INTO "T_2MANY47"("NOM", "DETAIL")
VALUES ('Test47', 'Some details 47');
INSERT INTO "T_2MANY48"("NOM", "DETAIL")
VALUES ('Test48', 'Some details 48');
INSERT INTO "T_2MANY49"("NOM", "DETAIL")
VALUES ('Test49', 'Some details 49');
INSERT INTO "T_2MANY50"("NOM", "DETAIL")
VALUES ('Test50', 'Some details 50');
INSERT INTO "T_2MANY51"("NOM", "DETAIL")
VALUES ('Test51', 'Some details 51');
INSERT INTO "T_2MANY52"("NOM", "DETAIL")
VALUES ('Test52', 'Some details 52');
INSERT INTO "T_2MANY53"("NOM", "DETAIL")
VALUES ('Test53', 'Some details 53');
INSERT INTO "T_2MANY54"("NOM", "DETAIL")
VALUES ('Test54', 'Some details 54');
INSERT INTO "T_2MANY55"("NOM", "DETAIL")
VALUES ('Test55', 'Some details 55');
INSERT INTO "T_2MANY56"("NOM", "DETAIL")
VALUES ('Test56', 'Some details 56');
INSERT INTO "T_2MANY57"("NOM", "DETAIL")
VALUES ('Test57', 'Some details 57');
INSERT INTO "T_2MANY58"("NOM", "DETAIL")
VALUES ('Test58', 'Some details 58');
INSERT INTO "T_2MANY59"("NOM", "DETAIL")
VALUES ('Test59', 'Some details 59');
INSERT INTO "T_2MANY60"("NOM", "DETAIL")
VALUES ('Test60', 'Some details 60');
INSERT INTO "T_2MANY61"("NOM", "DETAIL")
VALUES ('Test61', 'Some details 61');
INSERT INTO "T_2MANY62"("NOM", "DETAIL")
VALUES ('Test62', 'Some details 62');
INSERT INTO "T_2MANY63"("NOM", "DETAIL")
VALUES ('Test63', 'Some details 63');
INSERT INTO "T_2MANY64"("NOM", "DETAIL")
VALUES ('Test64', 'Some details 64');
INSERT INTO "T_2MANY65"("NOM", "DETAIL")
VALUES ('Test65', 'Some details 65');
INSERT INTO "T_2MANY66"("NOM", "DETAIL")
VALUES ('Test66', 'Some details 66');
INSERT INTO "T_2MANY67"("NOM", "DETAIL")
VALUES ('Test67', 'Some details 67');
INSERT INTO "T_2MANY68"("NOM", "DETAIL")
VALUES ('Test68', 'Some details 68');
INSERT INTO "T_2MANY69"("NOM", "DETAIL")
VALUES ('Test69', 'Some details 69');
INSERT INTO "T_2MANY70"("NOM", "DETAIL")
VALUES ('Test70', 'Some details 70');
INSERT INTO "T_2MANY71"("NOM", "DETAIL")
VALUES ('Test71', 'Some details 71');
INSERT INTO "T_2MANY72"("NOM", "DETAIL")
VALUES ('Test72', 'Some details 72');
INSERT INTO "T_2MANY73"("NOM", "DETAIL")
VALUES ('Test73', 'Some details 73');
INSERT INTO "T_2MANY74"("NOM", "DETAIL")
VALUES ('Test74', 'Some details 74');
INSERT INTO "T_2MANY75"("NOM", "DETAIL")
VALUES ('Test75', 'Some details 75');
INSERT INTO "T_2MANY76"("NOM", "DETAIL")
VALUES ('Test76', 'Some details 76');
INSERT INTO "T_2MANY77"("NOM", "DETAIL")
VALUES ('Test77', 'Some details 77');
INSERT INTO "T_2MANY78"("NOM", "DETAIL")
VALUES ('Test78', 'Some details 78');
INSERT INTO "T_2MANY79"("NOM", "DETAIL")
VALUES ('Test79', 'Some details 79');
INSERT INTO "T_2MANY80"("NOM", "DETAIL")
VALUES ('Test80', 'Some details 80');
INSERT INTO "T_2MANY81"("NOM", "DETAIL")
VALUES ('Test81', 'Some details 81');
INSERT INTO "T_2MANY82"("NOM", "DETAIL")
VALUES ('Test82', 'Some details 82');
INSERT INTO "T_2MANY83"("NOM", "DETAIL")
VALUES ('Test83', 'Some details 83');
INSERT INTO "T_2MANY84"("NOM", "DETAIL")
VALUES ('Test84', 'Some details 84');
INSERT INTO "T_2MANY85"("NOM", "DETAIL")
VALUES ('Test85', 'Some details 85');
INSERT INTO "T_2MANY86"("NOM", "DETAIL")
VALUES ('Test86', 'Some details 86');
INSERT INTO "T_2MANY87"("NOM", "DETAIL")
VALUES ('Test87', 'Some details 87');
INSERT INTO "T_2MANY88"("NOM", "DETAIL")
VALUES ('Test88', 'Some details 88');
INSERT INTO "T_2MANY89"("NOM", "DETAIL")
VALUES ('Test89', 'Some details 89');
INSERT INTO "T_2MANY90"("NOM", "DETAIL")
VALUES ('Test90', 'Some details 90');
INSERT INTO "T_2MANY91"("NOM", "DETAIL")
VALUES ('Test91', 'Some details 91');
INSERT INTO "T_2MANY92"("NOM", "DETAIL")
VALUES ('Test92', 'Some details 92');
INSERT INTO "T_2MANY93"("NOM", "DETAIL")
VALUES ('Test93', 'Some details 93');
INSERT INTO "T_2MANY94"("NOM", "DETAIL")
VALUES ('Test94', 'Some details 94');
INSERT INTO "T_2MANY95"("NOM", "DETAIL")
VALUES ('Test95', 'Some details 95');
INSERT INTO "T_2MANY96"("NOM", "DETAIL")
VALUES ('Test96', 'Some details 96');
INSERT INTO "T_2MANY97"("NOM", "DETAIL")
VALUES ('Test97', 'Some details 97');
INSERT INTO "T_2MANY98"("NOM", "DETAIL")
VALUES ('Test98', 'Some details 98');
INSERT INTO "T_2MANY99"("NOM", "DETAIL")
VALUES ('Test99', 'Some details 99');
INSERT INTO "T_2MANY100"("NOM", "DETAIL")
VALUES ('Test100', 'Some details 100');
INSERT INTO "T_2MANY1"("NOM", "DETAIL")
VALUES ('2Test1', 'Some details 1');
INSERT INTO "T_2MANY2"("NOM", "DETAIL")
VALUES ('2Test2', 'Some details 2');
INSERT INTO "T_2MANY3"("NOM", "DETAIL")
VALUES ('2Test3', 'Some details 3');
INSERT INTO "T_2MANY4"("NOM", "DETAIL")
VALUES ('2Test4', 'Some details 4');
INSERT INTO "T_2MANY5"("NOM", "DETAIL")
VALUES ('2Test5', 'Some details 5');
INSERT INTO "T_2MANY6"("NOM", "DETAIL")
VALUES ('2Test6', 'Some details 6');
INSERT INTO "T_2MANY7"("NOM", "DETAIL")
VALUES ('2Test7', 'Some details 7');
INSERT INTO "T_2MANY8"("NOM", "DETAIL")
VALUES ('2Test8', 'Some details 8');
INSERT INTO "T_2MANY9"("NOM", "DETAIL")
VALUES ('2Test9', 'Some details 9');
INSERT INTO "T_2MANY10"("NOM", "DETAIL")
VALUES ('2Test10', 'Some details 10');
INSERT INTO "T_2MANY11"("NOM", "DETAIL")
VALUES ('2Test11', 'Some details 11');
INSERT INTO "T_2MANY12"("NOM", "DETAIL")
VALUES ('2Test12', 'Some details 12');
INSERT INTO "T_2MANY13"("NOM", "DETAIL")
VALUES ('2Test13', 'Some details 13');
INSERT INTO "T_2MANY14"("NOM", "DETAIL")
VALUES ('2Test14', 'Some details 14');
INSERT INTO "T_2MANY15"("NOM", "DETAIL")
VALUES ('2Test15', 'Some details 15');
INSERT INTO "T_2MANY16"("NOM", "DETAIL")
VALUES ('2Test16', 'Some details 16');
INSERT INTO "T_2MANY17"("NOM", "DETAIL")
VALUES ('2Test17', 'Some details 17');
INSERT INTO "T_2MANY18"("NOM", "DETAIL")
VALUES ('2Test18', 'Some details 18');
INSERT INTO "T_2MANY19"("NOM", "DETAIL")
VALUES ('2Test19', 'Some details 19');
INSERT INTO "T_2MANY20"("NOM", "DETAIL")
VALUES ('2Test20', 'Some details 20');
INSERT INTO "T_2MANY21"("NOM", "DETAIL")
VALUES ('2Test21', 'Some details 21');
INSERT INTO "T_2MANY22"("NOM", "DETAIL")
VALUES ('2Test22', 'Some details 22');
INSERT INTO "T_2MANY23"("NOM", "DETAIL")
VALUES ('2Test23', 'Some details 23');
INSERT INTO "T_2MANY24"("NOM", "DETAIL")
VALUES ('2Test24', 'Some details 24');
INSERT INTO "T_2MANY25"("NOM", "DETAIL")
VALUES ('2Test25', 'Some details 25');
INSERT INTO "T_2MANY26"("NOM", "DETAIL")
VALUES ('2Test26', 'Some details 26');
INSERT INTO "T_2MANY27"("NOM", "DETAIL")
VALUES ('2Test27', 'Some details 27');
INSERT INTO "T_2MANY28"("NOM", "DETAIL")
VALUES ('2Test28', 'Some details 28');
INSERT INTO "T_2MANY29"("NOM", "DETAIL")
VALUES ('2Test29', 'Some details 29');
INSERT INTO "T_2MANY30"("NOM", "DETAIL")
VALUES ('2Test30', 'Some details 30');
INSERT INTO "T_2MANY31"("NOM", "DETAIL")
VALUES ('2Test31', 'Some details 31');
INSERT INTO "T_2MANY32"("NOM", "DETAIL")
VALUES ('2Test32', 'Some details 32');
INSERT INTO "T_2MANY33"("NOM", "DETAIL")
VALUES ('2Test33', 'Some details 33');
INSERT INTO "T_2MANY34"("NOM", "DETAIL")
VALUES ('2Test34', 'Some details 34');
INSERT INTO "T_2MANY35"("NOM", "DETAIL")
VALUES ('2Test35', 'Some details 35');
INSERT INTO "T_2MANY36"("NOM", "DETAIL")
VALUES ('2Test36', 'Some details 36');
INSERT INTO "T_2MANY37"("NOM", "DETAIL")
VALUES ('2Test37', 'Some details 37');
INSERT INTO "T_2MANY38"("NOM", "DETAIL")
VALUES ('2Test38', 'Some details 38');
INSERT INTO "T_2MANY39"("NOM", "DETAIL")
VALUES ('2Test39', 'Some details 39');
INSERT INTO "T_2MANY40"("NOM", "DETAIL")
VALUES ('2Test40', 'Some details 40');
INSERT INTO "T_2MANY41"("NOM", "DETAIL")
VALUES ('2Test41', 'Some details 41');
INSERT INTO "T_2MANY42"("NOM", "DETAIL")
VALUES ('2Test42', 'Some details 42');
INSERT INTO "T_2MANY43"("NOM", "DETAIL")
VALUES ('2Test43', 'Some details 43');
INSERT INTO "T_2MANY44"("NOM", "DETAIL")
VALUES ('2Test44', 'Some details 44');
INSERT INTO "T_2MANY45"("NOM", "DETAIL")
VALUES ('2Test45', 'Some details 45');
INSERT INTO "T_2MANY46"("NOM", "DETAIL")
VALUES ('2Test46', 'Some details 46');
INSERT INTO "T_2MANY47"("NOM", "DETAIL")
VALUES ('2Test47', 'Some details 47');
INSERT INTO "T_2MANY48"("NOM", "DETAIL")
VALUES ('2Test48', 'Some details 48');
INSERT INTO "T_2MANY49"("NOM", "DETAIL")
VALUES ('2Test49', 'Some details 49');
INSERT INTO "T_2MANY50"("NOM", "DETAIL")
VALUES ('2Test50', 'Some details 50');
INSERT INTO "T_2MANY51"("NOM", "DETAIL")
VALUES ('2Test51', 'Some details 51');
INSERT INTO "T_2MANY52"("NOM", "DETAIL")
VALUES ('2Test52', 'Some details 52');
INSERT INTO "T_2MANY53"("NOM", "DETAIL")
VALUES ('2Test53', 'Some details 53');
INSERT INTO "T_2MANY54"("NOM", "DETAIL")
VALUES ('2Test54', 'Some details 54');
INSERT INTO "T_2MANY55"("NOM", "DETAIL")
VALUES ('2Test55', 'Some details 55');
INSERT INTO "T_2MANY56"("NOM", "DETAIL")
VALUES ('2Test56', 'Some details 56');
INSERT INTO "T_2MANY57"("NOM", "DETAIL")
VALUES ('2Test57', 'Some details 57');
INSERT INTO "T_2MANY58"("NOM", "DETAIL")
VALUES ('2Test58', 'Some details 58');
INSERT INTO "T_2MANY59"("NOM", "DETAIL")
VALUES ('2Test59', 'Some details 59');
INSERT INTO "T_2MANY60"("NOM", "DETAIL")
VALUES ('2Test60', 'Some details 60');
INSERT INTO "T_2MANY61"("NOM", "DETAIL")
VALUES ('2Test61', 'Some details 61');
INSERT INTO "T_2MANY62"("NOM", "DETAIL")
VALUES ('2Test62', 'Some details 62');
INSERT INTO "T_2MANY63"("NOM", "DETAIL")
VALUES ('2Test63', 'Some details 63');
INSERT INTO "T_2MANY64"("NOM", "DETAIL")
VALUES ('2Test64', 'Some details 64');
INSERT INTO "T_2MANY65"("NOM", "DETAIL")
VALUES ('2Test65', 'Some details 65');
INSERT INTO "T_2MANY66"("NOM", "DETAIL")
VALUES ('2Test66', 'Some details 66');
INSERT INTO "T_2MANY67"("NOM", "DETAIL")
VALUES ('2Test67', 'Some details 67');
INSERT INTO "T_2MANY68"("NOM", "DETAIL")
VALUES ('2Test68', 'Some details 68');
INSERT INTO "T_2MANY69"("NOM", "DETAIL")
VALUES ('2Test69', 'Some details 69');
INSERT INTO "T_2MANY70"("NOM", "DETAIL")
VALUES ('2Test70', 'Some details 70');
INSERT INTO "T_2MANY71"("NOM", "DETAIL")
VALUES ('2Test71', 'Some details 71');
INSERT INTO "T_2MANY72"("NOM", "DETAIL")
VALUES ('2Test72', 'Some details 72');
INSERT INTO "T_2MANY73"("NOM", "DETAIL")
VALUES ('2Test73', 'Some details 73');
INSERT INTO "T_2MANY74"("NOM", "DETAIL")
VALUES ('2Test74', 'Some details 74');
INSERT INTO "T_2MANY75"("NOM", "DETAIL")
VALUES ('2Test75', 'Some details 75');
INSERT INTO "T_2MANY76"("NOM", "DETAIL")
VALUES ('2Test76', 'Some details 76');
INSERT INTO "T_2MANY77"("NOM", "DETAIL")
VALUES ('2Test77', 'Some details 77');
INSERT INTO "T_2MANY78"("NOM", "DETAIL")
VALUES ('2Test78', 'Some details 78');
INSERT INTO "T_2MANY79"("NOM", "DETAIL")
VALUES ('2Test79', 'Some details 79');
INSERT INTO "T_2MANY80"("NOM", "DETAIL")
VALUES ('2Test80', 'Some details 80');
INSERT INTO "T_2MANY81"("NOM", "DETAIL")
VALUES ('2Test81', 'Some details 81');
INSERT INTO "T_2MANY82"("NOM", "DETAIL")
VALUES ('2Test82', 'Some details 82');
INSERT INTO "T_2MANY83"("NOM", "DETAIL")
VALUES ('2Test83', 'Some details 83');
INSERT INTO "T_2MANY84"("NOM", "DETAIL")
VALUES ('2Test84', 'Some details 84');
INSERT INTO "T_2MANY85"("NOM", "DETAIL")
VALUES ('2Test85', 'Some details 85');
INSERT INTO "T_2MANY86"("NOM", "DETAIL")
VALUES ('2Test86', 'Some details 86');
INSERT INTO "T_2MANY87"("NOM", "DETAIL")
VALUES ('2Test87', 'Some details 87');
INSERT INTO "T_2MANY88"("NOM", "DETAIL")
VALUES ('2Test88', 'Some details 88');
INSERT INTO "T_2MANY89"("NOM", "DETAIL")
VALUES ('2Test89', 'Some details 89');
INSERT INTO "T_2MANY90"("NOM", "DETAIL")
VALUES ('2Test90', 'Some details 90');
INSERT INTO "T_2MANY91"("NOM", "DETAIL")
VALUES ('2Test91', 'Some details 91');
INSERT INTO "T_2MANY92"("NOM", "DETAIL")
VALUES ('2Test92', 'Some details 92');
INSERT INTO "T_2MANY93"("NOM", "DETAIL")
VALUES ('2Test93', 'Some details 93');
INSERT INTO "T_2MANY94"("NOM", "DETAIL")
VALUES ('2Test94', 'Some details 94');
INSERT INTO "T_2MANY95"("NOM", "DETAIL")
VALUES ('2Test95', 'Some details 95');
INSERT INTO "T_2MANY96"("NOM", "DETAIL")
VALUES ('2Test96', 'Some details 96');
INSERT INTO "T_2MANY97"("NOM", "DETAIL")
VALUES ('2Test97', 'Some details 97');
INSERT INTO "T_2MANY98"("NOM", "DETAIL")
VALUES ('2Test98', 'Some details 98');
INSERT INTO "T_2MANY99"("NOM", "DETAIL")
VALUES ('2Test99', 'Some details 99');
INSERT INTO "T_2MANY100"("NOM", "DETAIL")
VALUES ('2Test100', 'Some details 100');
INSERT INTO "T_2MANY1"("NOM", "DETAIL")
VALUES ('3Test1', 'Some details 1');
INSERT INTO "T_2MANY2"("NOM", "DETAIL")
VALUES ('3Test2', 'Some details 2');
INSERT INTO "T_2MANY3"("NOM", "DETAIL")
VALUES ('3Test3', 'Some details 3');
INSERT INTO "T_2MANY4"("NOM", "DETAIL")
VALUES ('3Test4', 'Some details 4');
INSERT INTO "T_2MANY5"("NOM", "DETAIL")
VALUES ('3Test5', 'Some details 5');
INSERT INTO "T_2MANY6"("NOM", "DETAIL")
VALUES ('3Test6', 'Some details 6');
INSERT INTO "T_2MANY7"("NOM", "DETAIL")
VALUES ('3Test7', 'Some details 7');
INSERT INTO "T_2MANY8"("NOM", "DETAIL")
VALUES ('3Test8', 'Some details 8');
INSERT INTO "T_2MANY9"("NOM", "DETAIL")
VALUES ('3Test9', 'Some details 9');
INSERT INTO "T_2MANY10"("NOM", "DETAIL")
VALUES ('3Test10', 'Some details 10');
INSERT INTO "T_2MANY11"("NOM", "DETAIL")
VALUES ('3Test11', 'Some details 11');
INSERT INTO "T_2MANY12"("NOM", "DETAIL")
VALUES ('3Test12', 'Some details 12');
INSERT INTO "T_2MANY13"("NOM", "DETAIL")
VALUES ('3Test13', 'Some details 13');
INSERT INTO "T_2MANY14"("NOM", "DETAIL")
VALUES ('3Test14', 'Some details 14');
INSERT INTO "T_2MANY15"("NOM", "DETAIL")
VALUES ('3Test15', 'Some details 15');
INSERT INTO "T_2MANY16"("NOM", "DETAIL")
VALUES ('3Test16', 'Some details 16');
INSERT INTO "T_2MANY17"("NOM", "DETAIL")
VALUES ('3Test17', 'Some details 17');
INSERT INTO "T_2MANY18"("NOM", "DETAIL")
VALUES ('3Test18', 'Some details 18');
INSERT INTO "T_2MANY19"("NOM", "DETAIL")
VALUES ('3Test19', 'Some details 19');
INSERT INTO "T_2MANY20"("NOM", "DETAIL")
VALUES ('3Test20', 'Some details 20');
INSERT INTO "T_2MANY21"("NOM", "DETAIL")
VALUES ('3Test21', 'Some details 21');
INSERT INTO "T_2MANY22"("NOM", "DETAIL")
VALUES ('3Test22', 'Some details 22');
INSERT INTO "T_2MANY23"("NOM", "DETAIL")
VALUES ('3Test23', 'Some details 23');
INSERT INTO "T_2MANY24"("NOM", "DETAIL")
VALUES ('3Test24', 'Some details 24');
INSERT INTO "T_2MANY25"("NOM", "DETAIL")
VALUES ('3Test25', 'Some details 25');
INSERT INTO "T_2MANY26"("NOM", "DETAIL")
VALUES ('3Test26', 'Some details 26');
INSERT INTO "T_2MANY27"("NOM", "DETAIL")
VALUES ('3Test27', 'Some details 27');
INSERT INTO "T_2MANY28"("NOM", "DETAIL")
VALUES ('3Test28', 'Some details 28');
INSERT INTO "T_2MANY29"("NOM", "DETAIL")
VALUES ('3Test29', 'Some details 29');
INSERT INTO "T_2MANY30"("NOM", "DETAIL")
VALUES ('3Test30', 'Some details 30');
INSERT INTO "T_2MANY31"("NOM", "DETAIL")
VALUES ('3Test31', 'Some details 31');
INSERT INTO "T_2MANY32"("NOM", "DETAIL")
VALUES ('3Test32', 'Some details 32');
INSERT INTO "T_2MANY33"("NOM", "DETAIL")
VALUES ('3Test33', 'Some details 33');
INSERT INTO "T_2MANY34"("NOM", "DETAIL")
VALUES ('3Test34', 'Some details 34');
INSERT INTO "T_2MANY35"("NOM", "DETAIL")
VALUES ('3Test35', 'Some details 35');
INSERT INTO "T_2MANY36"("NOM", "DETAIL")
VALUES ('3Test36', 'Some details 36');
INSERT INTO "T_2MANY37"("NOM", "DETAIL")
VALUES ('3Test37', 'Some details 37');
INSERT INTO "T_2MANY38"("NOM", "DETAIL")
VALUES ('3Test38', 'Some details 38');
INSERT INTO "T_2MANY39"("NOM", "DETAIL")
VALUES ('3Test39', 'Some details 39');
INSERT INTO "T_2MANY40"("NOM", "DETAIL")
VALUES ('3Test40', 'Some details 40');
INSERT INTO "T_2MANY41"("NOM", "DETAIL")
VALUES ('3Test41', 'Some details 41');
INSERT INTO "T_2MANY42"("NOM", "DETAIL")
VALUES ('3Test42', 'Some details 42');
INSERT INTO "T_2MANY43"("NOM", "DETAIL")
VALUES ('3Test43', 'Some details 43');
INSERT INTO "T_2MANY44"("NOM", "DETAIL")
VALUES ('3Test44', 'Some details 44');
INSERT INTO "T_2MANY45"("NOM", "DETAIL")
VALUES ('3Test45', 'Some details 45');
INSERT INTO "T_2MANY46"("NOM", "DETAIL")
VALUES ('3Test46', 'Some details 46');
INSERT INTO "T_2MANY47"("NOM", "DETAIL")
VALUES ('3Test47', 'Some details 47');
INSERT INTO "T_2MANY48"("NOM", "DETAIL")
VALUES ('3Test48', 'Some details 48');
INSERT INTO "T_2MANY49"("NOM", "DETAIL")
VALUES ('3Test49', 'Some details 49');
INSERT INTO "T_2MANY50"("NOM", "DETAIL")
VALUES ('3Test50', 'Some details 50');
INSERT INTO "T_2MANY51"("NOM", "DETAIL")
VALUES ('3Test51', 'Some details 51');
INSERT INTO "T_2MANY52"("NOM", "DETAIL")
VALUES ('3Test52', 'Some details 52');
INSERT INTO "T_2MANY53"("NOM", "DETAIL")
VALUES ('3Test53', 'Some details 53');
INSERT INTO "T_2MANY54"("NOM", "DETAIL")
VALUES ('3Test54', 'Some details 54');
INSERT INTO "T_2MANY55"("NOM", "DETAIL")
VALUES ('3Test55', 'Some details 55');
INSERT INTO "T_2MANY56"("NOM", "DETAIL")
VALUES ('3Test56', 'Some details 56');
INSERT INTO "T_2MANY57"("NOM", "DETAIL")
VALUES ('3Test57', 'Some details 57');
INSERT INTO "T_2MANY58"("NOM", "DETAIL")
VALUES ('3Test58', 'Some details 58');
INSERT INTO "T_2MANY59"("NOM", "DETAIL")
VALUES ('3Test59', 'Some details 59');
INSERT INTO "T_2MANY60"("NOM", "DETAIL")
VALUES ('3Test60', 'Some details 60');
INSERT INTO "T_2MANY61"("NOM", "DETAIL")
VALUES ('3Test61', 'Some details 61');
INSERT INTO "T_2MANY62"("NOM", "DETAIL")
VALUES ('3Test62', 'Some details 62');
INSERT INTO "T_2MANY63"("NOM", "DETAIL")
VALUES ('3Test63', 'Some details 63');
INSERT INTO "T_2MANY64"("NOM", "DETAIL")
VALUES ('3Test64', 'Some details 64');
INSERT INTO "T_2MANY65"("NOM", "DETAIL")
VALUES ('3Test65', 'Some details 65');
INSERT INTO "T_2MANY66"("NOM", "DETAIL")
VALUES ('3Test66', 'Some details 66');
INSERT INTO "T_2MANY67"("NOM", "DETAIL")
VALUES ('3Test67', 'Some details 67');
INSERT INTO "T_2MANY68"("NOM", "DETAIL")
VALUES ('3Test68', 'Some details 68');
INSERT INTO "T_2MANY69"("NOM", "DETAIL")
VALUES ('3Test69', 'Some details 69');
INSERT INTO "T_2MANY70"("NOM", "DETAIL")
VALUES ('3Test70', 'Some details 70');
INSERT INTO "T_2MANY71"("NOM", "DETAIL")
VALUES ('3Test71', 'Some details 71');
INSERT INTO "T_2MANY72"("NOM", "DETAIL")
VALUES ('3Test72', 'Some details 72');
INSERT INTO "T_2MANY73"("NOM", "DETAIL")
VALUES ('3Test73', 'Some details 73');
INSERT INTO "T_2MANY74"("NOM", "DETAIL")
VALUES ('3Test74', 'Some details 74');
INSERT INTO "T_2MANY75"("NOM", "DETAIL")
VALUES ('3Test75', 'Some details 75');
INSERT INTO "T_2MANY76"("NOM", "DETAIL")
VALUES ('3Test76', 'Some details 76');
INSERT INTO "T_2MANY77"("NOM", "DETAIL")
VALUES ('3Test77', 'Some details 77');
INSERT INTO "T_2MANY78"("NOM", "DETAIL")
VALUES ('3Test78', 'Some details 78');
INSERT INTO "T_2MANY79"("NOM", "DETAIL")
VALUES ('3Test79', 'Some details 79');
INSERT INTO "T_2MANY80"("NOM", "DETAIL")
VALUES ('3Test80', 'Some details 80');
INSERT INTO "T_2MANY81"("NOM", "DETAIL")
VALUES ('3Test81', 'Some details 81');
INSERT INTO "T_2MANY82"("NOM", "DETAIL")
VALUES ('3Test82', 'Some details 82');
INSERT INTO "T_2MANY83"("NOM", "DETAIL")
VALUES ('3Test83', 'Some details 83');
INSERT INTO "T_2MANY84"("NOM", "DETAIL")
VALUES ('3Test84', 'Some details 84');
INSERT INTO "T_2MANY85"("NOM", "DETAIL")
VALUES ('3Test85', 'Some details 85');
INSERT INTO "T_2MANY86"("NOM", "DETAIL")
VALUES ('3Test86', 'Some details 86');
INSERT INTO "T_2MANY87"("NOM", "DETAIL")
VALUES ('3Test87', 'Some details 87');
INSERT INTO "T_2MANY88"("NOM", "DETAIL")
VALUES ('3Test88', 'Some details 88');
INSERT INTO "T_2MANY89"("NOM", "DETAIL")
VALUES ('3Test89', 'Some details 89');
INSERT INTO "T_2MANY90"("NOM", "DETAIL")
VALUES ('3Test90', 'Some details 90');
INSERT INTO "T_2MANY91"("NOM", "DETAIL")
VALUES ('3Test91', 'Some details 91');
INSERT INTO "T_2MANY92"("NOM", "DETAIL")
VALUES ('3Test92', 'Some details 92');
INSERT INTO "T_2MANY93"("NOM", "DETAIL")
VALUES ('3Test93', 'Some details 93');
INSERT INTO "T_2MANY94"("NOM", "DETAIL")
VALUES ('3Test94', 'Some details 94');
INSERT INTO "T_2MANY95"("NOM", "DETAIL")
VALUES ('3Test95', 'Some details 95');
INSERT INTO "T_2MANY96"("NOM", "DETAIL")
VALUES ('3Test96', 'Some details 96');
INSERT INTO "T_2MANY97"("NOM", "DETAIL")
VALUES ('3Test97', 'Some details 97');
INSERT INTO "T_2MANY98"("NOM", "DETAIL")
VALUES ('3Test98', 'Some details 98');
INSERT INTO "T_2MANY99"("NOM", "DETAIL")
VALUES ('3Test99', 'Some details 99');
INSERT INTO "T_2MANY100"("NOM", "DETAIL")
VALUES ('3Test100', 'Some details 100');


INSERT INTO "TAPPLICATIONINFO" VALUES ('AAAA', 1, 'DEMO USER', '02-01-2018 18:42:34',  NULL, NULL, 1 , 'V-DEMO-1', 'DATAGATE-DEMO', 'PROD', 1, 'Hello World');
INSERT INTO "TAPPLICATIONINFO" VALUES ('BBBB', 1, 'DEMO USER', '02-01-2019 15:42:34',  NULL, NULL, 1 , 'V-DEMO-2', 'DATAGATE-DEMO', 'PROD', 1, 'Hello World 2');


COMMIT;
