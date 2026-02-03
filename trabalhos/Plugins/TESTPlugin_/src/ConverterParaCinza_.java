import ij.*;
import ij.plugin.PlugIn;
import ij.process.*;

import java.awt.*;

public class ConverterParaCinza_ implements PlugIn {
	private ImagePlus imagemOriginal;
	private ImageProcessor processadorOriginal;
	private String metodoSelecionado = "Média";
	private boolean criarNovaImagem = false;

	public void run(String arg) {
		imagemOriginal = IJ.getImage();

		if (imagemOriginal.getType() != ImagePlus.COLOR_RGB) {
			IJ.error("A imagem deve estar no formato RGB.");
			return;
		}

		processadorOriginal = imagemOriginal.getProcessor();

		Frame janela = new Frame("Conversão para Escala de Cinza");
		janela.setLayout(new GridLayout(6, 1));

		CheckboxGroup grupoMetodos = new CheckboxGroup();
		Checkbox radioMedia = new Checkbox("Média (R+G+B)/3", grupoMetodos, true);
		Checkbox radioLuminancia = new Checkbox("Luminância (0.299R + 0.587G + 0.114B)", grupoMetodos, false);
		Checkbox radioDessaturacao = new Checkbox("Dessaturação (maior+menor)/2", grupoMetodos, false);

		Checkbox checkboxNovaImagem = new Checkbox("Criar nova imagem em tons de cinza", false);

		Button ok = new Button("OK");
		Button cancelar = new Button("Cancelar");

		janela.add(radioMedia);
		janela.add(radioLuminancia);
		janela.add(radioDessaturacao);
		janela.add(checkboxNovaImagem);
		janela.add(ok);
		janela.add(cancelar);
		janela.pack();
		janela.setVisible(true);

		radioMedia.addItemListener(e -> metodoSelecionado = "Média");
		radioLuminancia.addItemListener(e -> metodoSelecionado = "Luminância");
		radioDessaturacao.addItemListener(e -> metodoSelecionado = "Dessaturação");

		checkboxNovaImagem.addItemListener(e -> criarNovaImagem = checkboxNovaImagem.getState());

		ok.addActionListener(e -> {
			aplicarConversao();
			janela.dispose();
		});

		cancelar.addActionListener(e -> janela.dispose());
	}

	private void aplicarConversao() {
		int largura = processadorOriginal.getWidth();
		int altura = processadorOriginal.getHeight();
		int[] rgb = new int[3];

		ImageProcessor cinza = new ByteProcessor(largura, altura);

		for (int y = 0; y < altura; y++) {
			for (int x = 0; x < largura; x++) {
				processadorOriginal.getPixel(x, y, rgb);
				int valorCinza = 0;

				switch (metodoSelecionado) {
				case "Média":
					valorCinza = (rgb[0] + rgb[1] + rgb[2]) / 3;
					break;

				case "Luminância":
					valorCinza = (int) (0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);
					break;

				case "Dessaturação":
					int max = Math.max(rgb[0], Math.max(rgb[1], rgb[2]));
					int min = Math.min(rgb[0], Math.min(rgb[1], rgb[2]));
					valorCinza = (max + min) / 2;
					break;
				}

				cinza.putPixel(x, y, valorCinza);
			}
		}

		if (criarNovaImagem) {
			ImagePlus imagemCinza = new ImagePlus("Imagem em tons de cinza - " + metodoSelecionado, cinza);
			imagemCinza.show();
		} else {
			imagemOriginal.setProcessor(cinza);
			imagemOriginal.updateAndDraw();
		}
	}
}
