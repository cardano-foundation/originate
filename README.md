# Originate

**Originate** is an open-source traceability solution developed by the Cardano Foundation to verify product authenticity and certification integrity across complex supply chains.
Designed for real-world implementation, Originate anchors key events to the Cardano blockchain—enabling tamper-proof records and exposing modular APIs for seamless integration into enterprise and regulatory systems.
Whether you are a developer building trust-driven applications or an enterprise embedding compliance into operations, Originate provides:

* **Digital signatures** ensuring data authenticity and integrity
* **Structured provenance metadata** for certification and inspection
* **Immutable records** on the Cardano UTXO ledger
* **API interfaces** for interoperability with ERP, QA, or traceability systems

Originate is already deployed in production environments and is adaptable across industries—from regulated goods to global exports—supporting transparency, operational alignment, and consumer confidence.

> note: ⚠️ The local development environment currently supports Linux and macOS only — Windows support is coming soon.

### Cardano

Cardano is a third-generation decentralized blockchain optimized for immutability, security, and machine-readable metadata—enabling tamper-proof records of certifications, supply chain data, and digital signatures.

The Cardano Foundation advances Cardano as public digital infrastructure and empowers the architects of the future to solve problems in new ways.

## Modules

#### API Module

A robust Spring Boot service that exposes both REST endpoints to orchestrate core provenance operations. This includes:

* Registering producers and users
* Minting product assets
* Recording events (e.g., shipping, inspection, certification)
* Querying historical trace data

It enforces role-based access control, stores off-chain metadata in a PostgreSQL database, and forwards signed blockchain jobs to the Metabus module for ledger submission.

#### Frontend Module

A React application built with Material UI, this dashboard enables users to create product batches directly from their browser.

#### Metabus Module

A lightweight transaction manager that turns any piece of business data into a **tamper-evident Cardano record** while shielding your application from blockchain complexity.
Incoming events arrive through a simple REST API. Metabus:

* Places a job on Kafka for batching
* Stores the full batch JSON payload in object storage
* Produces a content-addressable CID of the batch
* Picks an unspent UTXO
* Builds the corresponding metadata transaction
* Submits it through a local node
* Watches the chain for confirmation
* Returns the result to your service over RabbitMQ

#### Mobile App Module

The Originate Mobile App is built using an **Ionic/Capacitor hybrid stack**, enabling both **installable PWAs** and **native mobile deployment** from a single codebase. 
Designed for field operability and usability at the edge of supply chains, it enables real-time interaction with traceability workflows via QR scanning, wallet integration, and event logging.

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

### Step 4: Create the .env file

First in the terminal, in the same file path than before: >> /mnt/c/Users/originate/originate-agl-oss

```plain
$ touch .env
```

```plain
$ nano .env
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
docker compose --env-file ../.env -f docker-compose-local.yml up -d
```

Go back to the main route:

```plain
>> /mnt/c/Users/originate/originate-agl-oss
$ docker compose --env-file .env up -d 
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
