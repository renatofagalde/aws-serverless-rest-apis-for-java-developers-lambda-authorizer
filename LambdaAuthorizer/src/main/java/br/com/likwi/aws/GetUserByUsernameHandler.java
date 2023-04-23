package br.com.likwi.aws;

import br.com.likwi.aws.service.CognitoUserService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class GetUserByUsernameHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public static final String APPLICATION_JSON = "application/json";

    public static final String VERSION = "2012-10-17";
    public static final String EXECUTE_API_INVOKE = "execute-api:Invoke";

    public static final String AMERICA_SAO_PAULO = "America/Sao_Paulo";

    private final CognitoUserService cognitoUserService;

    private static Map<String, String> headers = new HashMap<>();

    private static APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

    public GetUserByUsernameHandler() {
        this.cognitoUserService = new CognitoUserService(System.getenv("AWS_REGION"));
    }

    static {
        headers.put("Content-Type", APPLICATION_JSON);
        headers.put("X-Custom-Header", APPLICATION_JSON);
        response.withStatusCode(200)
                .withHeaders(headers);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        Gson gson = new Gson();
        LambdaLogger logger = context.getLogger();

        try {

            response.withBody(
                    gson.toJson(this.cognitoUserService.getUserByUserName(
                            input.getPathParameters().get("user_name"),
                            System.getenv("POOL_ID"))
                    , JsonObject.class));
        } catch (Exception e) {
            logger.log("Exception\t" + e.getLocalizedMessage());
            response.withBody(ErrorResponse.build(new ErrorResponse(e.getLocalizedMessage())));
            response.withStatusCode(500);
        }

        return response;
    }
}
