# Missing Post Deployment Steps and Documentation

## Keycloak

- **BOLNISI_KEYCLOAK_API_SECRET:**  
  Should be the value mentioned in the post-deployment steps: the `manage_bolnisi_users` client secret.

- **METABUS_KEYCLOAK_CLIENT_SECRET:**  
  Not mentioned at all. This is the client secret from the client `"BOLNISI_PILOT_APPLICATION"` in the realm `"Metabus"`.

- **Using Keycloak on localhost:**  
  - When `KEYCLOAK_HOST` is set to `localhost`, the backend API cannot obtain the token to call Metabus from the container.
  - The workaround is to use `http://keycloak:8080`.
  - However, this breaks verification since `KEYCLOAK_HOST` is also used to construct the `issuer-uri` in API and Metabus-API configuration files.  
    The `issuer-uri` ends up being `localhost` rather than the expected Keycloak URI, so it must be updated accordingly.

## Missing Docs

- Missing description of the overall flow and the different roles/actions.
- Missing documentation on connecting the app, blockchain, and Scantrust data.
- Missing examples/documentation for NWA role actions (beyond Postman requests to obtain keys), such as creating a certificate.
- Missing examples on how to construct the signature for actions that require it.
- Missing documentation on the `app_terms` and `web_terms` Keycloak user attributes.

## Others

- **Flyway Migration for Metabus-API:**  
  There is an error with Flyway migration. The solution is to set `mixed` to `true` in the `metabus-api` `application-dev.yaml`:
  
  ```yaml
  flyway:
      mixed: true
    ```
- **Docker files:**
  The Docker files need to be updated to use the latest `cardano-node`, otherwise node won't fully syncs.
