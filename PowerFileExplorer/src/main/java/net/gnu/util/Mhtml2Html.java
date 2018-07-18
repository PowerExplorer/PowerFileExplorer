//package net.gnu.util;
//
//import org.apache.james.mime4j.dom.Message;
//import org.apache.james.mime4j.dom.Multipart;
//import org.apache.james.mime4j.dom.field.ContentTypeField;
//import org.apache.james.mime4j.message.DefaultMessageBuilder;
//import org.apache.james.mime4j.stream.MimeConfig;
//import java.io.File;
//import java.io.InputStream;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.List;
//import org.apache.james.mime4j.dom.Entity;
//import org.apache.james.mime4j.message.BasicTextBody;
//import java.io.FileOutputStream;
//import java.io.BufferedOutputStream;
//import java.io.BufferedInputStream;
//import java.lang.reflect.Field;
//import org.apache.james.mime4j.dom.SingleBody;
//import org.apache.james.mime4j.dom.Body;
//
//public class Mhtml2Html {
//	/**
//	 * Use Mime4J MessageBuilder to parse an mhtml file (assumes multipart) into
//	 * separate html files.
//	 * Files will be written to outDir (or parent) as baseName + partIdx + ext.
//	 */
//	public static void parseMhtToFile(final File mhtFile, File outDir) throws FileNotFoundException, IOException {
//		if (outDir == null) {
//			outDir = mhtFile.getParentFile();
//		}
//		// File baseName will be used in generating new filenames
//		final String mhtBaseName = mhtFile.getName();//.replaceFirst("~/\\.[^\\.]+$/", "");
//
//		// -- Set up Mime parser, using Default Message Builder
//		final MimeConfig parserConfig  = new MimeConfig();
//		parserConfig.setMaxHeaderLen(-1); // The default is a mere 10k
//		parserConfig.setMaxLineLen(-1); // The default is only 1000 characters.
//		parserConfig.setMaxHeaderCount(-1); // Disable the check for header count.
//		final DefaultMessageBuilder builder = new DefaultMessageBuilder();
//		builder.setMimeEntityConfig(parserConfig);
//
//		// -- Parse the MHT stream data into a Message object
//		System.out.println("Parsing " + mhtFile);
//		final InputStream mhtStream = new FileInputStream(mhtFile);
//		Message message = builder.parseMessage(mhtStream);
//
//		// -- Process the resulting body parts, writing to file
//		//assert message.getBody() instanceof Multipart;
//		final Body body = message.getBody();
//		System.out.println(body);
//		if (body instanceof Multipart) {
//			final Multipart multipart = (Multipart) body;
//			final List<Entity> parts = multipart.getBodyParts();
//			int i = 0;
//			int read = 0;
//			for (Entity p : parts) {
//				i++;
//				final ContentTypeField cType = (ContentTypeField) p.getHeader().getField("content-type");
//				System.out.println(p.getClass().getSimpleName() + "\t" + i + "\t" + cType.getMimeType());
//
//				final String partFileName = mhtBaseName + "_" + i + "." + cType.getSubType();
//				final File partFile = new File(outDir, partFileName);
//
//				final FileOutputStream fos = new FileOutputStream(partFile);
//				final BufferedOutputStream bos = new BufferedOutputStream(fos);
//
//				final BufferedInputStream partStream = new BufferedInputStream(((SingleBody)p.getBody()).getInputStream());
//				final byte[] barr = new byte[65536];
//				while ((read = partStream.read(barr)) != -1) {
//					bos.write(barr, 0, read);
//				}
//
//				fos.flush();
//				bos.flush();
//				fos.close();
//				bos.close();
//				partStream.close();
//			}
//		} else {
//			final String partFileName = mhtBaseName + "_extracted.html";
//			final File partFile = new File(outDir, partFileName);
//
//			final FileOutputStream fos = new FileOutputStream(partFile);
//			final BufferedOutputStream bos = new BufferedOutputStream(fos);
//
//			((SingleBody) body).writeTo(bos);
//			fos.flush();
//			bos.flush();
//			fos.close();
//			bos.close();
//			body.dispose();
//		}
//
//
//    }
//}
