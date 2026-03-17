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

        int largura = ip.getWidth();
        int altura = ip.getHeight();

        ImageProcessor saida = new ByteProcessor(largura, altura);

        
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                saida.putPixel(x, y, 255);
            }
        }

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

                    while (!fila.isEmpty()) {

                        int[] p = fila.remove();

                        int px = p[0];
                        int py = p[1];

                        verificarVizinho(px + 1, py, largura, altura, ip, visitado, fila, saida, tomCinza);
                        verificarVizinho(px - 1, py, largura, altura, ip, visitado, fila, saida, tomCinza);
                        verificarVizinho(px, py + 1, largura, altura, ip, visitado, fila, saida, tomCinza);
                        verificarVizinho(px, py - 1, largura, altura, ip, visitado, fila, saida, tomCinza);
                    }

                    label++;
                }
            }
        }

        IJ.showMessage("Componentes", "Total encontrados: " + (label - 1));

        return saida;
    }

    private void verificarVizinho(int x, int y, int largura, int altura,ImageProcessor entrada,int[][] visitado,Queue<int[]> fila,
                                  ImageProcessor saida,int tomCinza) {

        if (x < 0 || x >= largura || y < 0 || y >= altura)
            return;

        if (entrada.getPixel(x, y) != 0 && visitado[x][y] == 0) {

            visitado[x][y] = 1;

            saida.putPixel(x, y, tomCinza);

            fila.add(new int[]{x, y});
        }
    }
}