package br.com.likwi.aws.service;

import com.google.gson.JsonObject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;

public class CognitoUserService {

    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

    public CognitoUserService(String region) {

        this.cognitoIdentityProviderClient = CognitoIdentityProviderClient
                .builder()
                .region(Region.of(region))
                .build();
    }

    public JsonObject getUserByUserName(String userName, String userPoolId) {
        AdminGetUserRequest userRequest = AdminGetUserRequest.builder()
                .username(userName)
                .userPoolId(userPoolId)
                .build();
        AdminGetUserResponse adminGetUserResponse = this.cognitoIdentityProviderClient.adminGetUser(userRequest);

        JsonObject userDetails = new JsonObject();
        if (!adminGetUserResponse.sdkHttpResponse().isSuccessful()) {
            throw new IllegalArgumentException("Acesso não permitido. Code " + adminGetUserResponse.sdkHttpResponse().statusCode());
        }

        adminGetUserResponse.userAttributes().forEach(attibute -> userDetails.addProperty(attibute.name(), attibute.value()));
        return userDetails;
    }
}
