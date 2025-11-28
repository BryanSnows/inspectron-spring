#!/bin/sh
set -eu

# Script de inicialização do banco de dados PostgreSQL
# Este script é executado durante a criação do container do PostgreSQL

echo "Initializing database for environment: ${ENVIRONMENT:-development}"

# Determinar o nome do banco de dados da aplicação
APP_DB_NAME="${APP_DB_NAME:-${POSTGRES_DB:-inspectron}}"

# Criar banco de dados se não existir (idempotente)
echo "Creating database '${APP_DB_NAME}' if it doesn't exist..."
psql -v ON_ERROR_STOP=1 -v DBNAME="${APP_DB_NAME}" --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    SELECT 'CREATE DATABASE ' || quote_ident(:'DBNAME')
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = :'DBNAME')\gexec
EOSQL

echo "Database creation check completed. Proceeding with configuration..."

# Criar extensões e aplicar configurações (idempotente)
psql -v ON_ERROR_STOP=1 -v DBNAME="${APP_DB_NAME}" --username "$POSTGRES_USER" --dbname "$APP_DB_NAME" <<-EOSQL
    -- Criar extensões úteis para a aplicação (no template atual)
    CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

    -- Configurações de performance
    ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
    ALTER SYSTEM SET log_statement = 'all';
    ALTER SYSTEM SET log_min_duration_statement = 1000;

    -- Configurações de checkpoint
    ALTER SYSTEM SET checkpoint_completion_target = 0.9;
    ALTER SYSTEM SET wal_buffers = '16MB';
    ALTER SYSTEM SET default_statistics_target = 100;

    SELECT pg_reload_conf();
EOSQL

echo "Database initialization completed successfully!"