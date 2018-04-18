package net.gnu.androidutil;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import java.security.SecureRandom;

public class Crypto {
	private static String TAG = "Crypto";

	private static final String engine = "AES";
	private static final String crypto = "AES/CBC/PKCS5Padding";
	private static Context ctx;

	public Crypto(Context cntx) {
		ctx = cntx;
	}

	public static byte[] encrypt(byte[] key, byte[] data){
		SecretKeySpec sKeySpec = new SecretKeySpec(key,"AES");
		Cipher cipher;
		byte[] ciphertext = null;
		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);
			ciphertext = cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG,"NoSuchAlgorithmException", e);
		} catch (NoSuchPaddingException e) {
			Log.e(TAG,"NoSuchPaddingException", e);
		} catch (IllegalBlockSizeException e) {
			Log.e(TAG,"IllegalBlockSizeException", e);
		} catch (BadPaddingException e) {
			Log.e(TAG,"BadPaddingException", e);
		} catch (InvalidKeyException e) {
			Log.e(TAG,"InvalidKeyException", e);
		}
		return ciphertext;
	}

	/**
	 * byte[] key = Crypto.generateKey("randomtext".getBytes());
	 * outputStream.write(encrypt(key,contact.getBytes()));
	 */
	public static byte[] generateKey(byte[] randomNumberSeed) {
		SecretKey sKey = null;
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(randomNumberSeed);
			keyGen.init(256,random);
			sKey = keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG,"No such algorithm exception");
		}
		return sKey.getEncoded();
	}

	public byte[] cipher(byte[] data, int mode)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException {
		KeyManager km = new KeyManager(ctx);
		SecretKeySpec sks = new SecretKeySpec(km.getId(), engine);
		IvParameterSpec iv = new IvParameterSpec(km.getIv());
		Cipher c = Cipher.getInstance(crypto);
		c.init(mode, sks, iv);
		return c.doFinal(data);
	}

	public byte[] encrypt(byte[] data) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException {
		return cipher(data, Cipher.ENCRYPT_MODE);
	}

	public byte[] decrypt(byte[] data) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException {
		return cipher(data, Cipher.DECRYPT_MODE);
	}

	public String armorEncrypt(byte[] data) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException {
		return Base64.encodeToString(encrypt(data), Base64.DEFAULT);
	}

	public String armorDecrypt(String data) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException {
		return new String(decrypt(Base64.decode(data, Base64.DEFAULT)));
	}

}
