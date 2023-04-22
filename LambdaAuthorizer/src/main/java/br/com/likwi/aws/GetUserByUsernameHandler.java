package br.com.likwi.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class GetUserByUsernameHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public static final String APPLICATION_JSON = "application/json";

    public static final String VERSION = "2012-10-17";
    public static final String EXECUTE_API_INVOKE = "execute-api:Invoke";

    public static final String AMERICA_SAO_PAULO = "America/Sao_Paulo";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        Gson gson = new Gson();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", APPLICATION_JSON);
        headers.put("X-Custom-Header", APPLICATION_JSON);
        headers.put("Lambda-Version", context.getFunctionVersion());
        LambdaLogger logger = context.getLogger();

        logger.log("GetUserByUsernameHandler in authorizer #5 "+ LocalDateTime.now(ZoneId.of(AMERICA_SAO_PAULO)));

        input.getPathParameters()
                .entrySet().stream()
                .forEach(e -> {
                    logger.log(String.format("[input.getPathParameters()]\tKey %s -> [%s]",e.getKey(),e.getValue()));
                });

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        try {
            String userName = input.getPathParameters().get("user_name");
            HashMap<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put("Content-Type", APPLICATION_JSON);
            responseHeaders.put("Lambda-Version", context.getFunctionVersion());

            response
                    .withHeaders(responseHeaders)
                    .withStatusCode(200)
                    .withBody("{\"nome\":\""+userName+"__getUserHandler\"}");
        } catch (Exception e) {
            logger.log("Exception\t" + e.getLocalizedMessage());
            response.withBody("{\"erro\": \"" + e.getLocalizedMessage() + "\"}");
            response.withStatusCode(500);

        }

        logger.log("GetUserByUsernameHandler response  -> " +
                gson.toJson(response, APIGatewayProxyResponseEvent.class));
        return response;
    }
}
