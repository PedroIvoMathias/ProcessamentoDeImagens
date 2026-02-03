import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
//Desenvolver um plugin para a aplicação de filtros não lineares

public class FiltroSobelMedianaFiltrosNaoLineares_ implements PlugIn {
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

        GenericDialog gd = new GenericDialog("Escolher Filtro");
        gd.addRadioButtonGroup("Filtro", new String[]{"Sobel", "Mediana"}, 1, 2, "Sobel");
        gd.showDialog();

        if (gd.wasCanceled()) {
            IJ.showStatus("Operação cancelada.");
            return;
        }

        String escolha = gd.getNextRadioButton();
        ImageProcessor copiaProcessador = processadorOriginal.duplicate();

        if (escolha.equals("Sobel")) {
            aplicarSobel(copiaProcessador);
        } else if (escolha.equals("Mediana")) {
            aplicarMediana(copiaProcessador);
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

    private void aplicarSobel(ImageProcessor ip) {
        float[] kernelHorizontal = {
            -1, -2, -1,
             0,  0,  0,
             1,  2,  1
        };
        float[] kernelVertical = {
            -1, 0, 1,
            -2, 0, 2,
            -1, 0, 1
        };

        ImageProcessor copia = ip.duplicate();
        ImageProcessor gx = ip.duplicate();
        ImageProcessor gy = ip.duplicate();

        aplicarConvolucao(copia, gx, kernelHorizontal);
        aplicarConvolucao(copia, gy, kernelVertical);

        new ImagePlus("Sobel Horizontal", gx.duplicate()).show();
        new ImagePlus("Sobel Vertical", gy.duplicate()).show();

        for (int y = 0; y < ip.getHeight(); y++) {
            for (int x = 0; x < ip.getWidth(); x++) {
                int valGx = gx.getPixel(x, y);
                int valGy = gy.getPixel(x, y);
                int val = (int) Math.round(Math.sqrt(valGx * valGx + valGy * valGy));
                val = Math.min(255, Math.max(0, val));
                ip.putPixel(x, y, val);
            }
        }

        new ImagePlus("Sobel Combinado", ip.duplicate()).show();
    }

    private void aplicarConvolucao(ImageProcessor entrada, ImageProcessor destino, float[] kernel) {
        int largura = entrada.getWidth();
        int altura = entrada.getHeight();
        ImageProcessor copia = entrada.duplicate();

        for (int y = 1; y < altura - 1; y++) {
            for (int x = 1; x < largura - 1; x++) {
                float soma = 0;
                int k = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        soma += copia.getPixel(x + kx, y + ky) * kernel[k++];
                    }
                }
                int valor = Math.round(soma);
                valor = Math.min(255, Math.max(0, valor));
                destino.putPixel(x, y, valor);
            }
        }
    }

    private void aplicarMediana(ImageProcessor ip) {
        int largura = ip.getWidth();
        int altura = ip.getHeight();
        ImageProcessor copia = ip.duplicate();

        for (int y = 1; y < altura - 1; y++) {
            for (int x = 1; x < largura - 1; x++) {
                int[] vizinhos = new int[9];
                int idx = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        vizinhos[idx++] = copia.getPixel(x + kx, y + ky);
                    }
                }
                java.util.Arrays.sort(vizinhos);
                ip.putPixel(x, y, vizinhos[4]); 
            }
        }
    }

    private void copiarPixels(ImageProcessor origem, ImageProcessor destino) {
        int largura = origem.getWidth();
        int altura = origem.getHeight();

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                destino.putPixel(x, y, origem.getPixel(x, y));
            }
        }
    }
}
