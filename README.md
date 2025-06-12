# Originate (README)

**Originate** is an open source solution created by the Cardano Foundation to prove the origin of wine bottles on the Cardano blockchain. It anchors key supply-chain events on chain and exposes simple application interfaces so vineyards, distributors, regulators, and consumers can verify a bottle’s journey. By combining decentralized identifiers, richly structured metadata, and the Cardano UTXO ledger, Originate delivers a transparent, tamper evident provenance layer that any wine producer, or developer, can adopt and extend.

> note: ⚠️ The local development environment currently supports Linux and macOS only — Windows support is coming soon.

### Cardano

Cardano is a decentralized, immutable blockchain whose design lets certificates, supply-chain events, and digital signatures be stored as machine-readable metadata in a single, tamper-proof transaction. The network is stewarded by the Cardano Foundation, an independent Swiss not-for-profit that advances Cardano as public digital infrastructure across multiple industries.

## Modules

#### API Module

Spring Boot service that exposes REST and WebSocket endpoints to manage all provenance operations—register producers, mint bottle assets, append shipping or quality events, and query history. It stores off-chain data in PostgreSQL database, enforces role-based access, and forwards signed Cardano jobs to Metabus.

#### Frontend Module

React + Material UI dashboard. Users can create product batches using their browser wallets.

#### Metabus Module

Lightweight transaction manager that turns any piece of business data into a tamper-evident Cardano record while shielding your application from blockchain complexity. Incoming events arrive through a simple REST API; Metabus stores the full JSON payload in object storage, produces a content-addressable CID, places a job on Kafka, picks an unspent UTXO, builds and batches the corresponding metadata transaction, submits it through a local node, watches the chain for confirmation, and finally returns the result to your service over RabbitMQ.

#### Mobile App Module

Ionic/Capacitor hybrid app (installable PWA and native wrapper)

## Dependencies

| **Program** | **URL** | **Version** | **Support** |
| ---| ---| ---| --- |
| Apache Maven | [https://maven.apache.org/](https://maven.apache.org/) |  | [**macOS**](https://maven.apache.org/install.html): Homebrew, SDKMAN! and MacPorts<br>[**Windows**](https://maven.apache.org/install.html): Chocolatey, Scoop |
| Java SDK | [https://adoptium.net/installation/](https://adoptium.net/installation/) | 21 |  |
| Git | [https://git-scm.com/](https://git-scm.com/) |  |  |
| NVM for Node JS | [https://github.com/nvm-sh/nvm](https://github.com/nvm-sh/nvm) | Node JS 18 |  |
| Docker | [https://docs.docker.com/desktop/](https://docs.docker.com/desktop/) |  |  |
| Apache Kafka | [https://kafka.apache.org/](https://kafka.apache.org/) |  |  |
| Postman | [https://www.postman.com/downloads/](https://www.postman.com/downloads/) |  |  |

### Step 1: Open WSL or your terminal and navigate to your working folder

```plain
cd /mnt/c/Users/originate
```

### Step 2: Clone the repository using HTTPS (no SSH key needed)

```plain
git clone https://github.com/cardano-foundation/originate.git
```

### or Clone the repository using SSH

```plain
git clone git@github.com:cardano-foundation/originate.git
```


### Step 3: Build the Java backend (Spring Boot API)

_We recommend using Java 21 to avoid compatibility issues._

If this is your first time, make sure the Maven wrapper is executable:

Then build the app:

```plain
./mvn clean package
```

### Step 4: Create the .env.dev file

First in the terminal, in the same file path than before: >> /mnt/c/Users/originate/originate-agl-oss

```plain
$ touch .env.dev
```

```plain
$ nano .env.dev
```

Add the environment file that you need based on the environment that you are working

> Local: [.env.dev](.env.dev)  
> Staging: [.env.staging file](.env.staging)  
> Production: [.env.prod file](.env.prod)

### Step 5: Build the project using Docker

> Remember to check the requirements before continuing. You’ll need Docker if you want to proceed this way. Make sure the Docker instance is running.

#### Navigate into **metabus folder**: /mnt/c/Users/originate/originate-agl-oss/metabus

```plain
sudo usermod -aG docker $USER
restart session in wsl
docker compose --env-file ../.env.dev -f docker-compose-local.yml up -d
```

Go back to the main route:

```plain
>> /mnt/c/Users/originate/originate-agl-oss
$ docker compose --env-file .env.dev up -d 
```

If everything works properly we are going to see in our terminal something like this:

api **\-------------------** Built
frontend **\-------------** Built Container 
db **\---------** Healthy Container 
frontend **\---** Started
Container api **\---------** Started

> For some reason, your Kafka instance is down. You need to restart both Kafka and Zookeeper. If you get the error “Cluster ID doesn’t match,” you can fix it with the command below and then restart the services again.

**Windows:**

```plain
$ sudo echo > /var/lib/docker/volumes/metabus_kafka-data/_data/meta.properties
```

**MacOS:**

```plain
brew services restart kafka
brew services restart zookeeper
```

### Post deployment

*   Detailed post-deployment instructions are provided [here](docs/post-deployment-steps.md).

## Dev environment cleanup

To reset the development environment for fresh demos, follow these steps. They do not clear the Metabus database, because the existing data there does not affect users.

1\. Clear relevant database tables:

```sql
docker exec -it db psql -U proofoforiginadmin -d cf_proof_of_origin
truncate table winery cascade;
```

2\. All users except a default admin (e.g. [`root@admin.com`](mailto:root@admin.com)) can be bulk deleted from the [user dashboard](https://dev.auth.cf-bolnisi-wine-preprod.originate.company.com/admin/master/console/#/BolnisiPilotApplication/users).

3\. Relevant QR codes can be reset on Scantrust using the API. UAT token available from Scantrust dashboard.

```plain
curl -X POST -H "Authorization: UAT <uat-token>" -H "Content-Type: application/json" -d @docs/resetScantrust.json https://api.staging.scantrust.io/api/v2/scm/upload/
```

###
