package net.savantly.authorization.jwt;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Creates signed JWTs for testing
 * 
 * inspired by 
 * https://connect2id.com/products/nimbus-jose-jwt/examples/jwt-with-rsa-signature
 *
 */
public class MockJwt {
	
	static final String KEY_ID = "123";
	
	private RSAKey rsaJWK;
	private RSAKey rsaPublicJWK;
	private JWSSigner signer;
	JWSVerifier verifier;
    
	public MockJwt() {
		try {
			// RSA signatures require a public and private RSA key pair, the public key 
	    	// must be made known to the JWS recipient in order to verify the signatures
	    	rsaJWK = new RSAKeyGenerator(2048)
	    	    .keyID(KEY_ID)
	    	    .generate();
	    	rsaPublicJWK = rsaJWK.toPublicJWK();
	
	    	// Create RSA-signer with the private key
	    	signer = new RSASSASigner(rsaJWK);
	    	verifier = new RSASSAVerifier(rsaPublicJWK);
		} catch (JOSEException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public String createJWT(String username) {
		return createJWT(username, new ArrayList<String>());
	}
	
	public String createJWT(String username, List<String> groups) {
		return createJWT(username, groups, new ArrayList<String>());
	}
    
    public String createJWT(String username, List<String> groups, List<String> scopes) {
    	// Prepare JWT with claims set
    	JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
    	    .subject(username)
    	    .issuer("https://issuer")
    	    .issueTime(new Date())
    	    .expirationTime(new Date(new Date().getTime() + 60 * 1000))
    	    .claim("groups", groups)
    	    .claim("scp", scopes)
    	    .claim("preferred_username", username)
    	    .build();

    	SignedJWT signedJWT = new SignedJWT(
    	    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(),
    	    claimsSet);

    	// Compute the RSA signature
    	try {
			signedJWT.sign(signer);
		} catch (JOSEException e) {
			throw new RuntimeException("Failed while creating the RSA signature for the JWT");
		}

    	// To serialize to compact form, produces something like
    	// eyJhbGciOiJSUzI1NiJ9.SW4gUlNBIHdlIHRydXN0IQ.IRMQENi4nJyp4er2L
    	// mZq3ivwoAjqa1uUkSBKFIX7ATndFF5ivnt-m8uApHO4kfIFOrW7w2Ezmlg3Qd
    	// maXlS9DhN0nUk_hGI3amEjkKd0BWYCB8vfUbUv0XGjQip78AI4z1PrFRNidm7
    	// -jPDm5Iq0SZnjKjCNS5Q15fokXZc8u0A
    	String s = signedJWT.serialize();

    	// On the consumer side, parse the JWS and verify its RSA signature
    	//signedJWT = SignedJWT.parse(s);

    	return s;
    }

	public RSAKey getRsaPublicJWK() {
		return rsaPublicJWK;
	}
}