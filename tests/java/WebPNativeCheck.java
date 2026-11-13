import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * LDEV-6525: checks on the current OS/arch (CI runs it on macOS arm64) that the native WebP coder
 * from com.github.usefulness:webp-imageio loads and can write and read WebP, without Lucee.
 * Run: java -cp "deps/*" tests/java/WebPNativeCheck.java
 */
public class WebPNativeCheck {

	public static void main(String[] args) throws Exception {
		System.out.println("os.name=" + System.getProperty("os.name") + " os.arch=" + System.getProperty("os.arch") + " java=" + System.getProperty("java.version"));
		for (int type: new int[] { BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB }) {
			BufferedImage img = new BufferedImage(64, 48, type);
			Graphics2D g = img.createGraphics();
			g.setColor(Color.RED);
			g.fillRect(0, 0, 64, 48);
			g.setColor(Color.BLUE);
			g.fillRect(8, 8, 20, 20);
			g.dispose();

			Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
			if (!writers.hasNext()) throw new IllegalStateException("no WebP writer registered");
			ImageWriter writer = writers.next();
			System.out.println("writer: " + writer.getClass().getName() + " from " + writer.getClass().getProtectionDomain().getCodeSource().getLocation());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
				writer.setOutput(ios);
				writer.write(img);
			}
			finally {
				writer.dispose();
			}
			byte[] bytes = baos.toByteArray();
			String header = new String(bytes, 0, 4, "US-ASCII") + new String(bytes, 8, 4, "US-ASCII");
			if (bytes.length == 0 || !"RIFFWEBP".equals(header)) throw new IllegalStateException("not a WebP file: " + bytes.length + " bytes, header " + header);
			BufferedImage back = ImageIO.read(new ByteArrayInputStream(bytes));
			if (back == null || back.getWidth() != 64 || back.getHeight() != 48) throw new IllegalStateException("WebP did not read back: " + back);
			System.out.println("type " + type + ": wrote " + bytes.length + " bytes, read back " + back.getWidth() + "x" + back.getHeight());
		}
		System.out.println("OK");
	}
}
