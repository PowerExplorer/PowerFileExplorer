package net.gnu.searcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import net.gnu.util.FileUtil;
import net.gnu.util.HtmlUtil;

public class SplitMergeTask  extends AsyncTask<Void, Void, String> {

	private Activity activity = null;
	private Collection<String> filePaths;
	private static NumberFormat nf = NumberFormat.getInstance();
	//private static final String SPLIT_PAT01 = ".+\\.001";
	private static final String SPLIT_PAT = ".+\\.\\d{3}";
	//private static final Pattern SPLIT_PATTERN01 = Pattern.compile(SPLIT_PAT01);
	private static final Pattern SPLIT_PATTERN = Pattern.compile(SPLIT_PAT);
	private static final int DEFAULT_ARRAY_LENGTH = 32768;
	private int parts;
	private long size;
	private String saveTo;
	boolean byChar = false;
	boolean byWord = false;
	private static final String TAG = "MergeSplitTask";

	public SplitMergeTask(Activity s, Collection<String> filePaths, String saveTo, int parts, long size, boolean byChar, boolean byWord) {
		this.filePaths = filePaths;
		this.parts = parts;
		this.size = size;
		this.activity = s;
		this.saveTo = saveTo;
		this.byChar = byChar;
		this.byWord = byWord;
	}

	protected String doInBackground(Void... s) {
		try {
			if (size == 0) {
				mergeSplit(filePaths, parts, 0, saveTo);
			} else {
				mergeSplit(filePaths, 0, size, saveTo);
			}
			return "Spliting and Merging are finished";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Spliting and Merging are unsuccessful";
	}

	@Override
	protected void onPostExecute(String result) {
		Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
		Log.d(TAG, result);
	}

	public void mergeSplit(Collection<String> filePaths, long parts, long sizeOfPart, String savePath) throws IOException {
		nf.setMinimumIntegerDigits(3);
		for (String st : filePaths) {
			if (!isCancelled()) {
				if (SPLIT_PATTERN.matcher(st).matches()) {
					merge(st, savePath);
				} else if (!SPLIT_PATTERN.matcher(st).matches()) {
					split(st, parts, sizeOfPart, savePath, byChar, byWord);
				}
			}
		}
	}

	private void split(String fPath, long parts, long sizeOfPart, String savePath, boolean byChar, boolean byWord) throws IOException {

		Log.d(TAG, fPath + ", " + parts + ", " + sizeOfPart + ", " + saveTo);
		File inFile = new File(fPath);
		RandomAccessFile f = new RandomAccessFile(inFile, "r");
		long len = f.length();
		String readFileWithCheckEncode = "";
		if (byWord) {
			readFileWithCheckEncode = FileUtil.readFileWithCheckEncode(fPath);
			len = readFileWithCheckEncode.length();
		}
		if (len < parts) {
			parts = 0;//len;
			sizeOfPart = DEFAULT_ARRAY_LENGTH;
		}
		if (len < sizeOfPart) {
			sizeOfPart = len;
		}
		if (parts == 0 && sizeOfPart == 0) {
			parts = 1;
		}
		if (parts == 1 || len == 0) {
			f.close();
			return;
		}
		if (parts > 0) {
			sizeOfPart = len / parts;
		} else {
			parts = ((len % sizeOfPart) == 0) ? len / sizeOfPart : len / sizeOfPart + 1;
		}
		byte[] bArr = new byte[(int)Math.min(DEFAULT_ARRAY_LENGTH, sizeOfPart)];
		Log.d(TAG, len + ", " + parts + ", " + sizeOfPart + ", " + bArr.length);

		String newPath = savePath + fPath;
		new File(newPath).getParentFile().mkdirs();

		if (byWord) {
			int no = 1;
			FileWriter fw = new FileWriter(newPath + "." + nf.format(no++));
			BufferedWriter bw = new BufferedWriter(fw);
			f.close();
			FileReader fr = new FileReader(inFile);
			BufferedReader br = new BufferedReader(fr);
			int ch = 0;
			int totalRead = 0;
			boolean inWord = false;
			int wordCount = 0;
			CharArrayWriter barrBuffer = new CharArrayWriter(4096);
			while ((ch = br.read()) != -1) {
				//totalRead++;
				barrBuffer.write(ch);
				if (inWord && "`~!@#$%^&*()_-+={[}]\\|;:'\",<.>/? \t\r\n\fÃ¢ÂÂÃ¢ÂÂÃ¢ÂÂÃ¢ÂÂ".indexOf(ch) >= 0) {
					inWord = false;
				} else if (!inWord && ("`~!@#$%^&*()_-+={[}]\\|;:'\",<.>/? \t\r\n\fÃ¢ÂÂÃ¢ÂÂÃ¢ÂÂÃ¢ÂÂ".indexOf(ch) < 0)) {
					wordCount++;
					totalRead++;
					inWord = true;
				}
				//Log.i(TAG, "first append");
				if (ch == '\n' || ch == '.' || ch == '?' || ch == '!') {
					if (totalRead >= sizeOfPart) {
						//wordCount = lastWordNum;
						if (totalRead == sizeOfPart) {
							totalRead = 0;
						} else {
							totalRead = wordCount;//barrBuffer.length();
						}
						FileUtil.flushClose(bw);
						FileUtil.flushClose(fw);
						fw = new FileWriter(newPath + "." + nf.format(no++));
						bw = new BufferedWriter(fw);
						//Log.i(TAG, "new bos");
					} else {
						bw.write(barrBuffer.toCharArray());
						barrBuffer.reset();
						//Log.i(TAG, "clear buf");
					}
					wordCount = 0;
				} else if (wordCount >= sizeOfPart) { //barrBuffer.length()
					bw.write(barrBuffer.toCharArray());
					FileUtil.flushClose(bw);
					FileUtil.flushClose(fw);
					if (br.ready()) {
						fw = new FileWriter(newPath + "." + nf.format(no++));
						bw = new BufferedWriter(fw);
					}
					barrBuffer.reset();
					totalRead = 0;
					wordCount = 0;
				}
			}
			Log.i(TAG, "last new " + totalRead + ", " + sizeOfPart + ", " + barrBuffer.size());
			if (totalRead >= sizeOfPart) {
				FileUtil.flushClose(bw);
				FileUtil.flushClose(fw);
				if (barrBuffer.size() > 0) {
					fw = new FileWriter(newPath + "." + nf.format(no++));
					bw = new BufferedWriter(fw);
				}
				Log.i(TAG, "last new");
			}
			if (barrBuffer.size() > 0) {
				bw.write(barrBuffer.toCharArray());
				Log.i(TAG, "last write");
			} 
			FileUtil.flushClose(bw);
			FileUtil.flushClose(fw);
			
			br.close();
			fr.close();
//			String p = "(\\s*\\S+\\s*){1," + sizeOfPart + "}[\\.?!\n]";
//			Pattern patt = Pattern.compile(p, Pattern.MULTILINE | Pattern.UNICODE_CASE);
//			Log.i(TAG, p);
//			Matcher mat = patt.matcher(readFileWithCheckEncode);
//			int end = 0;
//			int start = 0;
//			int i = 1;
//			while (mat.find()) {
//				start = mat.start();
//				if (start != end) {
//					Log.i(TAG + i, readFileWithCheckEncode.substring(end, start));
//					FileUtil.writeFully(new File(newPath + "." + nf.format(i++)), readFileWithCheckEncode.substring(end, start).getBytes());
//				}
//				Log.i(TAG + i, mat.group());
//				FileUtil.writeFully(new File(newPath + "." + nf.format(i++)), mat.group().getBytes());
//				end = mat.end();
//			}
//			Log.i(TAG, len + ".");
//			if (end < len) {
//				Log.i(TAG + i, readFileWithCheckEncode.substring(end));
//				FileUtil.writeFully(new File(newPath + "." + nf.format(i)), readFileWithCheckEncode.substring(end).getBytes());
//			}
		} else if (!byChar) {
			if (sizeOfPart <= DEFAULT_ARRAY_LENGTH) {
				for (long i = 1; i < parts; i++) {
					writeClose(f, bArr, newPath, i);
				}
				writeClose(f, new byte[(int)(len - (parts - 1) * sizeOfPart)], newPath, parts);
			} else {
				for (long i = 1; i <= parts; i++) {
					String format = newPath + "." + nf.format(i);
					File file = new File(format);
					file.delete();
					file.createNewFile();
					OutputStream os = new FileOutputStream(format, true);
					BufferedOutputStream bos = new BufferedOutputStream(os);

					for (long j = 0; j < (sizeOfPart / DEFAULT_ARRAY_LENGTH) - 1; j++) {
						writeNotClose(f, bArr, bos);
					}
					if (i < parts) {
						writeNotClose(f, new byte[(int)(sizeOfPart - (sizeOfPart / DEFAULT_ARRAY_LENGTH - 1) * bArr.length)], bos);
					} else {
						writeNotClose(f, new byte[(int)(len - (parts-1)*sizeOfPart - (sizeOfPart/DEFAULT_ARRAY_LENGTH - 1)*bArr.length)], bos);
					}
					bos.close();
				}
			}
		} else {
			long written = 0;
			if (sizeOfPart <= DEFAULT_ARRAY_LENGTH) {

				long length = f.length();
				int i = 1;
				while (length - written > sizeOfPart) {
					written = writeClose(f, written, sizeOfPart, newPath, i++);
				}
				writeClose(f, written, length - written, newPath, i);//parts
			} else {
				long prevPrevWritten = 0;
				for (long i = 1; i <= parts; i++) {
					String format = newPath + "." + nf.format(i);
					File file = new File(format);
					file.delete();
					file.createNewFile();
					OutputStream os = new FileOutputStream(format, true);
					BufferedOutputStream bos = new BufferedOutputStream(os);

					long prevWritten = 0;
					long curWritten = 0;
					while (sizeOfPart - prevWritten > DEFAULT_ARRAY_LENGTH) {
						curWritten = writeNotClose(f, prevPrevWritten, DEFAULT_ARRAY_LENGTH, bos);
						prevWritten += curWritten;
						prevPrevWritten += curWritten;
					}
					if (i < parts) {
						curWritten = writeNotClose(f, prevPrevWritten, sizeOfPart - prevWritten, bos);
						prevPrevWritten += curWritten;
					} else {
						writeNotClose(f, prevPrevWritten, f.length() - prevPrevWritten, bos);
					}
					bos.close();
				}
			}
		}
		f.close();
	}

	private long writeClose(RandomAccessFile f, long start, long size, String st2, long parts) throws IOException {
		OutputStream os = new FileOutputStream(st2 + "." + nf.format(parts));
		BufferedOutputStream bos = new BufferedOutputStream(os);
		long len = size - 1;
		int read = 0;
		if (start + size < f.length()) {
			while (len > 0) {
				f.seek(start + len);
				read = f.read();
				if (read == '\n' || read == '.' || read == '?' || read == '!') {
					break;
				} else {
					len--;
				}
			}
		} else {
			byte[] bArr = new byte[(int)size];
			f.seek(start);
			writeNotClose(f, bArr, bos);
			bos.close();
			f.seek(start + size);
			return start + size;
		}
		if (len > 0) {
			byte[] bArr = new byte[(int)len + 1];
			f.seek(start);
			writeNotClose(f, bArr, bos);
			bos.close();
			f.seek(start + len + 1);
			return start + len + 1;
		} else {
			byte[] bArr = new byte[(int)size];
			f.seek(start);
			writeNotClose(f, bArr, bos);
			bos.close();
			f.seek(start + size);
			return start + size;
		}

	}

	private void writeClose(RandomAccessFile f, byte[] bArr, String st2, long parts) throws IOException {
		OutputStream os = new FileOutputStream(st2 + "." + nf.format(parts));
		BufferedOutputStream bos = new BufferedOutputStream(os);
		writeNotClose(f, bArr, bos);
		bos.close();
	}

	private long writeNotClose(RandomAccessFile f, long start, long size, BufferedOutputStream bos) throws IOException {
		long len = size - 1;
		if (start + size < f.length()) {
			while (len > 0) {
				f.seek(start + len);
				int read = f.read();
				if (read == '\n' || read == '.' || read == '?' || read == '!') {
					break;
				} else {
					len--;
				}
			}
		} else {
			byte[] bArr = new byte[(int)size];
			f.seek(start);
			writeNotClose(f, bArr, bos);
			f.seek(start + size);
			return start + size;
		}
		if (len > 0) {
			byte[] bArr = new byte[(int)len + 1];
			f.seek(start);
			writeNotClose(f, bArr, bos);
			f.seek(start + len + 1);
			return len + 1;
		} else {
			byte[] bArr = new byte[(int)size];
			f.seek(start);
			writeNotClose(f, bArr, bos);
			f.seek(start + size);
			return start + size;
		}
	}

	private void writeNotClose(RandomAccessFile f, byte[] bArr, BufferedOutputStream bos) throws IOException {
		f.readFully(bArr);
		bos.write(bArr);
		bos.flush();
	}

	private void merge(String fPath, String savePath) throws IOException {
		Log.d(TAG, fPath);
		String realName = fPath.substring(0, fPath.lastIndexOf("."));

		Log.d(TAG, realName);
		String pat = realName.replaceAll(HtmlUtil.SPECIAL_CHAR_PATTERNSTR, "\\\\$1") + "\\.\\d{3}";
		Pattern M_PATTERN = Pattern.compile(pat);
		File fTmp = new File(savePath + realName + ".tmp");
		fTmp.getParentFile().mkdirs();
		Log.d(TAG, fTmp.getAbsolutePath());
		fTmp.delete();
		fTmp.createNewFile();
		FileOutputStream fos = new FileOutputStream(fTmp, true);
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		// copy s to bos
		File[] list = new File(fPath).getParentFile().listFiles();
		Arrays.sort(list);
		for (File sst : list) {
//			System.out.println(sst);
			if (M_PATTERN.matcher(sst.getAbsolutePath()).matches()) {
//				System.err.println(sst);
				FileUtil.is2OsNotCloseOs(new FileInputStream(sst), bos);
			}
		}
		FileUtil.flushClose(bos, fos);
		File file = new File(savePath + realName);
		file.delete();
		fTmp.renameTo(file);
	}

	
}
