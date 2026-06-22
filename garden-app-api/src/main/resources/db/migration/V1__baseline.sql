-- ベースライン: 導入時点で Supabase 上に既に存在していたスキーマを記録するためのファイル。
-- spring.flyway.baseline-on-migrate=true により、このファイルは実行されず
-- 「既に適用済み」として扱われる（実DBへの再実行はされない）。
-- 将来DBをゼロから再構築する場合の正本として保持する。

CREATE TYPE plant_type AS ENUM ('vegetable', 'fruit', 'herb', 'flower', 'tree', 'other');

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE plants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR NOT NULL,
    type plant_type NOT NULL,
    planted_at TIMESTAMP,
    memo TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE care_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plant_id UUID REFERENCES plants(id) ON DELETE CASCADE,
    care_type VARCHAR NOT NULL,
    cared_at TIMESTAMP NOT NULL,
    memo TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
