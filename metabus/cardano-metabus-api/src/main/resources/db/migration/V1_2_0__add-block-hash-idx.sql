CREATE INDEX CONCURRENTLY IF NOT EXISTS block_hash_idx ON block (hash);

CREATE INDEX CONCURRENTLY IF NOT EXISTS block_block_no_idx ON block (block_no DESC);
