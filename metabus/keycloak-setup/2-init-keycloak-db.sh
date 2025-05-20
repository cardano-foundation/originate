#!/bin/bash

psql -U keycloak-master -tc "SELECT 1 FROM pg_database WHERE datname = 'keycloak'" | grep -q 1 || psql -U keycloak-master -c "CREATE DATABASE keycloak"

psql -U keycloak-master -c "CREATE USER keycloak WITH PASSWORD 'kc'"
