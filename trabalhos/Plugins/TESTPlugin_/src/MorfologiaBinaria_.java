import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.*;

public class MorfologiaBinaria_ implements PlugIn {
    ImagePlus imagemOriginal;
    ImageProcessor processadorOriginal;

    public void run(String arg) {
        imagemOriginal = IJ.getImage();
        int tipo = imagemOriginal.getType();

        if (tipo != ImagePlus.GRAY8) {
            IJ.error("A imagem deve estar binarizada (preto e branco).");
            return;
        }

        processadorOriginal = imagemOriginal.getProcessor();

        GenericDialog gd = new GenericDialog("Operações Morfológicas");
        gd.addRadioButtonGroup("Técnica", new String[]{"Outline", "Skeleton"}, 1, 2, "Outline");
        gd.showDialog();

        if (gd.wasCanceled()) {
            IJ.showStatus("Operação cancelada.");
            return;
        }

        String escolha = gd.getNextRadioButton();
        ImageProcessor copiaProcessador = processadorOriginal.duplicate();

        if (escolha.equals("Outline")) {
            aplicarOutline(copiaProcessador);
        } else {
            aplicarSkeleton(copiaProcessador);
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

    public void aplicarOutline(ImageProcessor ip) {
        ImageProcessor erodida = erosaoBinaria(ip.duplicate());
        ip.copyBits(erodida, 0, 0, Blitter.SUBTRACT);
        IJ.log("Outline concluído: imagem - erosão.");
    }

    public void aplicarSkeleton(ImageProcessor ip) {
        ImageProcessor atual = ip.duplicate();
        ImageProcessor resultado = new ByteProcessor(ip.getWidth(), ip.getHeight());

        while (contaPixelsBrancos(atual) > 0) {
            ImageProcessor erodida = erosaoBinaria(atual);

            
            if (contaPixelsBrancos(erodida) == contaPixelsBrancos(atual)) break;

            ImageProcessor abertura = dilatacaoBinaria(erodida.duplicate());
            ImageProcessor borda = atual.duplicate();
            borda.copyBits(abertura, 0, 0, Blitter.SUBTRACT);

            
            binarizar(borda);

            
            resultado.copyBits(borda, 0, 0, Blitter.OR);

            atual = erodida;
        }

        binarizar(resultado);
        ip.copyBits(resultado, 0, 0, Blitter.COPY);
        IJ.log("Esqueletização concluída.");
    }

    
    private void binarizar(ImageProcessor ip) {
        for (int y = 0; y < ip.getHeight(); y++) {
            for (int x = 0; x < ip.getWidth(); x++) {
                ip.set(x, y, ip.get(x, y) > 0 ? 255 : 0);
            }
        }
    }


    public ImageProcessor erosaoBinaria(ImageProcessor ip) {
        ImageProcessor saida = ip.duplicate();
        int largura = ip.getWidth();
        int altura = ip.getHeight();

        for (int y = 1; y < altura - 1; y++) {
            for (int x = 1; x < largura - 1; x++) {
                if (ip.get(x, y) == 255) {
                    if (ip.get(x, y - 1) != 255 || ip.get(x - 1, y) != 255 ||
                        ip.get(x, y + 1) != 255 || ip.get(x + 1, y) != 255) {
                        saida.set(x, y, 0);
                    }
                }
            }
        }

        return saida;
    }

    public ImageProcessor dilatacaoBinaria(ImageProcessor ip) {
        ImageProcessor saida = new ByteProcessor(ip.getWidth(), ip.getHeight());
        int largura = ip.getWidth();
        int altura = ip.getHeight();

        for (int y = 1; y < altura - 1; y++) {
            for (int x = 1; x < largura - 1; x++) {
                if (ip.get(x, y) == 255) {
                    saida.set(x, y, 255);
                    saida.set(x, y - 1, 255);
                    saida.set(x - 1, y, 255);
                    saida.set(x, y + 1, 255);
                    saida.set(x + 1, y, 255);
                }
            }
        }

        return saida;
    }

    public int contaPixelsBrancos(ImageProcessor ip) {
        int count = 0;
        for (int y = 0; y < ip.getHeight(); y++) {
            for (int x = 0; x < ip.getWidth(); x++) {
                if (ip.get(x, y) == 255) count++;
            }
        }
        return count;
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
