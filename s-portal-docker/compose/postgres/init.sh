#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER damian WITH PASSWORD 'test' SUPERUSER LOGIN;
    CREATE DATABASE sportal;
    GRANT ALL PRIVILEGES ON DATABASE sportal TO damian;
EOSQL

#pg_restore -U "$POSTGRES_USER" -d smportal -v "/docker-entrypoint-initdb.d/sm-portal.backup"