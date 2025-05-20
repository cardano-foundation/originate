package org.example;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.util.Base64URL;
import org.apache.commons.cli.*;
import org.erdtman.jcs.JsonCanonicalizer;

public class Main {
    private static String HEADER_OPTION = "h";
    private static String PAYLOAD_OPTION = "p64";
    private static String PAYLOAD_JSON_OPTION = "pjson";
    private static String SIGNATURE_OPTION = "s";
    private static String PUBLIC_KEY_OPTION = "pk";
    private static String HELP_OPTION = "help";
    private static String HEADER_LONG_OPTION = "header";
    private static String PAYLOAD_LONG_OPTION = "payloadBase64Url";
    private static String PAYLOAD_JSON_LONG_OPTION = "payloadJson";
    private static String SIGNATURE_LONG_OPTION = "signature";
    private static String PUBLIC_KEY_LONG_OPTION = "publicKey";

    public static void main(String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();

            // Create options for the command-line arguments
            Options options = new Options();
            options.addOption(HEADER_OPTION, HEADER_LONG_OPTION, true, "Header string in Base64Url (required)");
            options.addOption(PAYLOAD_OPTION, PAYLOAD_LONG_OPTION, true, "Payload string in Base64Url (required if -pjson is not specify)");
            options.addOption(PAYLOAD_JSON_OPTION, PAYLOAD_JSON_LONG_OPTION, true, "Payload string in Json (required if -p64 is not specify)");
            options.addOption(SIGNATURE_OPTION, SIGNATURE_LONG_OPTION, true, "Signature string (required)");
            options.addOption(PUBLIC_KEY_OPTION, PUBLIC_KEY_LONG_OPTION, true, "Public key string in Base64Url (required)");
            options.addOption(HELP_OPTION, HELP_OPTION, false, "Show help");

            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption(HELP_OPTION) || cmd.getOptions().length == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Ed25519JwsTestTool", options);
            } else {
                String headerStr = cmd.getOptionValue(HEADER_OPTION);
                String payloadBase64UrlStr = cmd.getOptionValue(PAYLOAD_OPTION);
                String payloadJsonStr = cmd.getOptionValue(PAYLOAD_JSON_OPTION);
                String signatureStr = cmd.getOptionValue(SIGNATURE_OPTION);
                String publicKeyStr = cmd.getOptionValue(PUBLIC_KEY_OPTION);

                if (isNullOrBlank(headerStr)) {
                    System.out.println("Missing: -" + HEADER_OPTION);
                    return;
                }
                if (isNullOrBlank(payloadBase64UrlStr) && isNullOrBlank(payloadJsonStr)) {
                    System.out.println("Missing: -" + PAYLOAD_OPTION + " or -" + PAYLOAD_JSON_OPTION);
                    return;
                }
                if (isNullOrBlank(signatureStr)) {
                    System.out.println("Missing: -" + SIGNATURE_OPTION);
                    return;
                }
                if (isNullOrBlank(publicKeyStr)) {
                    System.out.println("Missing: -" + PUBLIC_KEY_OPTION);
                    return;
                }

                if (isNullOrBlank(payloadBase64UrlStr)) {
                    JsonCanonicalizer jc = new JsonCanonicalizer(payloadJsonStr);
                    payloadJsonStr = jc.getEncodedString();
                    payloadBase64UrlStr = Base64URL.encode(payloadJsonStr).toString();
                }

                Base64URL publicKey = new Base64URL(publicKeyStr);
                OctetKeyPair jwk = (new OctetKeyPair.Builder(Curve.Ed25519, publicKey)).build();

                JWSVerifier verifier = new Ed25519Verifier(jwk.toPublicJWK());
                JWSObject jwsObject = JWSObject.parse(headerStr + "." + payloadBase64UrlStr + "." + signatureStr);

                if (jwsObject.verify(verifier)) {
                    System.out.println("Verification successful!");
                } else {
                    System.out.println("Verification failed!");
                }
            }
        } catch (Exception e) {
            System.out.println("Verification failed! Exception: " + e.getMessage());
        }
    }

    private static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }
}