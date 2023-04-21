package br.com.likwi.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import br.com.likwi.aws.authorizer.AuthorizerOutput;
import br.com.likwi.aws.authorizer.PolicyDocument;
import br.com.likwi.aws.authorizer.Statement;
import com.sun.tools.javac.util.List;

public class GetUserByUsernameHandler implements RequestHandler<APIGatewayProxyRequestEvent, AuthorizerOutput> {

    public static final String VERSION = "2012-10-17";

    @Override
    public AuthorizerOutput handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("\n\n\t*** REQUISICAO INICIADA 5 *** " + this.getClass().getName());
        String userName = input.getPathParameters().get("user_name");
        logger.log(String.format("User in pathParameters -> [%s]", userName));
        String effect = "Allow";
        if (userName.equals("123")) {
            effect = "Deny";
        }
        APIGatewayProxyRequestEvent.ProxyRequestContext proxyRequestContext =
                input.getRequestContext();
        logger.log("proxyRequestContext.getAccountId() = " + proxyRequestContext.getAccountId());
        logger.log("proxyRequestContext.getApiId() = " + proxyRequestContext.getApiId());
        logger.log("proxyRequestContext.getStage() = " + proxyRequestContext.getStage());
        logger.log("proxyRequestContext.getHttpMethod() = " + proxyRequestContext.getHttpMethod());

        String arn = String.format("arn:aws:execute-api:%s:%s:%s/%s/%s/%s",
                System.getenv("AWS_REGION"),
                proxyRequestContext.getAccountId(),
                proxyRequestContext.getApiId(),
                proxyRequestContext.getStage(),
                proxyRequestContext.getHttpMethod(),
                "*");

        Statement statement = Statement.builder()
                .action("execute-api:Invoke")
                .effect(effect)
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

//        Gson responseBody = new Gson();
//        logger.log(String.format("Final da requisição -> [%s]", responseBody.toJson(authorizerOutput, AuthorizerOutput.class)));

        return authorizerOutput;
    }
}
