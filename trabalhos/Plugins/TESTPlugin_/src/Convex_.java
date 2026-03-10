import ij.*;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import java.util.LinkedList;
import java.util.Queue;

public class Convex_ implements PlugIn {

    ImagePlus imagemOriginal;
    ImageProcessor entrada;

    public void run(String arg) {

        imagemOriginal = IJ.getImage();

        if (imagemOriginal.getType() != ImagePlus.GRAY8) {
            IJ.error("A imagem deve estar em 8 bits.");
            return;
        }

        entrada = imagemOriginal.getProcessor();

        ImageProcessor resultado = aplicarComponentesConexos(entrada);

        ImagePlus novaImagem = new ImagePlus("Componentes Conexos", resultado);
        novaImagem.show();

        IJ.showStatus("Componentes conexos calculados.");
    }

    private ImageProcessor aplicarComponentesConexos(ImageProcessor ip) {

        int altura = ip.getHeight();
        int largura = ip.getWidth();

        ImageProcessor saida = new ByteProcessor(largura, altura);


        saida.setValue(255);// fundo branco
        saida.fill();

        int[][] visitado = new int[largura][altura];

        int label = 1;

        Queue<int[]> fila = new LinkedList<>();

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {

                int pixel = ip.getPixel(x, y);

                if (pixel != 0 && visitado[x][y] == 0) {

                    int tomCinza = (label * 40) % 256;

                    visitado[x][y] = 1;

                    saida.putPixel(x, y, tomCinza);

                    fila.add(new int[]{x, y});


                    }

                    label++;
                }
            }

        return saida;
    }
}