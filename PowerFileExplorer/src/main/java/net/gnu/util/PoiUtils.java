//package net.gnu.util;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.regex.Pattern;
//
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.hwpf.converter.WordToTextConverter;
//import org.apache.poi.hwpf.extractor.Word6Extractor;
//import org.apache.poi.hwpf.extractor.WordExtractor;
//import org.apache.poi.poifs.filesystem.POIFSFileSystem;
//import org.apache.poi.ss.usermodel.Workbook;
//
//import android.content.Context;
//import android.net.Uri;
//import android.util.Log;
//
////import com.itextpdf.text.pdf.PRStream;
////import com.itextpdf.text.pdf.PdfName;
////import com.itextpdf.text.pdf.PdfObject;
////import com.itextpdf.text.pdf.PdfReader;
////import com.itextpdf.text.pdf.PdfStream;
////import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
////import com.itextpdf.text.pdf.parser.PdfTextExtractor;
//import org.apache.poi.hpbf.extractor.*;
//import org.apache.poi.hdgf.extractor.*;
//import org.apache.poi.hslf.extractor.*;
//import org.apache.poi.hssf.extractor.*;
//import java.io.*;
//import java.util.*;
//import net.gnu.p7zip.*;
//import net.gnu.util.*;
//
//public class PoiUtils {
//
////	public static void compressBy7Z(File f, SevenZOutputFile sevenZOutput, Integer len) throws IOException {
////		//List<File> l =  FileUtil.getFiles(fPath);
////		//int len = fPath.length();
////		//SevenZOutputFile sevenZOutput = new SevenZOutputFile(sz);
////		//for (File f:l) {
////		String absolutePath = f.getAbsolutePath();
////		Log.d(": " + f.length(), absolutePath);
////		SevenZArchiveEntry entry =
////			sevenZOutput .createArchiveEntry(f ,
////											 absolutePath.substring(len, absolutePath.length()));
////		sevenZOutput .putArchiveEntry(entry);
////		if (f.isFile()) {
////			byte[] barr = FileUtil.readFully(f);
////			sevenZOutput .write(barr);
////		}
////		sevenZOutput .closeArchiveEntry();
////	}
////
////	public static void executeFiles(String inPath, SevenZOutputFile sz , Method m) throws IOException {
////		FileUtil.executeFiles(new File(inPath), sz, m);
////	}
////
////	public static void executeFiles(File inFile, SevenZOutputFile sz, Method m) throws IOException {
////		String absolutePath = inFile.getAbsolutePath();
////		Log.d("executeFiles inFile", absolutePath);
////		int len = inFile.getParentFile().getAbsolutePath().length() + 1;
////		final Stack<File> stk = new Stack<File>();
////		if (inFile.isDirectory()) {
////			len = absolutePath.length() + 1;
////			stk.push(inFile);
////		} else {
////			try {
////				Log.d("executeFiles inFile", absolutePath);
////				m.invoke(null, inFile, sz, len);
////			} catch (Exception e) {
////				e.printStackTrace();
////				return;
////			}
////			//fList.add(f);
////		}
////		File fi = null;
////		File[] fs;
////		while (stk.size() > 0) {
////			fi = stk.pop();
////			fs = fi.listFiles();
////			if (fs != null) {
////				for (File f2 : fs) {
////					if (f2.isDirectory()) {
////						stk.push(f2);
////					} else {
////						try {
////							Log.d("executeFiles f2", f2.getAbsolutePath());
////							m.invoke(null, f2, sz, len);
////
////						} catch (Exception e) {
////							e.printStackTrace();
////							return;
////						}
////						//fList.add(f2);
////					}
////				}
////			}
////		}
////		sz.finish();
////		sz.close();
////	}
////
////	
//	
////	public static void extractPdfImages(String src, String dest)
////	throws IOException {
////		PdfReader reader = new PdfReader(src);
////		for (int i = 0; i < reader.getXrefSize(); i++) {
////			PdfObject pdfobj = reader.getPdfObject(i);
////			if (pdfobj == null || !pdfobj.isStream()) {
////				continue;
////			}
////			PdfStream stream = (PdfStream) pdfobj;
////			// PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
////			PdfObject pdfsubtype = stream.getDirectObject(PdfName.SUBTYPE);
////			if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
////				byte[] img = PdfReader.getStreamBytesRaw((PRStream) stream);
////				File file = new File(String.format(dest + "-%1$05d", i) + ".jpg");
////				FileOutputStream fos = new FileOutputStream(file);
////				BufferedOutputStream bos = new BufferedOutputStream(fos);
////				bos.write(img);
////				flushClose(bos);
////				flushClose(fos);
////				Log.d("wrote", file.getAbsolutePath());
////			}
////		}
//////		PdfReader reader = new PdfReader(src);
//////		PdfObject obj;
//////		for (int i = 1; i <= reader.getXrefSize(); i++) {
//////			obj = reader.getPdfObject(i);
//////			if (obj != null && obj.isStream()) {
//////				PRStream stream = (PRStream) obj;
//////				byte[] b;
//////				try {
//////					b = PdfReader.getStreamBytes(stream);
//////				} catch (UnsupportedPdfException e) {
//////					b = PdfReader.getStreamBytesRaw(stream);
//////				}
//////				FileOutputStream fos = new FileOutputStream(String.format(dest,
//////						i));
//////				fos.write(b);
//////				fos.flush();
//////				fos.close();
//////			}
//////		}
////	}
//
////	public static String[] readWordFileToParagraphs(String wordFileName)
////	throws IOException {
////		POIFSFileSystem fs = createPOIFSFileSystem(wordFileName);
////		WordExtractor we = new WordExtractor(fs);
////		String[] paragraphs = we.getParagraphText();
////		we.close();
////		return paragraphs;
////	}
//	
//	
//
//	public static String readWordFileToText(String wordFileName)
//	throws IOException {
//		try {
//			return WordToTextConverter.getText(new File(wordFileName));
//		} catch (Exception e) {
//			e.printStackTrace();
//			try {
//				POIFSFileSystem fs = createPOIFSFileSystem(wordFileName);
//				WordExtractor we = new WordExtractor(fs);
//				String[] paragraphs = we.getParagraphText();
//				we.close();
//				StringBuilder sb = new StringBuilder();
//				for (String st : paragraphs) {
//					sb.append(st, 0, st.length());
//				}
//				return sb.toString();
//			} catch (Exception e2) {
//				FileInputStream fis = new FileInputStream(wordFileName);
//				BufferedInputStream bis = new BufferedInputStream(fis);
//				Word6Extractor extractor = new Word6Extractor(bis);
//				String text = extractor.getText();
//				FileUtil.close(bis);
//				FileUtil.close(fis);
//				extractor.close();
//				return text;
//			}
//		}
//	}
//
//	public static String getPublisherText(String inFilePath) throws  IOException {
//		return new PublisherTextExtractor(new BufferedInputStream(
//											  new FileInputStream(inFilePath))).getText();
//	}
//
//	public static String getVisioText(String inFilePath) throws IOException {
//		return new VisioTextExtractor(new BufferedInputStream(
//										  new FileInputStream(inFilePath))).getText();
//	}
//
//	public static String getPowerPointText(String inFilePath) throws IOException {
//		return new PowerPointExtractor(inFilePath).getText();
//	}
//
//	public static String getExcelText(String inFilePath) throws IOException {
//		return new ExcelExtractor(new POIFSFileSystem(
//									  new BufferedInputStream(new FileInputStream(inFilePath)))).getText();
//	}
//
//	public static Workbook readWorkBook(String inputFile)
//	throws FileNotFoundException, IOException {
//		FileInputStream fis = new FileInputStream(inputFile);
//		BufferedInputStream bis = new BufferedInputStream(fis);
//		POIFSFileSystem fileSystem = new POIFSFileSystem(bis);
//		Workbook wb = new HSSFWorkbook(fileSystem);
//		return wb;
//	}
//
//	public static void writeWorkBook(Workbook wb, String outputFile)
//	throws FileNotFoundException, IOException {
//		File fTemp = new File(outputFile + ".tmp");
//		FileOutputStream fos = new FileOutputStream(fTemp);
//		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		wb.write(bos);
//		bos.flush();
//		bos.close();
//		fos.close();
//		File file = new File(outputFile);
//		file.delete();
//		fTemp.renameTo(file);
//	}
//
//	private static POIFSFileSystem createPOIFSFileSystem(String fileName)
//	throws IOException {
//		FileInputStream fis = new FileInputStream(fileName);
//		POIFSFileSystem fs = new POIFSFileSystem(new BufferedInputStream(fis));
//		return fs;
//	}
//
////	public static String convertToText(File inFile) // docx, xlsx, pptx
////			throws FileNotFoundException, IOException {
////		String inFilePath = inFile.getAbsolutePath().toLowerCase();
////		String currentContent = "";
////		// FileInputStream fis = new FileInputStream(inFile);
////		// if (inFilePath.endsWith("docx")) {
////		// XWPFWordExtractor extractor = new XWPFWordExtractor(new
////		// XWPFDocument(fis));
////		// currentContent = extractor.getText();
////		// } else if (inFilePath.endsWith("xlsx")) {
////		// XSSFWorkbook workbook = new XSSFWorkbook(fis);
////		// XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook);
////		// currentContent = extractor.getText();
////		// } else if (inFilePath.endsWith("pptx")) {
////		// XMLSlideShow slideShow = new XMLSlideShow(fis);
////		// XSLFPowerPointExtractor extractor = new
////		// XSLFPowerPointExtractor(slideShow);
////		// currentContent = extractor.getText();
////		// } else
////		if (inFilePath.endsWith("doc") || inFilePath.endsWith("rtf")) {
////			currentContent = readWordFileToText(inFile);
////		}
////		return currentContent;
////	}
//
//	public File getTempFile(Context context, String url) {
//		File file = null;
//		try {
//			String fileName = Uri.parse(url).getLastPathSegment();
//			file = File.createTempFile(fileName + "--", null,
//									   context.getCacheDir());
//		} catch (IOException e) {
//			// Error while creating file
//		}
//		return file;
//	}
//
////	public static void parsePdfToText(String pdfFile, String txtFile) //pdfbox
////	throws IOException, PDFException, PDFSecurityException, InterruptedException {
////		Log.i("Source PDF:", pdfFile);
////		Log.i("Destination txtFile: ", txtFile);
////
////        // open the url
////        Document document = new Document();
////		File fileTmp = new File(txtFile + ".tmp");
////
////		document.setFile(pdfFile);
////		// create a file to write the extracted text to
////		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileTmp));
////
////		// Get text from the first page of the document, assuming that there
////		// is text to extract.
////		for (int pageNumber = 0, max = document.getNumberOfPages();
////			 pageNumber < max; pageNumber++) {
////			PageText pageText = document.getPageText(pageNumber);
////			Log.d("Extracting page text: ", pageNumber + ".");
////			if (pageText != null && pageText.getPageLines() != null) {
////				fileWriter.write(pageText.toString());
////			}
////		}
////		// close the writer
////		fileWriter.flush();
////		fileWriter.close();
////        // clean up resources
////        document.dispose();
////		File file = new File(txtFile);
////		file.delete();
////		fileTmp.renameTo(file);
////	}
//
////	public static void parsePdfToText(String pdfFile, String txtFile)
////	throws IOException {
////		Log.i("Source PDF:", pdfFile);
////		Log.i("Destination txtFile: ", txtFile);
////		PdfReader reader = new PdfReader(pdfFile);
////		String tmpTxt = txtFile + ".tmp";
////		PrintWriter out = new PrintWriter(new FileOutputStream(tmpTxt));
////
////		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
////			out.println(PdfTextExtractor.getTextFromPage(reader, i,
////														 new LocationTextExtractionStrategy()));
//////			out.println(PdfTextExtractor.getTextFromPage(
//////			 		reader, i, new SimpleTextExtractionStrategy()));
////		}
////		flushClose(out);
////		File file = new File(txtFile);
////		file.delete();
////		new File(tmpTxt).renameTo(file);
////	}
//
//	public static void pdfToText(String pdfPath, String txtPath) throws IOException {
//
//		File txtFile = new File(txtPath);
//		if (!txtFile.getParentFile().exists()) {
//			txtFile.getParentFile().mkdirs();
//		}
//
//		if (!txtFile.exists()
//			|| (txtFile.lastModified() < new File(pdfPath).lastModified())) {
//			// try {
//			// PDFBoxToHtml.convertToText(pdfPath, txtPath, null);
//			// LOGGER.info("Used PDFBoxToHtml successfully");
//			// } catch (Throwable t) {
//
//			//parsePdfToText(pdfPath, txtPath);
//			Log.d("convert pdf", "Used ItextPdfToHtml successfully");
//			// String command = "./lib/pdftohtml.exe \"" + pdfPath
//			// + "\" \"" + txtPath + "\"";
//			// Util.LOGGER.info(command);
//			// Runtime.getRuntime().exec(command);
//			// File f = new File(txtPath);
//			// LOGGER.info("file: " + f);
//			// File file2 = new File(txtPath.substring(0, txtPath.length()
//			// - ".html".length())
//			// + "s.html");
//			// LOGGER.info("file2: " + file2);
//			// if (file2.renameTo(f)) {
//			// LOGGER.info("Rename successfully: "
//			// + f.getAbsolutePath());
//			// htmlFile = f;
//			// } else {
//			// htmlFile = file2;
//			// LOGGER.info("Renaming to " + f.getAbsolutePath()
//			// + " not OK");
//			// }
//			// LOGGER.info("Used pdftohtml.exe successfully");
//			// // FileUtil.printInputStream(p.getInputStream(),
//			// // p.getOutputStream(), null, null);
//			// }
//		} else {
//			Log.d(pdfPath + " has already converted before to file : ", txtPath);
//		}
//	}
//
//}
