import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class ExpansaoEqualizacaoHistograma_ implements PlugIn {
	ImagePlus imagemOriginal;
	ImageProcessor processadorOriginal;

	public void run(String arg) {
		imagemOriginal = IJ.getImage();
		int tipo = imagemOriginal.getType();

		if (tipo != ImagePlus.GRAY8) {
			IJ.error("A imagem deve estar no formato 8 bits.");
			return;
		}

		processadorOriginal = imagemOriginal.getProcessor();
		GenericDialog gd = new GenericDialog("Escolher Operação");
		gd.addRadioButtonGroup("Técnica", new String[] { "Expansão", "Equalização" }, 1, 2, "Expansão");
		gd.showDialog();

		if (gd.wasCanceled()) {
			IJ.showStatus("Operação cancelada.");
			return;
		}

		String escolha = gd.getNextRadioButton();
		ImageProcessor copiaProcessador = processadorOriginal.duplicate();
		int[] histograma = obterHistograma(copiaProcessador);

		if (escolha.equals("Expansão")) {
			expandirHistograma(copiaProcessador, histograma);
		} else {
			equalizarHistograma(copiaProcessador, histograma);
		}

		ImagePlus imagemProcessada = new ImagePlus("Pré-visualização: " + escolha, copiaProcessador);
		imagemProcessada.show();

		GenericDialog confirmacao = new GenericDialog("Confirmar alteração");
		confirmacao.setTitle("Aplicar resultado?");
		confirmacao.showDialog();

		if (confirmacao.wasCanceled() || !confirmacao.wasOKed()) {
			IJ.showStatus("Alteração não aplicada.");
			imagemProcessada.close();
			return;
		}

		copiarPixels(copiaProcessador, processadorOriginal);

		imagemOriginal.updateAndDraw();
		imagemProcessada.close();
		IJ.showStatus("Alteração aplicada com sucesso.");
	}
	
	
	
	
	
	
	public int[] obterHistograma(ImageProcessor processador) {
		int[] histograma = new int[256];
		for (int y = 0; y < processador.getHeight(); y++) {
			for (int x = 0; x < processador.getWidth(); x++) {
				int valor = processador.getPixel(x, y);
				histograma[valor]++; // cada oisição do vetor é uma cor e aqui ele faz a contagem de quantas vezes a cor aparece naquela imagem (ver histograma em cinza https://www.bing.com/images/search?view=detailV2&ccid=WbsG6jx2&id=F4D109D4BC87C5212C38DA478489561292F84980&thid=OIP.WbsG6jx2uLR-VZixPQ6YwQAAAA&mediaurl=https%3a%2f%2fwww.researchgate.net%2fprofile%2fThomaz-Almeida%2fpublication%2f305641202%2ffigure%2ffig2%2fAS%3a388066297565187%401469533623289%2fFigura-226-imagem-real-a-em-tons-de-cinza-e-b-seu-histograma.png&cdnurl=https%3a%2f%2fth.bing.com%2fth%2fid%2fR.59bb06ea3c76b8b47e5598b13d0e98c1%3frik%3dgEn4khJWiYRH2g%26pid%3dImgRaw%26r%3d0&exph=269&expw=437&q=hitograma+escala+de+cinza&FORM=IRPRST&ck=7DA5923DD1376662D3D6ADCF8F5C8041&selectedIndex=1&itb=0&ajaxhist=0&ajaxserp=0)
			}
		}
		return histograma;
	}
	
	
	
	
	

	public void expandirHistograma(ImageProcessor processador, int[] histograma) {
		int inicio = PosicaoInicial(histograma);
		int fim = PosicaoFinal(histograma);
		if (inicio == fim) {
			IJ.showMessage("A imagem possui apenas uma tonalidade.");
			return;
		}

		for (int y = 0; y < processador.getHeight(); y++) {
			for (int x = 0; x < processador.getWidth(); x++) {
				int valor = processador.getPixel(x, y);
				int novoValor = (valor - inicio) * 255 / (fim - inicio);//algoritimo pra não estourar a imagem
				novoValor = Math.min(255, Math.max(0, novoValor));
				processador.putPixel(x, y, novoValor);
			}
		}

		IJ.log("Expansão concluída: [" + inicio + " - " + fim + "] → [0 - 255]");
	}

	
	
	
	
	
	
	public void equalizarHistograma(ImageProcessor processador, int[] histograma) {
		int totalPixels = processador.getWidth() * processador.getHeight();
		double[] probabilidade = new double[256];
		double[] acumulado = new double[256];
		int[] mapeamento = new int[256];

		for (int i = 0; i < 256; i++) {
			probabilidade[i] = (double) histograma[i] / totalPixels;
		}

		acumulado[0] = probabilidade[0];
		for (int i = 1; i < 256; i++) {
			acumulado[i] = acumulado[i - 1] + probabilidade[i];
		}

		for (int i = 0; i < 256; i++) {
			mapeamento[i] = (int) Math.round(acumulado[i] * 255);
		}

		for (int y = 0; y < processador.getHeight(); y++) {
			for (int x = 0; x < processador.getWidth(); x++) {
				int valor = processador.getPixel(x, y);
				processador.putPixel(x, y, mapeamento[valor]);
			}
		}

		IJ.log("Equalização de histograma concluída.");
	}

	public int PosicaoInicial(int[] histograma) {
		for (int i = 0; i < 256; i++) {
			if (histograma[i] != 0)
				return i;
		}
		return 0;
	}

	public int PosicaoFinal(int[] histograma) {
		for (int i = 255; i >= 0; i--) {
			if (histograma[i] != 0)
				return i;
		}
		return 255;
	}

	public void copiarPixels(ImageProcessor origem, ImageProcessor destino) {
		int largura = origem.getWidth();
		int altura = origem.getHeight();

		for (int y = 0; y < altura; y++) {
			for (int x = 0; x < largura; x++) {
				int valor = origem.getPixel(x, y);
				destino.putPixel(x, y, valor);
			}
		}
	}
}
