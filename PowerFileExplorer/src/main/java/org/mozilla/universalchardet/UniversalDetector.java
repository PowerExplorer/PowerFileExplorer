package org.mozilla.universalchardet;
import java.io.*;
import net.gnu.util.*;


public class UniversalDetector   {

    private long mDet;
	
	public static String detectCharset(File file) throws IOException {
		InputStream is = new BufferedInputStream( new FileInputStream( file ) , 65536 );
		is.mark(65536);

		//  æå­ã³ã¼ãã®å¤å®
		String encode = null;
		try{
			final UniversalDetector detector = new UniversalDetector();
			try {
				int nread;
				int start = 0;
				int length = Math.min(65536, (int)file.length());
				byte[] buff = new byte[length];
				if (length > 0) {
					while ((nread = is.read(buff, start, length - start)) > 0) {
						start += nread;
					}
					detector.handleData(buff, 0, length);
					detector.dataEnd();
				}
			} catch (IOException e) {
				e.printStackTrace();
				FileUtil.close(is);
				return null;
			}
			encode = detector.getCharset();
			detector.reset();
			detector.destroy();
		} catch( UniversalDetector.DetectorException e){
			e.printStackTrace();
		}
		return encode;
	}
	
    public UniversalDetector() throws DetectorException
    {
        mDet = chardet_create();
        if ( mDet == 0 ){
            throw new DetectorException();
        }
    }

    public static class DetectorException extends Exception{
        private static final long serialVersionUID = 1L;
    }

    public  void destroy()
    {
        chardet_destroy(mDet);
    }
    public int handleData( byte[] data , int offset , int len)
    {
        return chardet_handle_data( mDet , data , offset , len );
    }
    public int dataEnd()
    {
        return chardet_data_end(mDet);
    }
    public int reset()
    {
        return chardet_reset(mDet);
    }
    public String getCharset()
    {
        String ret = chardet_get_charset(mDet);
        if ( ret.length() == 0 ){
            ret = null;
        }
        return ret;
    }

    static {
        System.loadLibrary("universalchardet");
    }
    /**
     * Create an encoding detector.
     * @param pdet [out] pointer to a long variable that receives
     *             the encoding detector handle.
     * @return CHARDET_RESULT_OK if succeeded. CHARDET_RESULT_NOMEMORY otherwise.
     */
    private native static long chardet_create();

    /**
     * Destroy an encoding detector.
     * @param det [in] the encoding detector handle to be destroyed.
     */
    private native static void chardet_destroy(long det);

    /**
     * Feed data to an encoding detector.
     * @param det [in] the encoding detector handle
     * @param data [in] data
     * @param len [in] length of data in bytes.
     * @return CHARDET_RESULT_OK if succeeded.
     *         CHARSET_RESULT_NOMEMORY if running out of memory.
     *         CHARDET_RESULT_INVALID_DETECTOR if det was invalid.
     */
    private native static int chardet_handle_data(long det, byte[] data, int offset , int len);

    /**
     * Notify an end of data to an encoding detctor.
     * @param det [in] the encoding detector handle
     * @return CHARDET_RESULT_OK if succeeded.
     *         CHARDET_RESULT_INVALID_DETECTOR if det was invalid.
     */
    private native static int chardet_data_end(long det);

    /**
     * Reset an encoding detector.
     * @param det [in] the encoding detector handle
     * @return CHARDET_RESULT_OK if succeeded.
     *         CHARDET_RESULT_INVALID_DETECTOR if det was invalid.
     */
    private native static int chardet_reset(long det);

    /**
     * Get the name of encoding that was detected.
     * @param det [in] the encoding detector handle
     * @param namebuf [in/out] pointer to a buffer that receives the name of
     *                detected encoding. A valid encoding name or an empty string
     *                will be written to namebuf. If an empty strng was written,
     *                the detector could not detect any encoding.
     *                Written strings will always be NULL-terminated.
     * @param buflen [in] length of namebuf
     * @return CHARDET_RESULT_OK if succeeded.
     *         CHARDET_RESULT_NOMEMORY if namebuf was too small to store
     *         the entire encoding name.
     *         CHARDET_RESULT_INVALID_DETECTOR if det was invalid.
     */
    private native static String chardet_get_charset(long det);

}
