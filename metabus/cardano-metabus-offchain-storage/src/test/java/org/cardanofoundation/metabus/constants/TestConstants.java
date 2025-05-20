package org.cardanofoundation.metabus.constants;

public class TestConstants {
    /**
     * Default bucket name.
     */
    public static final String BUCKET_NAME = "test-bucket-01";

    /**
     * Default normal data.
     */
    public static final String JSON_NORMAL = """
            {
              "from_account": "543 232 625-3",
              "to_account": "321 567 636-4",
              "amount": 500,
              "currency": "USD"
            }
            """;
    public static final String CANONICALIZED_NORMAL = """
            {"amount":500,"currency":"USD","from_account":"543 232 625-3","to_account":"321 567 636-4"}""";
    public static final String CID_NORMAL = "zCT5htkeCML2oQsCZsJzN9kzEjytQdnCpTMS9JqkjqPiA8Wducq9";
    public static final String OBJECT_URL_NORMAL = """
            http://172.0.0.1:9300/test-bucket-01/zCT5htkeCML2oQsCZsJzN9kzEjytQdnCpTMS9JqkjqPiA8Wducq9?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=cardano-admin%2F20230627%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20230627T114039Z&X-Amz-Expires=120&X-Amz-SignedHeaders=host&X-Amz-Signature=ddb7ba3bcff06bf589a8c4fe2e2b546dcf664d74ef56688e84b612bc52852909""";

    /**
     * Default crazy data.
     */
    public static final String JSON_CRAZY =
            """
                    {
                      "1": {"f": {"f":  "hi","F":  5} ,"\\n":  56.0},
                      "10": { },
                      "":  "empty",
                      "a": { },
                      "111": [ {"e":  "yes","E":  "no" } ],
                      "A": { }
                    }
                    """;
    public static final String CANONICALIZED_CRAZY = """
            {"":"empty","1":{"\\n":56,"f":{"F":5,"f":"hi"}},"10":{},"111":[{"E":"no","e":"yes"}],"A":{},"a":{}}""";
    public static final String CID_CRAZY = "zCT5htke8zDH2izdos2c3Y9SoVJATMesw1vsXAPzrm3bmASHJVfq";

    /**
     * Re-arranged normal JSON.
     */
    public static final String JSON_NORMAL_REARRANGED = """
            {
              "to_account": "321 567 636-4",
                   "amount": 500.00,
              "from_account": "543 232 625-3",
                "currency": "USD"
            }
            """;
}
