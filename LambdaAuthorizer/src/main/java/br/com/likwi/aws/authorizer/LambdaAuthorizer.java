package br.com.likwi.aws.authorizer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.tools.javac.util.List;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Handler for requests to Lambda function.
 */
public class LambdaAuthorizer implements RequestHandler<APIGatewayProxyRequestEvent, AuthorizerOutput> {
    public static final String VERSION = "2012-10-17";
    public static final String EXECUTE_API_INVOKE = "execute-api:Invoke";
    public static final String AMERICA_SAO_PAULO = "America/Sao_Paulo";

    private final String poolId = System.getenv("POOL_ID");
    private final String myCognitoUserPoolId = System.getenv("MY_COGNITO_USER_POOL_ID");
//    private final String myCognitoClientAppSecret = System.getenv("MY_COGNITO_CLIENT_APP_SECRET");

    private DecodedJWT validateJWT4User = null;



    @Override
    public AuthorizerOutput handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("\n\n\t*** REQUISICAO INICIADA #12 " + LocalDateTime.now(ZoneId.of(AMERICA_SAO_PAULO)) + " *** " + this.getClass().getName());
        String EFFECT = "Allow";

        Gson result = new Gson();
        logger.log(result.toJson(input, APIGatewayProxyRequestEvent.class));

        input.getPathParameters()
                .entrySet().stream()
                .forEach(e -> {
                    logger.log(String.format("[input.getPathParameters()]\tKey %s -> [%s]", e.getKey(), e.getValue()));
                });
        String userName = input.getPathParameters().get("user_name");
        String authorization = input.getHeaders().get("Authorization");
        logger.log(String.format("User in pathParameters -> [%s]", userName));
        logger.log(String.format("User in Authorization -> [%s]", authorization));


        ValidateJWTForUser validateJWTForUser = new ValidateJWTForUser(authorization, System.getenv("AWS_REGION"), this.poolId, userName, this.myCognitoUserPoolId);
        logger.log(result.toJson(validateJWTForUser, ValidateJWTForUser.class));
        try {

            this.validateJWT4User = new JWTService().validateJWT4User(validateJWTForUser);
            userName = this.validateJWT4User.getSubject();
        } catch (RuntimeException e) {
            EFFECT = "Deny";
            logger.log("Erro ao gerar token para " + userName);
            logger.log(e.getLocalizedMessage());
            e.printStackTrace();
        }


        APIGatewayProxyRequestEvent.ProxyRequestContext proxyRequestContext =
                input.getRequestContext();
        logger.log(String.format("[proxyRequestContext]\t %s", result.toJson(proxyRequestContext, APIGatewayProxyRequestEvent.ProxyRequestContext.class)));

        String arn = String.format("arn:aws:execute-api:%s:%s:%s/%s/%s/%s",
                System.getenv("AWS_REGION"),
                proxyRequestContext.getAccountId(),
                proxyRequestContext.getApiId(),
                proxyRequestContext.getStage(),
                proxyRequestContext.getHttpMethod(),
                "*");
        logger.log(String.format("[arn]\t %s", arn));

        Statement statement = Statement.builder()
                .action(EXECUTE_API_INVOKE)
                .effect(EFFECT)
                .resource(arn)
                .build();

        PolicyDocument policyDocument = PolicyDocument.builder()
                .version(VERSION)
                .statements(List.of(statement))
                .build();

        AuthorizerOutput authorizerOutput = AuthorizerOutput.builder()
                .principalId(userName)
                .policyDocument(policyDocument)
                .build();

        logger.log(String.format("[authorizerOutput]\t %s", result.toJson(authorizerOutput, AuthorizerOutput.class)));

        return authorizerOutput;
    }

}
