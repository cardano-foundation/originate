# post-deployment-steps.md

## Post Deployment

Post-deployment relies on Keycloak an open-source identity and access-management server that provides single-sign-on (SSO), user federation, role-based access control, and OIDC/OAuth 2.0 tokens for your applications.

> Note: The checklist below is still manual. A scripted solution is planned, but for now you’ll need to complete these Keycloak settings once per environment (dev / staging / prod).  
>   
> Note: The local development environment currently supports Linux and macOS only — Windows support is coming soon.

### Keycloak Manual Configuration Guide

This guide walks you through the essential manual setup steps for Keycloak, including client configuration, SMTP setup, and theme settings.

⚠️ Don't forget to click on **"SAVE"** every time you make a change

#### 1\. Login to Keycloak Admin Console

Open your browser and go to your **KEYCLOAK\_HOST**. Click **Administration Console** and enter:

> You can find the KEYCLOAK\_HOST in the .env.dev. If you are using our local environment; navigate to [http://localhost:8881/](http://localhost:8881/) and click Administration Console

```plain
Username: cardano-admin
Password: Cardano@12345
```

> ⚠️ Make sure to select the **OriginatePilotApplicationrealm** from the dropdown in the top-left corner.

#### 2\. Configure _frontend\_dashboard_ Client

Navigate to **Clients** in the left sidebar and select the client named **_frontend\_dashboard_**. Then update the: _Home URL,_ _validRedirect URIs,_ _Valid Post Logout Redirect URIs, Web Origins_

> **The Home URL, Valid Redirect URIs, Valid Post Logout Redirect and Web Originis** should match with the ones that you have assign in your .env file

```plain
Home URL: http://localhost:3010/login
Valid Redirect URIs: http://localhost:3010/*
Valid Post Logout Redirect URIs: http://localhost:3010/*
Web Origins: http://localhost:3010
```

Then scroll down to **Capabilities** make sure the **Standard Flow** and **Direct Access Grants checkboxes are selected.** Scroll down to **Login Settings** check the login-theme should be: originate-keycloak-web. Ensure **Consent Required** and **Display on Consent Screen toggles are off.**

### 3\. Update _Secrets Key_ for Client

Go back to **Clients** and now select **manage\_originate\_users.** Then click on **Credentials** and "regenerate" a new client secret. Then copy and paste this value into your **.env** for the following variables:

`METABUS_KEYCLOAK_CLIENT_SECRET=your-new-secret`

`ORIGINATE_KEYCLOAK_API_SECRET=your-new-secret`

### 4\. Create an admin user

Go back the the hamburger menu and on **Users,** and select on **Add User**

```yaml
Email: root@admin.com
Email Verified: Yes
```

And click on **Save,** navigate to **Credentials** and add a **click** on **Set a Password**

```plain
Password: Password1!
Confirmation Password: Password1!
Temporary: Off 
```

Now in the tabs next to **Credentials** click on **Role mapping** and click on **Assign a** **Role**, here assign the **ADMIN** role to this user by selecting the checkbox, select assign.

Once we finish this process, we have everything set to using the project using the frontend, but since we add new keys into out .env file we need to restart it.

### 5\. **Editing /etc/hosts on Linux/macOS**

Add the entries in your system’s hosts file. In your terminal write:

```ruby
>> sudo vi /etc/hosts
```

Press key "A" to make to edit your file and move into the file using the arrows, next to this line:

```plain
127.0.0.1   localhost keycloak
```

Save and exit. Press key "Escape" and then write **:wq!** and press **Enter**

### 6\. Restart the project

Go back to your terminal and navigate into /metabus:

```plain
>> /mnt/c/Users/originate/originate-agl-oss/metabus
$  docker compose --env-file ../.env.dev -f docker-compose-local.yml up -d
```

Go back to the main route:

```plain
>> /mnt/c/Users/originate/originate-agl-oss
$ docker compose --env-file .env.dev up -d --build
```

### 7\. Login using the Frontend

Navigate into [http://localhost:3010](http://localhost:3010/) and login using the admin user that you just created.

## Creating NWA Token

To create, update, and retrieve specific data by ID, you need to create an NWA token. To achieve this, first generate a Secret Key

### 1\. Generate a Secret Key for NWA

Go back to **Keycloak.** Select the OriginateRealm, click on **Clients** and now select **nwa\_certificate.** Then click on **Credentials** and regenerate to create a new client secret. Then copy and paste this value for **client\_secret variable** that is in the **body tab -** **Postman Collection or the REST Client** of your preference:

[Postman Collection](https://.postman.co/workspace/My-Workspace~24086f02-d731-4a61-8835-800b42485958/request/17639607-64e2fb06-7076-4baf-a763-82adcb78ce23?action=share&creator=17639607&ctx=documentation&active-environment=17639607-d1ea47b5-e06f-4e73-b3c2-e5df69bd4d99)

```plain
URL: http://keycloak:8080/ealms/OriginatePilotApplication/protocol/openid-connect/token
```

## Configure SMTP and Email on Keycloak

In the dropdown where we selected now **OriginatePilotApplicationrealm** switch to **_Master._** Setup the email for the admin: Go to **_Users,_** click on **_cardano-admin,_** add **_new email_** and check it as **_verified._**

Now, click on **_Realm Settings_** click on tab **Email** and update the following fields: _From Email, From Display Name, Reply-To Email, Reply-To Display Name_

And last step, is set the **_SMTP Configuration_**

> ⚠️ _We suggest to use_ [mailtrap.io](http://mailtrap.io/) as SMTP provider

If you decided to use **_mailtrap_**, create an account in _mailtrap_ and then update the following fields:

**`Host:`** [`sandbox.smtp.mailtrap.io`](http://sandbox.smtp.mailtrap.io/)

**`Port:`** `587`

**`Encryption:`** `StartTLS`

**`Auth Enabled:`** `Yes`

**`Username/Password:`** `Provided by Mailtrap or your SMTP provider`

### Additional Realm Settings

Update the Theme, Localization, and Security Defenses on the option Realm Settings:

**Theme:**

**`Login Theme:`** `originate-keycloak-web`

**`Email Theme:`** `originate-keycloak`

**Localization:**

**`Internationalization: Enable`**

**Security Defenses,** set **Content-Security-Policy:**

```cs
default-src 'self' 'unsafe-eval' 'unsafe-inline'; object-src 'none'
```

## Config variable guide

### originate api section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| SPRING\_PROFILES\_ACTIVE | api active profile | dev |  |
| DB\_USER\_NAME | api database user name | proofoforiginadmin |  |
| DB\_USER\_SECRET | api database password | originateDoNotUseInProduction |  |
| API\_DB\_NAME | api database name | cf\_proof\_of\_origin |  |
| SERVICE\_USER\_NAME | api service user name | proofoforiginservice |  |
| SERVICE\_USER\_SECRET | api service user secret | originateDoNotUseInProduction |  |
| API\_EXPOSED\_PORT | api container docker port | 8080 |  |
| API\_LOCAL\_BIND\_PORT | api bind to host at port | 8081 |  |
| ORIGINATE\_KEYCLOAK\_API\_SECRET | api secret to call to Originate api | "\*\*\*\*\*\*\*\*\*\*" | "bH57KBQipsX53FCxpLRgK5wdpFsJtZlp" |
| SPRING\_EXECUTION\_POOL\_CORE\_SIZE |  | 5 |  |
| SPRING\_EXECUTION\_POOL\_MAX\_SIZE |  | 10 |  |
| SPRING\_EXECUTION\_THREAD\_NAME\_PREFIX |  | Async- |  |
| METABUS\_KEYCLOAK\_CLIENT\_SECRET | api secret for Originate to call to Metabus | "\*\*\*\*\*\*\*\*\*\*" | "bH57KBQipsX53FCxpLRgK5wdpFsJtZlp" |
| ENCRYPT\_PASSWORD | encrypt password when winery approve scm data | thisisarandompassword |  |
| FRONTEND\_LOGIN\_PATH | FE login path | /login |  |
| FRONTEND\_DOMAIN\_HOST | FE domain host (domain only) | [poo-frontend-dev.sotatek.works](http://poo-frontend-dev.sotatek.works) | localhost |
| FRONTEND\_DOMAIN\_PUBLIC\_URL | FE domain public url (with http scheme) | [https://poo-frontend-dev.sotatek.works](https://poo-frontend-dev.sotatek.works/) | [https://localhost](https://localhost/) |
| EMAIL\_USER\_NAME | Email username for sending reset password email | [buik32368@gmail.com](mailto:buik32368@gmail.com) | [cf-originate@mail.test](mailto:cf-originate@mail.test) |
| EMAIL\_PASSWORD | Email password for sending reset password email | ltprdacdfbnvyprg | cf-originate-mail-password |
| EMAIL\_FORM | Email from for sending reset password email | [buik32368@gmail.com](mailto:buik32368@gmail.com) | [cf-originate@mail.test](mailto:cf-originate@mail.test) |
| ORIGINATE\_BACKEND\_HOST | Originate api host | [poo-api-dev.sotatek.works](http://poo-api-dev.sotatek.works) | localhost |
| ORIGINATE\_BACKEND\_API | Originate api port | [https://poo-api-dev.sotatek.works](https://poo-api-dev.sotatek.works/) | [https://localhost](https://localhost/) |
| MOBILE\_IOS\_SCANNINGAPP\_DOMAIN | Mobile scanning app domain for IOS | capacitor://localhost |  |
| MOBILE\_ANDROID\_SCANNINGAPP\_DOMAIN | Mobile scanning app domain for Android | [http://localhost](http://localhost/) |  |

### originate db section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| ORIGINATE\_DB\_CONTAINER\_NAME | database container name | db | db |
| DB\_IMAGE\_NAME | database image name | postgres |  |
| DB\_IMAGE\_TAG | database image tag | 15.3 | 15.3 |
| DB\_LOCAL\_BIND\_PORT | database container docker port | 54321 | 54321 |
| DB\_EXPOSED\_PORT | database bind to host at port | 5432 | 5432 |

### state storage db section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| STATE\_DB\_IMAGE\_NAME | Metabus state storage database image name | postgres |  |
| STATE\_DB\_IMAGE\_TAG | Metabus state storage database image tag | 15.3 | 15.3 |
| STATE\_DB\_LOCAL\_BIND\_PORT | Metabus state storage database container docker port | 54324 | 54324 |
| STATE\_DB\_EXPOSED\_PORT | Metabus state storage database container bind to host at port | 5432 | 5432 |
| STATE\_DB\_USER\_NAME | Metabus state storage database user\_name | proofoforiginadmin | proofoforiginadmin |
| STATE\_DB\_USER\_SECRET | Metabus state storage database user\_secret | originateDoNotUseInProduction | originateDoNotUseInProduction |
| STATE\_DB\_NAME | Metabus state storage database name | state\_storage | state\_storage |
| STATE\_DB\_HOST | Metabus state storage database host | state-storage | state-storage |

### originate web section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| FRONTEND\_LOCAL\_BIND\_PORT | FE container docker port | 3001 | 3001 |
| FRONTEND\_EXPOSED\_PORT | FE container bind to host at port | 80 | 80 |

### metabus producer section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| JOB\_PRODUCER\_PORT | Job producer container docker port | 8071 | 8071 |
| JOB\_PRODUCER\_LOG | Job producer log level | TRACE |  |

### metabus api section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| METABUS\_API\_EXPOSED\_PORT | Metabus api container docker port | 8080 | 8080 |
| METABUS\_API\_LOCAL\_BIND\_PORT | Metabus api container bind to host at port | 8084 | 8084 |
| METABUS\_API\_JOB\_PRODUCER\_HOST | Metabus job producer host (with port) | [http://metabus-jobproducer:8080](http://metabus-jobproducer:8080/) | [http://metabus-jobproducer:8080](http://metabus-jobproducer:8080/) |
| METABUS\_API\_HOST | Metabus api host (without http scheme) | [http://metabus-api](http://metabus-api/) | [http://metabus-api](http://metabus-api/) |

### metabus cardano-node section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| CARDANO\_NODE\_IMAGE\_NAME | Cardano node image name | inputoutput/cardano-node |  |
| CARDANO\_NODE\_IMAGE\_TAG | Cardano node image tag | 1.35.7 | 1.35.7 |
| CARDANO\_NODE\_SOCKET\_PATH | Cardano node socket path | /ipc/node.socket | /ipc/node.socket |
| CARDANO\_NODE\_NETWORK | Cardano node network, user proprod for dev/staging or mainnet for production | preprod | preprod |
| CARDANO\_NODE\_LOCAL\_BIND\_PORT | Cardano node container docker port | 3011 | 3011 |
| CARDANO\_NODE\_EXPOSED\_PORT | Cardano node container bind to host at port | 3001 | 3001 |

### metabus txsubmitter section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| METABUS\_TXSUBMITTER\_PORT | Metabus txsubmitter container docker port | 8088 | 8088 |
| METABUS\_TXSUBMITTER\_LOG | Metabus txsubmitter log level | TRACE |  |
| METABUS\_TXSUBMITTER\_WALLET\_MNEMONIC | Metabus txsubmitter wallet mnemonic for paying fee when submit to blockchain | kit color <... hidden 21 words> era | kit color <... hidden 21 words> era |
| METABUS\_TXSUBMITTER\_METADATUM\_LABEL | Metabus txsubmitter metadatum label | 1904 | 1904 |
| METABUS\_TXSUBMITTER\_FIXED\_TX\_OUT | Metabus txsubmitter fixed tx out | 1000000 |  |
| METABUS\_TXSUBMITTER\_PENDING\_JOB\_BOUNDARY\_TIME | Metabus txsubmitter pending job boundary time (in millisecond) | 60000 | 60000 |
| METABUS\_TXSUBMITTER\_CONSUME\_BASE\_ON\_TIME\_INTERVAL | Metabus txsubmitter consume base on time interval (in millisecond) | 60000 | 60000 |
| METABUS\_TXSUBMITTER\_NUMBER\_OF\_ADDRESSES | Metabus txsubmitter number of addresses in above wallet | 10 | 10 |
| METABUS\_TXSUBMITTER\_WAITING\_TIME\_TO\_RECONSUME | Metabus txsubmitter waiting time to reconsume (in millisecond) | 30000 | 30000 |
| METABUS\_TXSUBMITTER\_NUMBER\_OF\_RETRY\_PULLING\_UTXO | Metabus txsubmitter number of retry pulling utxo | 1 | 1 |
| METABUS\_TXSUBMITTER\_OFFCHAIN\_BUCKET | Metabus txsubmitter offchain bucket | georgian-wine | georgian-wine |
| METABUS\_TXSUBMITTER\_TX\_SUBMISSION\_RETRY\_DELAY\_DURATION | Metabus txsubmitter retry delay duration (in millisecond) | 500 |  |
| METADATA\_VERSION | Metabus txsubmitter metadata version | 1 |  |

### metabus tx-watcher section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| TX\_WATCHER\_ROLLBACK\_THRESHOLD |  | 8 | 8 |
| TX\_WATCHER\_SERVER\_PORT |  | 8089 | 8089 |
| TX\_WATCHER\_DELAY\_RECONSUME\_DURATION |  | 3000 | 3000 |

### metabus txwatcher (ledger sync) section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| LEDGERSYNC\_CRAWLER\_PORT |  | 8091 | 8091 |
| LEDGERSYNC\_CRAWLER\_SPRING\_PROFILES\_ACTIVE |  | n2n,sentinel,kafka |  |
| TASK\_SCHEDULER\_POOL\_SIZE |  | 1 |  |
| TASK\_SCHEDULER\_THREAD\_POOL\_NAME |  | txSubmitterPool |  |
| TASK\_SCHEDULER\_THREAD\_PREFIX\_NAME |  | txSubmitterThread- |  |
| LEDGERSYNC\_CRAWLER\_CRAWLER\_NAME | Crawler crawler name | cardano-ledgersync-crawler |  |
| KAFKA\_BOOTSTRAP\_SERVER | Kafka bootstrap server | kafka:9092 | kafka:9092 |
| LEDGERSYNC\_CRAWLER\_BLOCKS\_TOPIC | Crawler blocks topic | local.crawler.blocks |  |
| LEDGERSYNC\_CRAWLER\_ENVIRONMENT | Crawler environment, preprod for dev/statging or mainnet for production | preprod | preprod |
| LEDGERSYNC\_CRAWLER\_NETWORK\_MAGIC | Crawler network magic | 1 |  |
| LEDGERSYNC\_CRAWLER\_NODE\_ADDRESS | Crawler node address | metabus-cardano-node |  |
| LEDGERSYNC\_CRAWLER\_NODE\_PORT | Crawler node port | 3001 | 3001 |
| LEDGERSYNC\_CRAWLER\_REDIS\_INITIAL\_CHECKPOINT | Crawler redis initial checkpoint | cardano-ledgersync-crawler\_PREPROD\_LATEST\_TIP |  |
| LEDGERSYNC\_CRAWLER\_REDIS\_INITIAL\_CHECKPOINT\_SLOT | Crawler redis initial checkpoint\_slot | 29884014 | 29884014 |
| LEDGERSYNC\_CRAWLER\_REDIS\_INITIAL\_CHECKPOINT\_HASH | Crawler redis initial checkpoint\_hash | 4cfdb7b63521fa6<...hidden...>8a2c82fea85925 |  |
| LEDGERSYNC\_CRAWLER\_REDIS\_INITIAL\_CHECKPOINT\_BLOCK | Crawler redis initial checkpoint block | 1000018 | 1000018 |
| LEDGERSYNC\_CRAWLER\_REDIS\_MASTER\_NAME | Crawler redis master name | mymaster | mymaster |
| LEDGERSYNC\_CRAWLER\_REDIS\_SENTINEL\_PASS | Crawler redis sentinel pass | redis\_sentinel\_pass |  |
| LEDGERSYNC\_CRAWLER\_REDIS\_SENTINEL\_HOST | Crawler redis sentinel host | cardano.redis.sentinel |  |
| LEDGERSYNC\_CRAWLER\_REDIS\_SENTINEL\_PORT | Crawler redis sentinel port | 26379 | 26379 |
| LEDGERSYNC\_CRAWLER\_REDIS\_MASTER\_PASS | Crawler redis master pass | redis\_master\_pass |  |
| LEDGERSYNC\_CRAWLER\_REDIS\_SLAVE\_PASS | Crawler redis slave pass | redis\_slave\_pass |  |
|  |  |  |  |

### keycloak section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| PG4KEYCLOAK\_IMAGE\_NAME | Pg4keycloak image name | postgres |  |
| PG4KEYCLOAK\_IMAGE\_TAG | Pg4keycloak image tag | 15.3 | 15.3 |
| PG4KEYCLOAK\_CONTAINER\_NAME | Pg4keycloak container name | pg4keycloak | pg4keycloak |
| PG4KEYCLOAK\_LOCAL\_BIND\_PORT | Pg4keycloak container docker port | 54327 | 54327 |
| PG4KEYCLOAK\_EXPOSED\_PORT | Pg4keycloak container bind to host at port | 5432 | 5432 |
| PG4KEYCLOAK\_USER | Pg4keycloak user | keycloak-master | keycloak-master |
| PG4KEYCLOAK\_PASSWORD | Pg4keycloak password | Cardano@12345 | Cardano@12345 |
| PG4KEYCLOAK\_DATABASE | Pg4keycloak database | keycloak | keycloak |
| KEYCLOAK\_IMAGE\_NAME | Keycloak image name | [quay.io/keycloak/keycloak](http://quay.io/keycloak/keycloak) |  |
| KEYCLOAK\_IMAGE\_TAG | Keycloak image tag | 21.1.1 | 21.1.1 |
| KEYCLOAK\_CONTAINER\_NAME | Keycloak container name | keycloak | keycloak |
| KEYCLOAK\_LOCAL\_BIND\_PORT | Keycloak local bind port | 8881 | 8881 |
| KEYCLOAK\_EXPOSED\_PORT | Keycloak exposed port | 8080 | 8080 |
| KEYCLOAK\_LOCAL\_BIND\_DEBUG\_PORT | Keycloak local bind debug port | 8787 | 8787 |
| KEYCLOAK\_EXPOSED\_DEBUG\_PORT | Keycloak exposed debug port | 8787 | 8787 |
| KEYCLOAK\_DB\_VENDOR | Keycloak db vendor | postgres | postgres |
| KEYCLOAK\_KEYCLOAK\_USER | Keycloak keycloak user | cardano-admin | cardano-admin |
| KEYCLOAK\_KEYCLOAK\_PASSWORD | Keycloak keycloak password | Cardano@12345 | Cardano@12345 |
| KEYCLOAK\_START\_TYPE | Keycloak start type, "start-dev" for dev or "start" for staging/production | start-dev | start-dev |
| KC\_HOSTNAME | Keycloak hostname (without http scheme) | [poo-keycloak-dev.sotatek.works](http://poo-keycloak-dev.sotatek.works) | [poo-keycloak-dev.sotatek.works](http://poo-keycloak-dev.sotatek.works) |
| KEYCLOAK\_HOST | Keycloak host (with http scheme) | [https://poo-keycloak-dev.sotatek.works](https://poo-keycloak-dev.sotatek.works/) | [https://localhost:8881](https://localhost:8881/) |
| KEYCLOAK\_REALM\_NAME | Keycloak realm\_name | OriginatePilotApplication | OriginatePilotApplication |
| KC\_PROXY | Keycloak proxy | edge |  |

### kafka section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| TOPIC\_RETRY\_JOB | Topic retry job | dev.job.retry |  |
| TOPIC\_SCHEDULE\_JOB | Topic schedule job | dev.job.schedule |  |
| TOPIC\_DEAD\_LETTER | Topic dead letter | dev.dead.letter |  |

### mobile app

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| MOBILE\_APP\_KEYCLOAK\_CLIENT\_ID | Mobile app keycloak client id | mobile-scan-app |  |
| MOBILE\_APP\_KEYCLOAK\_REDIRECT\_URI | Mobile app keycloak redirect uri | cfproofoforigin://login |  |
| SCANTRUST\_SCAN\_URL | Scantrust scan url | [https://nwxn.qr1.ch/](https://nwxn.qr1.ch/) |  |
| MOBILE\_APP\_HOST\_NAME | Mobile app host name | localhost |  |

### web app

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| ENV\_FILE | Env file | .env.dev |  |
| KEY\_CLOAK\_CLIENT\_CLIENT\_ID | Key cloak client client id | "frontend\_dashboard" |  |
| REACT\_APP\_REDIRECT\_KEY\_CLOAK\_LOGOUT\_AUTHORIZE\_QUERY | React app redirect key cloak logout authorize query | "logoutCode=401" |  |

### metabus offchain-storage section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| METABUS\_OFFCHAIN\_CONTAINER\_NAME | Offchain storage container name | metabus-offchain-storage |  |
| METABUS\_OFFCHAIN\_EXPOSED\_PORT | Offchain storage container docker port | 8080 | 8080 |
| METABUS\_OFFCHAIN\_LOCAL\_BIND\_PORT | Offchain storage container bind to host at port | 8061 | 8061 |
| METABUS\_OFFCHAIN\_HOSTNAME | Offchain storage hostname | [poo-offchain-dev.sotatek.works](http://poo-offchain-dev.sotatek.works) | localhost:8061 |
| METABUS\_OFFCHAIN\_MINIO\_ENDPOINT | Offchain storage minio endpoint for post to save offchain data | [http://minio](http://minio/) | [http://minio](http://minio/) |
| METABUS\_OFFCHAIN\_MINIO\_SECURE | Offchain storage minio secure | false |  |
| METABUS\_OFFCHAIN\_MINIO\_HOST | Offchain storage minio host for get public data (without http scheme) | [poo-minio-dev.sotatek.works](http://poo-minio-dev.sotatek.works) | localhost:9300 |
| METABUS\_OFFCHAIN\_MINIO\_PUBLIC\_ENDPOINT | Offchain storage minio public endpoint for get public data (http scheme) | [https://$](https://$){METABUS\_OFFCHAIN\_MINIO\_HOST} |  |
| METABUS\_OFFCHAIN\_MINIO\_BUCKET\_NAME | Offchain storage minio bucket name | commons |  |
| METABUS\_OFFCHAIN\_MINIO\_IMAGE\_SIZE | Offchain storage minio image size | 10485760 |  |
| METABUS\_OFFCHAIN\_MINIO\_FILE\_SIZE | Offchain storage minio file size | 1073741824 |  |
| METABUS\_OFFCHAIN\_OBJECT\_URL\_EXPIRY | Offchain storage object url expiry | 2 |  |
| METABUS\_TXSUBMITTER\_OFFCHAIN\_HOST | Metabus txsubmitter offchain host (for metabus txsubmitter) | [http://$](http://$){METABUS\_OFFCHAIN\_CONTAINER\_NAME}:${METABUS\_OFFCHAIN\_EXPOSED\_PORT} |  |
| MINIO\_IMAGE\_NAME | Minio image name | minio/minio |  |
| MINIO\_CONTAINER\_NAME | Minio container name | minio |  |
| MINIO\_API\_LOCAL\_BIND\_PORT | Minio api container bind to host at port | 9300 | 9300 |
| MINIO\_API\_EXPOSED\_PORT | Minio api container docker port | 9000 | 9000 |
| MINIO\_CONSOLE\_LOCAL\_BIND\_PORT | Minio console container bind to host at port | 9310 | 9310 |
| MINIO\_CONSOLE\_EXPOSED\_PORT | Minio console container docker port | 9001 | 9001 |
| MINIO\_ACCESS\_KEY | Minio access\_key | cardano-admin | cardano-admin |
| MINIO\_SECRET\_KEY | Minio secret\_key | Cardano@12345 | Cardano@12345 |

### rabbitmq section

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| RABBITMQ\_IMAGE\_NAME | Rabbitmq image name | rabbitmq |  |
| RABBITMQ\_IMAGE\_TAG | Rabbitmq image tag | 3.12.0-management | 3.12.0-management |
| RABBITMQ\_CONTAINER\_NAME | Rabbitmq container name | rabbitmq |  |
| RABBITMQ\_LOCAL\_BIND\_PORT | Rabbitmq container bind to host at port | 5672 | 5672 |
| RABBITMQ\_LOCAL\_EXPOSE\_PORT | Rabbitmq container docker port | 5672 | 5672 |
| RABBITMQ\_LOCAL\_MANAGEMENT\_BIND\_PORT | Rabbitmq local management container bind to host at port | 15672 | 15672 |
| RABBITMQ\_LOCAL\_MANAGEMENT\_EXPOSE\_PORT | Rabbitmq local management container docker port | 15672 | 15672 |
| RABBITMQ\_USER\_NAME | Rabbitmq user name | guest | guest |
| RABBITMQ\_PASSWORD | Rabbitmq password | guest | guest |
| RABBITMQ\_EXCHANGE | Rabbitmq exchange | job |  |
| RABBITMQ\_ROUTING\_KEY\_ORIGINATE | Rabbitmq routing key originate | originate |  |
| RABBITMQ\_QUEUE\_ORIGINATE | Rabbitmq queue originate | originate |  |
| RABBITMQ\_MESSAGE\_TTL\_ORIGINATE | Rabbitmq message ttl originate | 345600000 |  |
| RABBITMQ\_HAS\_DLQ\_ORIGINATE | Rabbitmq has dlq originate | true |  |
| DEAD\_LETTER\_QUEUE | Dead letter queue | originate-dead-letter-queue |  |
| DEAD\_LETTER\_EXCHANGE | Dead letter exchange | originate-dead-letter-exchange |  |
| DEAD\_LETTER\_ROUTING\_KEY | Dead letter routing\_key | originate-dead |  |

### redis

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| REDIS\_MASTER\_HOST | Redis\_master\_host | cardano-redis-master |  |

### scantrust

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| SCANTRUST\_DOMAIN | SCANTRUST\_DOMAIN | [https://api.staging.scantrust.io](https://api.staging.scantrust.io/) | [https://api.staging.scantrust.io](https://api.staging.scantrust.io/) |
| SCANTRUST\_SCM\_DATA\_ASYNC | SCANTRUST\_SCM\_DATA\_ASYNC | /api/v2/scm/upload/async/ |  |
| SCANTRUST\_SCM\_DATA\_SYNC | SCANTRUST\_SCM\_DATA\_SYNC | /api/v2/scm/upload/ |  |
| SCANTRUST\_SCM\_TASK\_STATE | SCANTRUST\_SCM\_TASK\_STATE | /api/v2/scm/tasks/ |  |
| SCANTRUST\_UAT\_TOKEN | SCANTRUST\_UAT\_TOKEN | 4ZbhPGGWchZkXwAfE6ghcda2VTvNZtGr390wHDeM | 4ZbhPGGWchZkXwAfE6ghcda2VTvNZtGr390wHDeM |
| SCANTRUST\_REPEAT\_TIMES | SCANTRUST\_REPEAT\_TIMES | 10 |  |
| SCANTRUST\_REPEAT\_INTERVAL | SCANTRUST\_REPEAT\_INTERVAL | 30 |  |
| SCANTRUST\_MIN\_BACKOFF | SCANTRUST\_MIN\_BACKOFF | 2 |  |
| SCANTRUST\_MAX\_BACKOFF | SCANTRUST\_MAX\_BACKOFF | 30 |  |

### Traefik specific

| Variable | Description | Default value | Example |
| ---| ---| ---| --- |
| INSTANCE\_ID\_HOSTNAME | Instance id hostname | [i-0872852d61ec68d6f.originate.company.com](http://i-0872852d61ec68d6f.originate.company.com) |  |
