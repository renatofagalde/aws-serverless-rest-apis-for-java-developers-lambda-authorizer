package br.com.likwi.aws.authorizer;

public class ValidateJWTForUser {
    private String JWT;
    private String region;
    private String userPoolId;
    private String principalId;
    private String audience;

    public ValidateJWTForUser(String JWT, String region, String userPoolId, String principalId, String audience) {
        this.JWT = JWT;
        this.region = region;
        this.userPoolId = userPoolId;
        this.principalId = principalId;
        this.audience = audience;
    }

    public String getJWT() {
        return JWT;
    }

    public String getRegion() {
        return region;
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public String getAudience() {
        return audience;
    }
}
