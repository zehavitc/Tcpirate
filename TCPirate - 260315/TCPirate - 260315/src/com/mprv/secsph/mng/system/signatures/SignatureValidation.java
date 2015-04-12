package com.mprv.secsph.mng.system.signatures;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 06/03/2005
 * Time: 12:17:29
 * To change this template use File | Settings | File Templates.
 */
public class SignatureValidation {

public static synchronized native boolean validateSignature(String signaturePattern, boolean isCaseSensitive, boolean isApplicable);

}
