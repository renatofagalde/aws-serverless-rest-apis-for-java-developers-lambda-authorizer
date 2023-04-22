package br.com.likwi.aws.authorizer;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JWTService {

    public DecodedJWT validateJWT4User(ValidateJWTForUser validateJWTForUser){
        AWSCognitoRSAKeyProvider awsCognitoRSAKeyProvider = new AWSCognitoRSAKeyProvider(
                validateJWTForUser.getRegion(), validateJWTForUser.getUserPoolId());

        Algorithm algorithm = Algorithm.RSA256(awsCognitoRSAKeyProvider);
        String issuer = String.format("https://cognito-idp.%s.amazonaws.com/%s",
                validateJWTForUser.getRegion(), validateJWTForUser.getPrincipalId());
        JWTVerifier jwtVerifier = JWT.require(algorithm)
                .withSubject(validateJWTForUser.getPrincipalId())
                .withSubject(validateJWTForUser.getAudience())
                .withIssuer(issuer)
                .withClaim("token_use", "id")
                .build();

        return jwtVerifier.verify(validateJWTForUser.getJWT());
    }
}
