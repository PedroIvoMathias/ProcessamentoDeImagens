import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;
import ij.WindowManager;

public class JuntarRgb_ implements PlugIn {
	public void run(String arg) {

		ImagePlus redImage = WindowManager.getImage("Red");
		ImagePlus greenImage = WindowManager.getImage("Green");
		ImagePlus blueImage = WindowManager.getImage("Blue");

		if (redImage == null || greenImage == null || blueImage == null) {
			IJ.error(
					"Certifique-se de que as imagens 'Red', 'Green' e 'Blue' estejam abertas e corretamente nomeadas.");
			return;
		}

		int width = redImage.getWidth();
		int height = redImage.getHeight();

		if (greenImage.getWidth() != width || greenImage.getHeight() != height || blueImage.getWidth() != width
				|| blueImage.getHeight() != height) {
			IJ.error("As imagens Red, Green e Blue devem ter o mesmo tamanho.");
			return;
		}

		ImageProcessor redProcessor = redImage.getProcessor();
		ImageProcessor greenProcessor = greenImage.getProcessor();
		ImageProcessor blueProcessor = blueImage.getProcessor();

		ColorProcessor rgbProcessor = new ColorProcessor(width, height);
		int[] rgb = new int[3];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				rgb[0] = redProcessor.getPixel(x, y); // R
				rgb[1] = greenProcessor.getPixel(x, y); // G
				rgb[2] = blueProcessor.getPixel(x, y); // B
				rgbProcessor.putPixel(x, y, rgb);
			}
		}

		ImagePlus rgbImage = new ImagePlus("Imagem RGB Combinada", rgbProcessor);
		rgbImage.show();
	}
}