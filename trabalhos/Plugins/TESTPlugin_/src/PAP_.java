import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.*;
import java.awt.event.*;

public class PAP_ implements PlugIn {
	ImagePlus imagemOriginal;
	ImagePlus imagemAtual;
	ImageProcessor processadorOriginal;
	ImageProcessor processadorAtual;

	int brilho = 0;
	int contraste = 0;
	int solarizacao = 0;
	int dessaturacao = 0;

	public void run(String arg) {
		imagemOriginal = IJ.getImage();

		if (imagemOriginal.getType() != ImagePlus.COLOR_RGB) {
			IJ.error("A imagem deve estar no formato RGB.");
			return;
		}

		imagemAtual = imagemOriginal.duplicate();
		imagemAtual.setTitle("Pré-visualização");
		imagemAtual.show();

		processadorOriginal = imagemOriginal.getProcessor().duplicate();
		processadorAtual = imagemAtual.getProcessor();

		Frame janela = new Frame("Ajustes Interativos");
		janela.setLayout(new GridLayout(6, 1));

		Scrollbar sliderBrilho = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, -255, 256);
		Scrollbar sliderContraste = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, -100, 101);
		Scrollbar sliderSolarizacao = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 0, 256);
		Scrollbar sliderDessaturacao = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 0, 256);

		janela.add(new Label("Brilho"));
		janela.add(sliderBrilho);

		janela.add(new Label("Contraste"));
		janela.add(sliderContraste);

		janela.add(new Label("Solarização"));
		janela.add(sliderSolarizacao);

		janela.add(new Label("Dessaturação"));
		janela.add(sliderDessaturacao);

		Panel botoes = new Panel();
		Button ok = new Button("OK");
		Button cancelar = new Button("Cancelar");
		botoes.add(ok);
		botoes.add(cancelar);
		janela.add(botoes);

		AdjustmentListener atualizador = new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				brilho = sliderBrilho.getValue();
				contraste = sliderContraste.getValue();
				solarizacao = sliderSolarizacao.getValue();
				dessaturacao = sliderDessaturacao.getValue();
				aplicarAjustes();
			}
		};

		sliderBrilho.addAdjustmentListener(atualizador);
		sliderContraste.addAdjustmentListener(atualizador);
		sliderSolarizacao.addAdjustmentListener(atualizador);
		sliderDessaturacao.addAdjustmentListener(atualizador);

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imagemOriginal.setProcessor(imagemAtual.getProcessor());
				imagemOriginal.updateAndDraw();
				imagemAtual.close();
				janela.dispose();
			}
		});

		cancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imagemOriginal.setProcessor(processadorOriginal);
				imagemOriginal.updateAndDraw();
				imagemAtual.close();
				janela.dispose();
			}
		});

		janela.pack();
		janela.setVisible(true);
	}

	private void aplicarAjustes() {
		processadorAtual = processadorOriginal.duplicate();

		int width = processadorAtual.getWidth();
		int height = processadorAtual.getHeight();
		int[] rgb = new int[3];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				processadorOriginal.getPixel(x, y, rgb);

				rgb[0] = clamp(rgb[0] + brilho);
				rgb[1] = clamp(rgb[1] + brilho);
				rgb[2] = clamp(rgb[2] + brilho);

				rgb[0] = clamp((int) ((rgb[0] - 128) * (1 + contraste / 100.0) + 128));
				rgb[1] = clamp((int) ((rgb[1] - 128) * (1 + contraste / 100.0) + 128));
				rgb[2] = clamp((int) ((rgb[2] - 128) * (1 + contraste / 100.0) + 128));

				if (rgb[0] < solarizacao)
					rgb[0] = 255 - rgb[0];
				if (rgb[1] < solarizacao)
					rgb[1] = 255 - rgb[1];
				if (rgb[2] < solarizacao)
					rgb[2] = 255 - rgb[2];

				int mediaCinza = (rgb[0] + rgb[1] + rgb[2]) / 3;
				rgb[0] = mix(rgb[0], mediaCinza, dessaturacao);
				rgb[1] = mix(rgb[1], mediaCinza, dessaturacao);
				rgb[2] = mix(rgb[2], mediaCinza, dessaturacao);

				processadorAtual.putPixel(x, y, rgb);
			}
		}

		imagemAtual.setProcessor(processadorAtual);
		imagemAtual.updateAndDraw();
	}

	private int clamp(int val) {
		return Math.max(0, Math.min(255, val));
	}

	private int mix(int original, int alvo, int porcentagem) {
		return clamp((original * (255 - porcentagem) + alvo * porcentagem) / 255);
	}
}
