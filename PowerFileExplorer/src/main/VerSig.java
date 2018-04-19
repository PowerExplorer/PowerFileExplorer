/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

class VerSig {
    public static void verSig(String publickey, String signature, String data) {
        /* Verify a DSA signature */
        try {
			/* import encoded public key */
			//FileInputStream keyfis = new FileInputStream(publickeyfile);
			byte[] encKey = publickey.getBytes();//new byte[keyfis.available()];
			//keyfis.read(encKey);
			//keyfis.close();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
			PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
			/* input the signature bytes */
			//FileInputStream sigfis = new FileInputStream(signaturefile);
			byte[] sigToVerify = signature.getBytes();//new byte[sigfis.available()];
			//sigfis.read(sigToVerify);
			//sigfis.close();
			/* create a Signature object and initialize it with the public key */
			Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
			sig.initVerify(pubKey);
			/* Update and verify the data */
			//FileInputStream datafis = new FileInputStream(datafile);
			//BufferedInputStream bufin = new BufferedInputStream(datafis);
//			byte[] buffer = new byte[1024];
//			int len;
//			while (bufin.available() != 0) {
//				len = bufin.read(buffer);
//				sig.update(buffer, 0, len);
//			};
//			bufin.close();
			sig.update(data.getBytes());
			boolean verifies = sig.verify(sigToVerify);
			System.out.println("signature verifies: " + verifies);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	public static void genSig(String nameOfFileToSign) {
        /* Generate a DSA signature */
        try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			keyGen.initialize(1024, random);
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey priv = pair.getPrivate();
			PublicKey pub = pair.getPublic();
			Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
			dsa.initSign(priv);
			FileInputStream fis = new FileInputStream(nameOfFileToSign);
			BufferedInputStream bufin = new BufferedInputStream(fis);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = bufin.read(buffer)) >= 0) {
				dsa.update(buffer, 0, len);
			}
			bufin.close();

			byte[] realSig = dsa.sign();

			/* save the signature in a file */
			FileOutputStream sigfos = new FileOutputStream("sig");
			sigfos.write(realSig);
			sigfos.close();

			/* save the public key in a file */
			byte[] key = pub.getEncoded();
			FileOutputStream keyfos = new FileOutputStream("suepk");
			keyfos.write(key);
			keyfos.close();

		} catch (Exception e) {
			System.err.println("Caught exception " + e.toString());
		}
    }

	public static String showCert(String ksName, String ksPass, String alias) throws KeyStoreException, FileNotFoundException, CertificateEncodingException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException {
		KeyStore ks = KeyStore.getInstance("JKS");
		FileInputStream ksfis = new FileInputStream(ksName);
		BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
		ks.load(ksbufin, ksPass.toCharArray());
		PrivateKey priv = (PrivateKey) ks.getKey(alias, ksPass.toCharArray());
		System.out.println("PrivateKey " + priv);
		java.security.cert.Certificate cert = ks.getCertificate(alias);
		System.out.println("PublicKey " + cert.getPublicKey());
		byte[] encodedCert = cert.getEncoded();
		// Save the certificate in a file named "suecert"
//		FileOutputStream certfos = new FileOutputStream("suecert");
//		certfos.write(encodedCert);
//		certfos.close();
		return new String(encodedCert);

	}
}
