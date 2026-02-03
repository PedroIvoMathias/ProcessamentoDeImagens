import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.*;
//Desenvolver um plugin para a aplicação de filtros lineares

public class FiltroConvulacaoFiltrosLineares_ implements PlugIn {
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

        GenericDialog gd = new GenericDialog("Filtros 3x3");
        gd.addRadioButtonGroup("Escolha o filtro:", 
            new String[]{"Passa-Baixas (Média)", "Passa-Altas", "Detecção de Bordas (Sobel Horizontal)"},
            1, 3, "Passa-Baixas (Média)");
        gd.showDialog();

        if (gd.wasCanceled()) {
            IJ.showStatus("Operação cancelada.");
            return;
        }

        String escolha = gd.getNextRadioButton();

        ImageProcessor copiaProcessador = processadorOriginal.duplicate();

        if (escolha.equals("Passa-Baixas (Média)")) {
            aplicarConvolucao(copiaProcessador, getKernelMedia());
        } else if (escolha.equals("Passa-Altas")) {
            aplicarConvolucao(copiaProcessador, getKernelPassaAltas());
        } else {
            aplicarConvolucao(copiaProcessador, getKernelSobelHorizontal());
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
        IJ.showStatus("Filtro '" + escolha + "' aplicado com sucesso.");
    }

    
    private float[] getKernelMedia() {
        float[] k = new float[9];
        for (int i = 0; i < 9; i++) k[i] = 1f / 9f;
        return k;
    }

    private float[] getKernelPassaAltas() {
        return new float[]{
            -1, -1, -1,
            -1,  8, -1,
            -1, -1, -1
        };
    }

    private float[] getKernelSobelHorizontal() {
        return new float[]{
            -1, -2, -1,
             0,  0,  0,
             1,  2,  1
        };
    }

    public void aplicarConvolucao(ImageProcessor ip, float[] kernel) {
        int largura = ip.getWidth();
        int altura = ip.getHeight();
        ImageProcessor copia = ip.duplicate();

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
                ip.putPixel(x, y, valor);
            }
        }
    }

    public void copiarPixels(ImageProcessor origem, ImageProcessor destino) {
        int largura = origem.getWidth();
        int altura = origem.getHeight();

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                destino.putPixel(x, y, origem.getPixel(x, y));
            }
        }
    }
}
