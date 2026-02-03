import ij.IJ;
import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class SepararRgb_ implements PlugIn{
	public void run(String arg) {
		ImagePlus imagem = IJ.getImage();
		IJ.log("Imagem obtida");
		ImageProcessor processador = imagem.getProcessor();
		IJ.log("Processador obtido");
		int altura_imagem = imagem.getHeight();
		IJ.log("Altura:");
		IJ.log(String.valueOf(altura_imagem));
		int largura_imagem = imagem.getWidth();
		IJ.log("Largura:");
		IJ.log(String.valueOf(largura_imagem));
		
		
		
		if (imagem.getType() != ImagePlus.COLOR_RGB) {
			IJ.log("Imagem não é RGB");
			IJ.error("A imagem não é RGB");
		}
		else {	
			ImagePlus red = IJ.createImage("Red", "8-bit black", largura_imagem, altura_imagem, 1);
			IJ.log("Imagem vermelha criada");
			ImageProcessor processador_red = red.getProcessor();
			IJ.log("Processador vermelho obtido");
			
			ImagePlus green = IJ.createImage("Green", "8-bit black", largura_imagem, altura_imagem, 1);
			IJ.log("Imagem verde");
			ImageProcessor processador_green = green.getProcessor();
			IJ.log("Processador verde obtido");
			
			ImagePlus blue = IJ.createImage("Blue", "8-bit black", largura_imagem, altura_imagem, 1);
			IJ.log("Imagem azul obtido");
			ImageProcessor processador_blue = blue.getProcessor();
			IJ.log("Processador azul obtido");
			
			int x, y, valorPixel[] = {0,0,0};
			IJ.log("Viaráveis auxiliares");
			
			for (x = 0; x < largura_imagem; x++) {
				for (y = 0; y < altura_imagem; y++) {
					valorPixel = processador.getPixel(x, y, valorPixel);
					processador_red.putPixel(x, y, valorPixel[0]);
					processador_green.putPixel(x, y, valorPixel[1]);
					processador_blue.putPixel(x, y, valorPixel[2]);	
				}	
			}
			IJ.log("Saída do loop");
			imagem.updateAndDraw();
			IJ.log("Imagem original atualizada");
			red.show();
			IJ.log("Imagem Red");
			green.show();
			IJ.log("Imagem Green");
			blue.show();
			IJ.log("Imagem azul");
		}
		
	}
}
