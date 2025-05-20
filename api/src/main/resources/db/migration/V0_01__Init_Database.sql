
CREATE TABLE IF NOT EXISTS  Winery
(
    winery_id character varying(4) NOT NULL,
    keycloak_user_id character varying,
    winery_name character varying NOT NULL,
    private_key character varying,
    public_key character varying,
    salt character varying,
    CONSTRAINT "Winery_pkey" PRIMARY KEY (winery_id),
    CONSTRAINT "Winery_unique_keycloak_user_id" UNIQUE (keycloak_user_id)
);

CREATE TABLE IF NOT EXISTS Bottle
(
    id character varying NOT NULL,
    lot_id character varying(11) NOT NULL,
    cert_id character varying,
    sequential_number integer NOT NULL,
    reel_number integer,
    winery_id character varying(4) not null ,
    cert_update_status SMALLINT NOT NULL DEFAULT 0,
    lot_update_status smallint,
    CONSTRAINT "Bottle_pkey" PRIMARY KEY (id),
    CONSTRAINT "Bottle_fk_Winery" FOREIGN KEY (winery_id) REFERENCES Winery (winery_id),
    CONSTRAINT "Bottle_UQ_sequential_number" UNIQUE (sequential_number)
);
COMMENT ON COLUMN Bottle.cert_update_status IS '0: not_updated, 1: updated';
COMMENT ON COLUMN Bottle.lot_update_status IS '0: non_updated, 1: updated, 2: failed';

CREATE TABLE IF NOT EXISTS Certificate
(
    certificate_id character varying NOT NULL,
    company character varying NOT NULL,
    country character varying NOT NULL,
    address character varying NOT NULL,
    certificate_type character varying NOT NULL,
    nwa_signature character varying NOT NULL,
    tx_id character varying,
    job_id bigint,
    job_index character varying(64),
    cert_status SMALLINT NOT NULL DEFAULT 1,
    winery_id character varying (4) not null,
    revoke_job_id bigint,
    revoke_tx_id character varying,
    revoke_job_index character varying(64),
    CONSTRAINT "Certificate_pkey" PRIMARY KEY (certificate_id),
    CONSTRAINT "Certificate_fk_Winery" FOREIGN KEY (winery_id) REFERENCES winery (winery_id)
);
COMMENT ON COLUMN Certificate.cert_status IS '0: revoked, 1: active';

CREATE TABLE IF NOT EXISTS Certificate_Lot_Entry
(
    certificate_id character varying NOT NULL,
    laboratory character varying NOT NULL,
    wine_name character varying NOT NULL,
    wine_type character varying NOT NULL,
    nominate character varying NOT NULL,
    type character varying NOT NULL,
    quantity integer NOT NULL,
    price_per_bottle double precision NOT NULL,
    total_price double precision NOT NULL,
    "date" timestamp NOT NULL,
    lot_id character varying(11) NOT NULL,
    scanning_status character varying(11) not null ,
    winery_id character varying(4) NOT NULL,
    total_volume double precision NOT NULL,
    bottle_volume double precision NOT NULL,
    CONSTRAINT "Certificate_Lot_Entry_pkey" PRIMARY KEY (certificate_id,lot_id),
    CONSTRAINT "Certificate_Lot_Entry_fkey" FOREIGN KEY (certificate_id)
        REFERENCES Certificate (certificate_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_certificate_lot_entry_unique_ids
    ON Certificate_lot_entry USING btree
    (certificate_id ASC NULLS LAST, lot_id ASC NULLS LAST);

CREATE TABLE IF NOT EXISTS Lot
(
    lot_id character varying(11) NOT NULL,
    wine_name character varying NOT NULL,
    origin character varying NOT NULL,
    country_of_origin character varying NOT NULL,
    produced_by character varying NOT NULL,
    producer_address character varying NOT NULL,
    producer_latitude double precision,
    producer_longitude double precision,
    varietal_name character varying NOT NULL,
    vintage_year integer NOT NULL,
    wine_type character varying NOT NULL,
    wine_color character varying NOT NULL,
    harvest_date date NOT NULL,
    harvest_location character varying NOT NULL,
    pressing_date date NOT NULL,
    processing_location character varying NOT NULL,
    fermentation_vessel character varying NOT NULL,
    fermentation_duration character varying NOT NULL,
    aging_recipient character varying,
    aging_time character varying,
    storage_vessel character varying NOT NULL,
    bottling_date date,
    bottling_location character varying NOT NULL,
    number_of_bottles integer NOT NULL,
    winery_signature character varying,
    tx_id character varying,
    status smallint DEFAULT 0,
    winery_id character varying(4) not null ,
    job_id bigint,
	job_index character varying(64),
    CONSTRAINT "Lot_pkey" PRIMARY KEY (lot_id),
    CONSTRAINT "lot_fk_winery" FOREIGN KEY (winery_id)
        REFERENCES Winery (winery_id)
);
COMMENT ON COLUMN Lot.status IS '0: not finalized, 1: finalized, 2: approved';

CREATE TABLE IF NOT EXISTS ScantrustTask
(
    task_id CHARACTER VARYING NOT NULL,
    lot_id CHARACTER VARYING,
    task_state smallint,
    step smallint,
    CONSTRAINT "ScantrustTask_pkey" PRIMARY KEY (task_id),
    CONSTRAINT "ScantrustTask_fk_lot" FOREIGN KEY (lot_id) REFERENCES lot (lot_id)
);
