---- This is OK and easier because at time of integration these tables are completely empty
DROP TABLE Certificate_Lot_Entry;
DROP TABLE Certificate;

CREATE TABLE Certificate
(
    certificate_id character varying NOT NULL,
    certificate_number character varying NOT NULL,
    certificate_type character varying NOT NULL,
    export_country character varying NOT NULL,
    exam_protocol_number character varying,
    tasting_protocol_number character varying,
    signature character varying NOT NULL,
    pub_key character varying NOT NULL,
    tx_id character varying,
    job_id bigint,
    job_index character varying(64),
    cert_status SMALLINT NOT NULL DEFAULT 1,
    winery_id character varying (4) NOT NULL,
    revoke_job_id bigint,
    revoke_tx_id character varying,
    revoke_job_index character varying(64),
    revoke_signature character varying,
    revoke_pub_key character varying,
    CONSTRAINT "Certificate_pkey" PRIMARY KEY (certificate_id),
    CONSTRAINT "Certificate_fk_Winery" FOREIGN KEY (winery_id) REFERENCES winery (winery_id)
);
COMMENT ON COLUMN Certificate.cert_status IS '0: revoked, 1: active';

CREATE TABLE Certificate_Lot_Entry
(
    certificate_id character varying NOT NULL,
    lot_id character varying(11) NOT NULL,
    wine_name character varying NOT NULL,
    wine_description character varying,
    serial_name character varying,
    origin character varying,
    viticulture_area character varying,
    type character varying,
    color character varying,
    sugar_content_category character varying,
    grape_variety character varying,
    harvest_year integer,
    delayed_on_chacha boolean,
    bottle_type character varying,
    bottling_date date,
    bottle_volume double precision,
    bottle_count_in_lot integer,
    scanning_status character varying(11) NOT NULL,
    winery_id character varying(4) NOT NULL,
    CONSTRAINT "Certificate_Lot_Entry_pkey" PRIMARY KEY (certificate_id,lot_id),
    CONSTRAINT "Certificate_Lot_Entry_fkey" FOREIGN KEY (certificate_id)
        REFERENCES Certificate (certificate_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_certificate_lot_entry_unique_ids
    ON Certificate_lot_entry USING btree
    (certificate_id ASC NULLS LAST, lot_id ASC NULLS LAST);

ALTER TABLE winery ADD COLUMN winery_rs_code character varying;
