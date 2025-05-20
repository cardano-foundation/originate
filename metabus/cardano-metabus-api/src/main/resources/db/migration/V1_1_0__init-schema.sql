
CREATE TABLE IF NOT EXISTS unconfirmed_txs
(
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    tx_hash character varying(64) NOT NULL,
    metadata character varying(65535),
    created_date timestamp not null default now(),
    last_updated timestamp not null default now(),
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS jobs
(
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    job_state character varying(64),
    type character varying(255),
    data character varying(65535),
    signature bytea,
    pub_key bytea,
    "group_type" character varying(255),
    "group" character varying(255),
    created_date timestamp not null default now(),
    last_updated timestamp not null default now(),
    job_index VARCHAR(32),
    sub_type VARCHAR(32),
    jws_header bytea,
    retry_count SMALLINT NOT NULL DEFAULT 5,
    PRIMARY KEY(id),
    unconfirmed_tx_id bigint,
    CONSTRAINT fk_unconfirmed_txs
        FOREIGN KEY(unconfirmed_tx_id)
            REFERENCES unconfirmed_txs(id)
);

CREATE TABLE IF NOT EXISTS utxos
(
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    address character varying(255),
    tx_hash character varying(64) NOT NULL,
    output_index int NOT NULL,
    lovelace bigint NOT NULL,
    created_date timestamp not null default now(),
    last_updated timestamp not null default now(),
    PRIMARY KEY(id),
    unconfirmed_tx_id bigint,
    CONSTRAINT fk_unconfirmed_txs
        FOREIGN KEY(unconfirmed_tx_id)
            REFERENCES unconfirmed_txs(id)
);

CREATE TABLE IF NOT EXISTS scheduled_batches
(
    id SERIAL,
    job_type CHARACTER VARYING(255) NOT NULL UNIQUE,
    batch_status SMALLINT NOT NULL,
    first_consumed_job_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    PRIMARY KEY(id)
);

create table IF NOT EXISTS block
(
    id bigint not null
        PRIMARY KEY,
    is_deleted        boolean default false,
    created_date      timestamp   not null,
    last_updated      timestamp   not null,
    hash              varchar(64) not null,
    slot_no           bigint,
    block_no          bigint,
    previous_hash     varchar(255),
    tx_onchain_hashes text
);
