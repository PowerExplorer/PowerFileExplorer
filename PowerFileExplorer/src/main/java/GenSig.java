import java.io.*;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

class GenSig {
    public static void main(String[] args) {
        /* Generate a DSA signature */
        if (args.length != 1) {
            System.out.println("Usage: GenSig nameOfFileToSign");
        } else try {
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
				keyGen.initialize(1024, random);
				KeyPair pair = keyGen.generateKeyPair();
				PrivateKey priv = pair.getPrivate();
				PublicKey pub = pair.getPublic();
				Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
				dsa.initSign(priv);
				FileInputStream fis = new FileInputStream(args[0]);
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

	void genCert(String ksName, String ksPass, String alias) throws KeyStoreException, FileNotFoundException, CertificateEncodingException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException {
		KeyStore ks = KeyStore.getInstance("JKS");
		FileInputStream ksfis = new FileInputStream(ksName);
		BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
		ks.load(ksbufin, ksPass.toCharArray());
		PrivateKey priv = (PrivateKey) ks.getKey(alias, ksPass.toCharArray());

		java.security.cert.Certificate cert = ks.getCertificate(alias);
		byte[] encodedCert = cert.getEncoded();
// Save the certificate in a file named "suecert"
		FileOutputStream certfos = new FileOutputStream("suecert");
		certfos.write(encodedCert);
		certfos.close();
		
		
	}
}
	
