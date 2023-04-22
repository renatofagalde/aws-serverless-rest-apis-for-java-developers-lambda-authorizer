package br.com.likwi.aws.authorizer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.tools.javac.util.List;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;

/**
 * Handler for requests to Lambda function.
 */
public class LambdaAuthorizer implements RequestHandler<APIGatewayProxyRequestEvent, AuthorizerOutput> {
    public static final String VERSION = "2012-10-17";
    public static final String EXECUTE_API_INVOKE = "execute-api:Invoke";
    public static final String AMERICA_SAO_PAULO = "America/Sao_Paulo";

    @Override
    public AuthorizerOutput handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("\n\n\t*** REQUISICAO INICIADA #5 "+LocalDateTime.now(ZoneId.of(AMERICA_SAO_PAULO))+" *** " + this.getClass().getName());

        Gson result = new Gson();
        logger.log(result.toJson(input, APIGatewayProxyRequestEvent.class));

        input.getPathParameters()
                .entrySet().stream()
                .forEach(e -> {
                    logger.log(String.format("[input.getPathParameters()]\tKey %s -> [%s]",e.getKey(),e.getValue()));
                });
        String userName = input.getPathParameters().get("user_name");
        logger.log(String.format("User in pathParameters -> [%s]", userName));

        String EFFECT = "Allow";
        if (userName.equals("123") || userName.equals("negado")) {
            EFFECT = "Deny";
        }
        APIGatewayProxyRequestEvent.ProxyRequestContext proxyRequestContext =
                input.getRequestContext();
        logger.log(String.format("[proxyRequestContext]\t %s", result.toJson(proxyRequestContext,APIGatewayProxyRequestEvent.ProxyRequestContext.class)));

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

        logger.log(String.format("[authorizerOutput]\t %s", result.toJson(authorizerOutput,AuthorizerOutput.class)));
        logger.log(String.format("[authorizerOutput]\t %s", LocalDateTime.now(ZoneId.of(AMERICA_SAO_PAULO))));

        return authorizerOutput;
    }

}
