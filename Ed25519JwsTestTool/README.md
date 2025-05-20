# Test tools for verify JWS using Ed25519

This project is used to verify JWS signatures generated with Ed25519.

## How to build?

For building you need
- [Apache Maven](https://maven.apache.org/)
- [Java SDK](https://www.oracle.com/java/technologies/downloads/)

### Building from source
```console
$ mvn clean package
```

## How to run?

```console
$ java -jar Ed25519JwsTestTool-1.0-jar-with-dependencies.jar
```

usage: Ed25519JwsTestTool
 -h,--header <arg>               Header string in Base64Url (required)
 -help,--help                    Show help
 -p64,--payloadBase64Url <arg>   Payload string in Base64Url (required if
                                 -pjson is not specify)
 -pjson,--payloadJson <arg>      Payload string in Json (required if -p64
                                 is not specify)
 -pk,--publicKey <arg>           Public key string in Base64Url (required)
 -s,--signature <arg>            Signature string (required)

### Sample request
```console
$ java -jar Ed25519JwsTestTool.jar -h eyJraWQiOiI4OTY0YTBjZS1kZjAxLTRmN2QtYTk1YS0yOTAyMTk3MTJmMDEiLCJhbGciOiJFZERTQSJ9 -p64 eyJib3R0bGluZ19kYXRlIjoiMjAyMS0wMy0yMSIsImJvdHRsaW5nX2xhdCI6NDMuMTIzMjAxNiwiYm90dGxpbmdfbG9jYXRpb24iOiJib3R0bGluZ19sb2NhdGlvbiIsImJvdHRsaW5nX2xvbmdpdHVkZSI6NDMuODk2MzcsImRlbGl2ZXJ5X2RhdGUiOiIyMDIxLTAzLTI2IiwiZmVybWVudGF0aW9uX2RhdGUiOiIyMDIwLTExLTAzIiwiZmVybWVudGF0aW9uX2xhdCI6NDIuMTIzMjAxNiwiZmVybWVudGF0aW9uX2xvY2F0aW9uIjoiZmVybWVudGF0aW9uX2xvY2F0aW9uIiwiZmVybWVudGF0aW9uX2xvbmciOjQyLjg5NjM3LCJoYXJ2ZXN0X2RhdGUiOiIyMDIwLTEwLTA4IiwiaGFydmVzdF9sYXQiOjQwLjEyMzc3NiwiaGFydmVzdF9sb2NhdGlvbiI6ImhhcnZlc3RfbG9jYXRpb24iLCJoYXJ2ZXN0X2xvbmciOjQwLjg5NjI5NzUsImludGVuZGVkX21hcmtldCI6ImludGVuZGVkX21hcmtldCIsImxvdF9udW1iZXIiOiIxMjM0NTY3ODkwMSIsInByb2Nlc3NpbmdfZGF0ZSI6IjIwMjAtMTAtMDkiLCJwcm9jZXNzaW5nX2xhdCI6NDEuMTIzMjAxNiwicHJvY2Vzc2luZ19sb2NhdGlvbiI6InByb2Nlc3NpbmdfbG9jYXRpb24iLCJwcm9jZXNzaW5nX2xvbmciOjQxLjg5NjM3LCJzaGlwbWVudF9sYXRpdHVkZSI6NDUuNDY0MjcsInNoaXBtZW50X2xvbmdpdHVkZSI6OS4xODk1MSwidG90YWxfYm90dGxlcyI6MSwid2luZV9jZWxsYXJfbmFtZSI6ImNlbGxhcl9uYW1lIn0 -s vgfD_yIoafUuGSzaNO2pvum93PUX8ISz2Qspf9Krz18aZ41aB7wkq1ak7jQVFMkC5E3ZbaTMWJ2IYcb9oOY6Ag -pk dLFBiIcuKYrWN2gfa8kHOgYVmvg0gZyhV7e0KV1X6Cs
```

```console
$ java -jar Ed25519JwsTestTool.jar -h eyJraWQiOiI4OTY0YTBjZS1kZjAxLTRmN2QtYTk1YS0yOTAyMTk3MTJmMDEiLCJhbGciOiJFZERTQSJ9 -pjson "{\"bottling_date\":\"2021-03-21\",\"bottling_lat\":43.1232016,\"bottling_location\":\"bottling_location\",\"bottling_longitude\":43.89637,\"delivery_date\":\"2021-03-26\",\"fermentation_date\":\"2020-11-03\",\"fermentation_lat\":42.1232016,\"fermentation_location\":\"fermentation_location\",\"fermentation_long\":42.89637,\"harvest_date\":\"2020-10-08\",\"harvest_lat\":40.123776,\"harvest_location\":\"harvest_location\",\"harvest_long\":40.8962975,\"intended_market\":\"intended_market\",\"lot_number\":\"12345678901\",\"processing_date\":\"2020-10-09\",\"processing_lat\":41.1232016,\"processing_location\":\"processing_location\",\"processing_long\":41.89637,\"shipment_latitude\":45.46427,\"shipment_longitude\":9.18951,\"total_bottles\":1,\"wine_cellar_name\":\"cellar_name\"}" -s vgfD_yIoafUuGSzaNO2pvum93PUX8ISz2Qspf9Krz18aZ41aB7wkq1ak7jQVFMkC5E3ZbaTMWJ2IYcb9oOY6Ag -pk dLFBiIcuKYrWN2gfa8kHOgYVmvg0gZyhV7e0KV1X6Cs
```
