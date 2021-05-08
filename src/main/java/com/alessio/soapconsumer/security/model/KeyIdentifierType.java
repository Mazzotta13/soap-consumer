package com.alessio.soapconsumer.security.model;

public enum KeyIdentifierType {
    BINARY_SECURITY_TOKEN_REFERENCE, // Add certificate as BinarySecurityToken
    ISSUER_SERIAL_NUMBER_REFERENCE // Add issuer name and serial number of certificate
}
